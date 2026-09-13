package fr.epita.payment;

import com.jayway.jsonpath.JsonPath;
import fr.epita.payment.model.OutboxEvent;
import fr.epita.payment.repository.OutboxEventRepository;
import fr.epita.payment.repository.PaymentRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HTTP → database → outbox, with H2 instead of Postgres. The brokers are not running here, so
 * the RabbitMQ listeners are not started, no Kafka topic is created, and the outbox relay is
 * kept idle — letting the test inspect the outbox rows the relay would publish.
 */
@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "payment.outbox.poll-interval-ms=3600000"
})
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class PaymentApiIntegrationTest {

    private static final String TUITION = """
            {"amount": 1500.00, "currency": "eur", "description": "Semester 1 tuition", "paymentMethod": "pm_card_visa"}
            """;

    @Autowired MockMvc mockMvc;
    @Autowired PaymentRepository paymentRepository;
    @Autowired OutboxEventRepository outboxRepository;

    @Value("${jwt.secret}") String jwtSecret;

    @Test
    void studentPaymentIsAcceptedAndItsCommandIsWrittenToTheOutbox() throws Exception {
        String id = idOf(pay(token("alice@uni.fr", "ROLE_STUDENT", 1L), "key-alice-1", TUITION)
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", startsWith("/api/payments/")))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.currency").value("EUR")));

        assertThat(outboxRepository.findByAggregateIdOrderByCreatedAtAsc(id)).singleElement().satisfies(row -> {
            assertThat(row.getDestination()).isEqualTo(OutboxEvent.Destination.RABBITMQ);
            assertThat(row.getEventType()).isEqualTo("ProcessPayment");
            assertThat(row.getPayload()).contains(id);
            assertThat(row.getPublishedAt()).isNull();
        });
    }

    @Test
    void retryingWithTheSameIdempotencyKeyReturnsTheOriginalPayment() throws Exception {
        String bob = token("bob@uni.fr", "ROLE_STUDENT", 1L);

        String first = idOf(pay(bob, "key-bob-1", TUITION).andExpect(status().isAccepted()));
        String retry = idOf(pay(bob, "key-bob-1", TUITION).andExpect(status().isAccepted()));

        assertThat(retry).isEqualTo(first);
        assertThat(paymentRepository.findByStudentEmailOrderByCreatedAtDesc("bob@uni.fr")).hasSize(1);
        assertThat(outboxRepository.findByAggregateIdOrderByCreatedAtAsc(first)).hasSize(1);
    }

    @Test
    void reusingAnIdempotencyKeyForADifferentPaymentIsRejected() throws Exception {
        String erin = token("erin@uni.fr", "ROLE_STUDENT", 1L);
        pay(erin, "key-erin-1", TUITION).andExpect(status().isAccepted());

        pay(erin, "key-erin-1", TUITION.replace("1500.00", "20.00")).andExpect(status().isConflict());
    }

    @Test
    void onlyThePayerAndTheirUniversityAdminCanReadAPayment() throws Exception {
        String carol = token("carol@uni.fr", "ROLE_STUDENT", 1L);
        String id = idOf(pay(carol, "key-carol-1", TUITION));

        mockMvc.perform(withToken(get("/api/payments/" + id), carol)).andExpect(status().isOk());
        mockMvc.perform(withToken(get("/api/payments/" + id), token("admin@uni.fr", "ROLE_UNI_ADMIN", 1L)))
                .andExpect(status().isOk());
        mockMvc.perform(withToken(get("/api/payments/" + id), token("dave@uni.fr", "ROLE_STUDENT", 1L)))
                .andExpect(status().isForbidden());
        mockMvc.perform(withToken(get("/api/payments/" + id), token("admin@other.fr", "ROLE_UNI_ADMIN", 2L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAuthenticatedStudentsCanPay() throws Exception {
        pay(token("lecturer@uni.fr", "ROLE_LECTURER", 1L), "key-lecturer-1", TUITION).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/payments").header("Idempotency-Key", "key-anon-1")
                        .contentType(MediaType.APPLICATION_JSON).content(TUITION))
                .andExpect(status().isForbidden());
    }

    @Test
    void idempotencyKeyAndAValidBodyAreRequired() throws Exception {
        String frank = token("frank@uni.fr", "ROLE_STUDENT", 1L);
        mockMvc.perform(withToken(post("/api/payments"), frank)
                        .contentType(MediaType.APPLICATION_JSON).content(TUITION))
                .andExpect(status().isBadRequest());
        pay(frank, "key-frank-1", TUITION.replace("1500.00", "-5")).andExpect(status().isBadRequest());
    }

    private ResultActions pay(String token, String idempotencyKey, String body) throws Exception {
        return mockMvc.perform(withToken(post("/api/payments"), token)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private String idOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    /** A token shaped like the ones the core backend's JwtUtil issues. */
    private String token(String email, String role, Long universityId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("universityId", universityId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                .compact();
    }
}
