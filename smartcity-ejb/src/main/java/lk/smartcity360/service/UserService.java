package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import lk.smartcity360.entity.User;
import lk.smartcity360.security.PasswordHasher;

/** User registration, credential authentication, and account lookup. */
@Stateless
public class UserService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    public User register(String username, String email, String fullName, String password, String role) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessRuleException("Username cannot be blank");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessRuleException("Email cannot be blank");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BusinessRuleException("Full name cannot be blank");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessRuleException("Password must be at least 6 characters");
        }

        String trimmedUsername = username.trim();
        String trimmedEmail = email.trim().toLowerCase();

        if (findByUsername(trimmedUsername).isPresent()) {
            throw new BusinessRuleException("Username '" + trimmedUsername + "' is already taken");
        }
        if (findByEmail(trimmedEmail).isPresent()) {
            throw new BusinessRuleException("Email '" + trimmedEmail + "' is already registered");
        }

        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(password, salt);

        String assignedRole = (role == null || role.isBlank()) ? "CITIZEN" : role.trim().toUpperCase();
        if (!"CITIZEN".equals(assignedRole) && !"OFFICER".equals(assignedRole) && !"ADMIN".equals(assignedRole)) {
            assignedRole = "CITIZEN";
        }

        User user = new User(trimmedUsername, trimmedEmail, fullName.trim(), hash, salt, assignedRole);
        em.persist(user);
        return user;
    }

    /** Convenience method for self-registration: always creates a CITIZEN user. */
    public User registerCitizen(String username, String email, String fullName, String password) {
        return register(username, email, fullName, password, "CITIZEN");
    }

    public User authenticate(String login, String password) {
        if (login == null || login.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessRuleException("Username/email and password are required");
        }

        String trimmedLogin = login.trim();
        List<User> matches = em.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:login) OR LOWER(u.email) = LOWER(:login)",
                        User.class)
                .setParameter("login", trimmedLogin)
                .setMaxResults(1)
                .getResultList();

        if (matches.isEmpty()) {
            throw new BusinessRuleException("Invalid username/email or password");
        }

        User user = matches.get(0);
        if (!PasswordHasher.verify(password, user.getSalt(), user.getPasswordHash())) {
            throw new BusinessRuleException("Invalid username/email or password");
        }

        return user;
    }

    public Optional<User> findByUsername(String username) {
        try {
            User u = em.createQuery("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)", User.class)
                    .setParameter("username", username.trim())
                    .getSingleResult();
            return Optional.of(u);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            User u = em.createQuery("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                    .setParameter("email", email.trim())
                    .getSingleResult();
            return Optional.of(u);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }
}
