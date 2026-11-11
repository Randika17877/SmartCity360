package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import lk.smartcity360.entity.CityPolicy;
import lk.smartcity360.pattern.interpreter.PolicyContext;
import lk.smartcity360.pattern.interpreter.PolicyParser;

/** Stores city policies and evaluates their rules (Interpreter pattern). */
@Stateless
public class PolicyService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

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
        return em.createQuery("SELECT p FROM CityPolicy p ORDER BY p.id", CityPolicy.class).getResultList();
    }

    /** @throws BusinessRuleException if the expression is invalid or refers to a missing variable */
    public boolean evaluate(String expression, Map<String, Double> values) {
        try {
            return new PolicyParser().parse(expression).interpret(new PolicyContext(values));
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage(), e);
        }
    }

    private void check(String expression) {
        try {
            new PolicyParser().parse(expression);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid policy expression: " + e.getMessage(), e);
        }
    }
}
