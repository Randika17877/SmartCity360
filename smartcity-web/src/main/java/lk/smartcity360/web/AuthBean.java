package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import lk.smartcity360.entity.User;
import lk.smartcity360.service.BusinessRuleException;
import lk.smartcity360.service.UserService;

/** Handles user session, sign-in authentication, and new user registration. */
@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserService userService;

    private User currentUser;

    // Sign in fields
    private String login;
    private String password;

    // Sign up fields
    private String regFullName;
    private String regUsername;
    private String regEmail;
    private String regPassword;
    private String regConfirmPassword;
    private String regRole = "CITIZEN";

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isOfficer() {
        return currentUser != null && "OFFICER".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isStaffOrAdmin() {
        return currentUser != null && ("ADMIN".equalsIgnoreCase(currentUser.getRole()) || "OFFICER".equalsIgnoreCase(currentUser.getRole()));
    }

    public boolean isCitizen() {
        return currentUser != null && "CITIZEN".equalsIgnoreCase(currentUser.getRole());
    }

    public String signIn() {
        try {
            currentUser = userService.authenticate(login, password);
            password = null;
            login = null;

            // Synchronize user into session map for servlet filter inspection
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.getSessionMap().put("currentUser", currentUser);

            return "dashboard?faces-redirect=true";
        } catch (BusinessRuleException e) {
            FacesMessages.error(e.getMessage());
            return null;
        } catch (Exception e) {
            FacesMessages.error(e);
            return null;
        }
    }

    public String signUp() {
        if (regPassword == null || !regPassword.equals(regConfirmPassword)) {
            FacesMessages.error("Passwords do not match");
            return null;
        }
        try {
            // Security fix: Public self-registration is strictly restricted to CITIZEN role.
            // Administrative or Officer roles can only be granted internally.
            User newUser = userService.registerCitizen(regUsername, regEmail, regFullName, regPassword);
            currentUser = newUser;

            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.getSessionMap().put("currentUser", currentUser);

            // Clear registration fields
            regFullName = null;
            regUsername = null;
            regEmail = null;
            regPassword = null;
            regConfirmPassword = null;
            regRole = "CITIZEN";

            return "dashboard?faces-redirect=true";
        } catch (BusinessRuleException e) {
            FacesMessages.error(e.getMessage());
            return null;
        } catch (Exception e) {
            FacesMessages.error(e);
            return null;
        }
    }

    public String signOut() {
        currentUser = null;
        login = null;
        password = null;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.getSessionMap().remove("currentUser");
        ec.invalidateSession();
        return "signin?faces-redirect=true";
    }

    /** Redirects to dashboard if already logged in (used on sign-in and sign-up pages). */
    public void redirectIfLoggedIn() {
        if (isLoggedIn()) {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            try {
                ec.redirect(ec.getRequestContextPath() + "/dashboard.html");
            } catch (IOException ignored) {
            }
        }
    }

    // Getters and setters
    public User getCurrentUser() { return currentUser; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRegFullName() { return regFullName; }
    public void setRegFullName(String regFullName) { this.regFullName = regFullName; }

    public String getRegUsername() { return regUsername; }
    public void setRegUsername(String regUsername) { this.regUsername = regUsername; }

    public String getRegEmail() { return regEmail; }
    public void setRegEmail(String regEmail) { this.regEmail = regEmail; }

    public String getRegPassword() { return regPassword; }
    public void setRegPassword(String regPassword) { this.regPassword = regPassword; }

    public String getRegConfirmPassword() { return regConfirmPassword; }
    public void setRegConfirmPassword(String regConfirmPassword) { this.regConfirmPassword = regConfirmPassword; }

    public String getRegRole() { return "CITIZEN"; }
    public void setRegRole(String regRole) { this.regRole = "CITIZEN"; }
}
