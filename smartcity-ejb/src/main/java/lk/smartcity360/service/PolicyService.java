package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import lk.smartcity360.entity.CityPolicy;
import lk.smartcity360.entity.PolicyHistory;
import lk.smartcity360.pattern.interpreter.PolicyContext;
import lk.smartcity360.pattern.interpreter.PolicyParser;

/**
 * Stores city policies, tracks rule modification audit logs, and evaluates expressions (Interpreter pattern).
 */
@Stateless
public class PolicyService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    private final PolicyParser parser = new PolicyParser();

    /** Saves a policy; its expression must parse. */
    public CityPolicy save(CityPolicy policy) {
        check(policy.getExpression());
        if (policy.getId() == null) {
            em.persist(policy);
            return policy;
        }
        return em.merge(policy);
    }

    public List<CityPolicy> all() {
        List<CityPolicy> list = em.createQuery("SELECT p FROM CityPolicy p ORDER BY p.id", CityPolicy.class).getResultList();
        if (list.isEmpty()) {
            resetDefaults();
            list = em.createQuery("SELECT p FROM CityPolicy p ORDER BY p.id", CityPolicy.class).getResultList();
        }
        return list;
    }

    public CityPolicy findById(Long id) {
        return em.find(CityPolicy.class, id);
    }

    /** Updates an active policy rule without restarting the system, recording the change in policy_history. */
    public CityPolicy updateRule(Long id, String newRule) {
        check(newRule);
        CityPolicy policy = em.find(CityPolicy.class, id);
        if (policy == null) {
            throw new BusinessRuleException("Policy with ID " + id + " does not exist");
        }
        String oldRule = policy.getExpression();
        policy.setExpression(newRule.trim());
        em.merge(policy);

        PolicyHistory history = new PolicyHistory(policy.getName(), oldRule, newRule.trim());
        em.persist(history);
        return policy;
    }

    public List<PolicyHistory> getHistory() {
        try {
            return em.createQuery("SELECT h FROM PolicyHistory h ORDER BY h.changedAt DESC", PolicyHistory.class).getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Resets the policies to the 3 standard municipal rules. */
    public void resetDefaults() {
        em.createQuery("DELETE FROM CityPolicy p").executeUpdate();
        save(new CityPolicy("Night Quiet Zone Regulation",
                "(hour >= 23 OR hour < 6) AND soundLevel <= 45 OR ((hour >= 6 AND hour < 23) AND soundLevel <= 65)"));
        save(new CityPolicy("Arterial Commercial Access",
                "transitPass == 1 OR hour >= 8 AND hour < 19"));
        save(new CityPolicy("Low Emission Transit Corridor",
                "emissionTier >= 4 OR NOT (hour >= 7 AND hour < 10)"));
    }

    /** Returns the formatted AST tree string for display in the table. */
    public String parseTree(String expression) {
        try {
            return parser.parse(expression).toTreeString();
        } catch (Exception e) {
            return "(parse error: " + e.getMessage() + ")";
        }
    }

    /** @throws BusinessRuleException if the expression is invalid or refers to a missing variable */
    public boolean evaluate(String expression, Map<String, Double> values) {
        try {
            return parser.parse(expression).interpret(new PolicyContext(values));
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage(), e);
        }
    }

    private void check(String expression) {
        try {
            parser.parse(expression);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid policy expression: " + e.getMessage(), e);
        }
    }
}
