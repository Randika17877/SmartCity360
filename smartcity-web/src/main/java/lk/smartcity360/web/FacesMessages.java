package lk.smartcity360.web;

import jakarta.ejb.EJBException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/** Small helper for reporting results and errors to the page. */
final class FacesMessages {

    private FacesMessages() {
    }

    static void info(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    static void error(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    /** Reports a failed action, unwrapping bean-validation errors raised inside the EJB layer. */
    static void error(Exception e) {
        Throwable cause = e;
        while (cause != null && !(cause instanceof ConstraintViolationException)) {
            cause = cause.getCause();
        }
        if (cause instanceof ConstraintViolationException violations) {
            for (ConstraintViolation<?> v : violations.getConstraintViolations()) {
                error(v.getMessage());
            }
        } else if (e instanceof EJBException && e.getCause() != null) {
            error(e.getCause().getMessage());
        } else {
            error(e.getMessage() == null ? "Unexpected error" : e.getMessage());
        }
    }
}
