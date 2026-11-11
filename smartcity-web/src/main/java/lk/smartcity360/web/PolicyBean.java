package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;
import lk.smartcity360.entity.CityPolicy;
import lk.smartcity360.service.PolicyService;

@Named
@RequestScoped
public class PolicyBean {

    @EJB
    private PolicyService service;

    private CityPolicy policy = new CityPolicy();
    private double noiseDb = 65;
    private double hour = 22;
    private String testResult;

    public CityPolicy getPolicy() { return policy; }

    public List<CityPolicy> getPolicies() { return service.all(); }

    public double getNoiseDb() { return noiseDb; }
    public void setNoiseDb(double noiseDb) { this.noiseDb = noiseDb; }

    public double getHour() { return hour; }
    public void setHour(double hour) { this.hour = hour; }

    public String getTestResult() { return testResult; }

    public void save() {
        try {
            service.save(policy);
            FacesMessages.info("Policy '" + policy.getName() + "' saved");
            policy = new CityPolicy();
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    /** Evaluates the typed expression against the sample sensor values. */
    public void evaluate() {
        try {
            boolean matched = service.evaluate(policy.getExpression(), Map.of("noiseDb", noiseDb, "hour", hour));
            testResult = (matched ? "Matched" : "Not matched") + " for noiseDb=" + noiseDb + ", hour=" + hour;
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }
}
