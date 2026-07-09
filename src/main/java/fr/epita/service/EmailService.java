package fr.epita.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    
    @Async
    public void sendApprovalEmail(String toEmail, String firstName, String platformEmail, String tempPassword) {
        log.info("Sending approval email to {} (platform login: {})", toEmail, platformEmail);
        send(toEmail,
             "Your Action Learning Platform Access Has Been Approved",
             buildApprovalBody(firstName, platformEmail, tempPassword));
    }

    @Async
    public void sendPlatformAdminWelcomeEmail(String toEmail, String firstName, String tempPassword) {
        log.info("Sending platform admin welcome email to {}", toEmail);
        send(toEmail,
             "Your Action Learning Platform Admin Account",
             buildPlatformAdminBody(firstName, toEmail, tempPassword));
    }

    @Async
    public void sendAccountCreatedEmail(String toEmail, String firstName, String loginEmail,
                                        String tempPassword, String roleLabel) {
        log.info("Sending {} welcome email to {} (login: {})", roleLabel, toEmail, loginEmail);
        send(toEmail,
             "Your Action Learning Platform Account Is Ready",
             buildAccountCreatedBody(firstName, loginEmail, tempPassword, roleLabel));
    }

    @Async
    public void sendRejectionEmail(String toEmail, String firstName, String reason) {
        log.info("Sending rejection email to {}", toEmail);
        send(toEmail,
             "Update on Your Action Learning Platform Request",
             buildRejectionBody(firstName, reason));
    }

    @Async
    public void sendNotificationEmail(String toEmail, String firstName, String subject, String message) {
        log.info("Sending notification email to {}", toEmail);
        send(toEmail, subject, buildNotificationBody(firstName, message));
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email successfully sent to {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }


    private String buildApprovalBody(String firstName, String platformEmail, String tempPassword) {
        return layout("Welcome to the Action Learning Platform",
                greeting(firstName)
              + "<p>Your university registration request has been "
              + "<strong style=\"color:#27ae60;\">approved</strong>. Your administrator account "
              + "has been created and is ready to use.</p>"
              + credentialsBlock(platformEmail, tempPassword)
              + "<p>As a university administrator you can now manage your institution's "
              + "programmes, cohorts, lecturers, and students.</p>");
    }

    private String buildAccountCreatedBody(String firstName, String loginEmail, String tempPassword, String roleLabel) {
        return layout("Welcome to the Action Learning Platform",
                greeting(firstName)
              + "<p>A <strong>" + roleLabel + "</strong> account has been created for you on the "
              + "Action Learning Platform and is ready to use.</p>"
              + credentialsBlock(loginEmail, tempPassword));
    }

    private String buildPlatformAdminBody(String firstName, String loginEmail, String tempPassword) {
        return layout("Welcome — Platform Administrator Account Created",
                greeting(firstName)
              + "<p>A platform administrator account has been created for you on the "
              + "Action Learning Platform.</p>"
              + credentialsBlock(loginEmail, tempPassword));
    }

    private String buildRejectionBody(String firstName, String reason) {
        String reasonSection = (reason != null && !reason.isBlank())
                ? "<p><strong>Reason:</strong> " + reason + "</p>"
                : "";
        return layout("Update on Your Registration Request",
                greeting(firstName)
              + "<p>After reviewing your request, we regret to inform you that your university "
              + "registration request has been <strong style=\"color:#e74c3c;\">declined</strong>.</p>"
              + reasonSection
              + "<p>If you believe this decision was made in error or would like to reapply, "
              + "please contact our support team.</p>");
    }

    private String buildNotificationBody(String firstName, String message) {
        return layout("A Notification from Your Lecturer",
                greeting(firstName)
              + "<p>" + message + "</p>"
              + "<p>Log in to the Action Learning Platform to view the details.</p>");
    }


    private String layout(String heading, String innerHtml) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                  <h2 style="color: #2c3e50;">%s</h2>
                  %s
                  <p style="color: #7f8c8d; font-size: 12px; margin-top: 32px;">
                    This is an automated message. Please do not reply to this email.
                  </p>
                </div>
                """.formatted(heading, innerHtml);
    }

    private String greeting(String firstName) {
        return "<p>Dear " + firstName + ",</p>";
    }

    private String credentialsBlock(String loginEmail, String tempPassword) {
        return """
                <div style="background: #f4f6f7; border-left: 4px solid #2E5F9E; padding: 16px; margin: 24px 0; border-radius: 4px;">
                  <h3 style="margin: 0 0 12px 0; color: #2c3e50;">Your Login Credentials</h3>
                  <p style="margin: 4px 0;"><strong>Login Email:</strong> %s</p>
                  <p style="margin: 4px 0;"><strong>Temporary Password:</strong>
                    <code style="background:#fff;padding:2px 6px;border-radius:3px;">%s</code>
                  </p>
                </div>
                <p style="color: #e74c3c;">
                  <strong>Important:</strong> Please log in and change your password immediately
                  using the <em>Change Password</em> option in your profile.
                </p>
                """.formatted(loginEmail, tempPassword);
    }
}
