package lk.smartcity360.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;
import lk.smartcity360.entity.User;

/**
 * Enforces authentication and role-based access control (RBAC) across SmartCity360 web resources.
 *
 * Access Rules:
 *  - Public: signin, signup, access-denied, and static assets (/resources/*, /jakarta.faces.resource/*)
 *  - Authenticated (Any role: CITIZEN, OFFICER, ADMIN): dashboard, complaints
 *  - Administrative / Operational (OFFICER and ADMIN only): zones, energy, streetlights, policies
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PAGES = Set.of(
            "/signin.html", "/signin.xhtml",
            "/signup.html", "/signup.xhtml",
            "/access-denied.html", "/access-denied.xhtml"
    );

    private static final Set<String> ADMIN_MODULE_PREFIXES = Set.of(
            "/zones",
            "/energy",
            "/streetlights",
            "/policies"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        // 1. Allow static resources (CSS, JS, images, fonts, JSF resource library requests)
        if (isStaticResource(path, requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Allow public authentication and error pages
        if (isPublicPage(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Resolve logged-in user from session (checks direct attribute and CDI AuthBean)
        HttpSession session = httpRequest.getSession(false);
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("currentUser");
            if (user == null) {
                AuthBean authBean = (AuthBean) session.getAttribute("authBean");
                if (authBean != null && authBean.isLoggedIn()) {
                    user = authBean.getCurrentUser();
                }
            }
        }

        // 4. Handle root context or index page
        if (path.isEmpty() || "/".equals(path) || "/index.html".equalsIgnoreCase(path) || "/index.xhtml".equalsIgnoreCase(path)) {
            if (user != null) {
                httpResponse.sendRedirect(contextPath + "/dashboard.html");
            } else {
                httpResponse.sendRedirect(contextPath + "/signin.html");
            }
            return;
        }

        // 5. Unauthenticated access to protected page -> redirect to signin
        if (user == null) {
            String facesRequest = httpRequest.getHeader("Faces-Request");
            if ("partial/ajax".equals(facesRequest)) {
                httpResponse.setContentType("text/xml");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\""
                                + contextPath + "/signin.html\"></redirect></partial-response>");
            } else {
                httpResponse.sendRedirect(contextPath + "/signin.html");
            }
            return;
        }

        // 6. Role check: evaluate permissions for administrative modules
        String role = user.getRole() != null ? user.getRole().trim().toUpperCase() : "CITIZEN";

        if (isAdminModule(path)) {
            boolean isAuthorized = "ADMIN".equals(role) || "OFFICER".equals(role);
            if (!isAuthorized) {
                // Deny access for unauthorized roles (e.g., CITIZEN accessing administrative modules)
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access Denied: The " + role + " role is not authorized to access administrative module: " + path);
                return;
            }
        }

        // Authenticated and authorized
        chain.doFilter(request, response);
    }

    private boolean isStaticResource(String path, String requestURI) {
        return path.startsWith("/resources/")
                || path.startsWith("/jakarta.faces.resource/")
                || requestURI.contains("ln=")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".svg")
                || path.endsWith(".ico")
                || path.endsWith(".woff")
                || path.endsWith(".woff2");
    }

    private boolean isPublicPage(String path) {
        String lower = path.toLowerCase();
        for (String pub : PUBLIC_PAGES) {
            if (lower.equals(pub)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdminModule(String path) {
        String lower = path.toLowerCase();
        for (String prefix : ADMIN_MODULE_PREFIXES) {
            if (lower.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // Filter cleanup
    }
}
