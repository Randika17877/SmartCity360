package lk.smartcity360.service;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

/**
 * Ensures baseline municipal data (default accounts, city zones, and policy ordinances)
 * exist in MySQL upon application boot.
 */
@Singleton
@Startup
public class DatabaseBootstrap {

    @EJB
    private UserService userService;

    @EJB
    private CityService cityService;

    @EJB
    private PolicyService policyService;

    @PostConstruct
    public void init() {
        try {
            userService.ensureDefaultUsers();
        } catch (Exception e) {
            // Ignored if already initialized
        }
        try {
            cityService.zones();
        } catch (Exception e) {
            // Ignored if already initialized
        }
        try {
            policyService.all();
        } catch (Exception e) {
            // Ignored if already initialized
        }
    }
}
