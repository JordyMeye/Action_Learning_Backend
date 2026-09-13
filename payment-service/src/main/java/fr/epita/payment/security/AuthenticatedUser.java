package fr.epita.payment.security;

/** Caller identity, read straight from the JWT claims (this service has no user table). */
public record AuthenticatedUser(String email, String role, Long universityId) {

    public boolean isUniAdmin() {
        return "ROLE_UNI_ADMIN".equals(role);
    }
}
