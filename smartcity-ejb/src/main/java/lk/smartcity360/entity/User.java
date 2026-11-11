package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/** An authenticated application user (citizen, officer, or administrator). */
@Entity
@Table(name = "app_users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    @Column(name = "username", nullable = false, unique = true, length = 60)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 120)
    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 120)
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "password_hash", nullable = false, length = 128)
    private String passwordHash;

    @Column(name = "salt", nullable = false, length = 64)
    private String salt;

    @Column(name = "role", nullable = false, length = 30)
    private String role = "CITIZEN";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public User(String username, String email, String fullName, String passwordHash, String salt, String role) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = (role == null || role.isBlank()) ? "CITIZEN" : role;
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
