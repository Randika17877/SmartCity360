package lk.smartcity360.web;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.*;
import lk.smartcity360.entity.CityPolicy;
import lk.smartcity360.entity.PolicyHistory;
import lk.smartcity360.service.PolicyService;

@Named
@RequestScoped
public class PolicyBean implements Serializable {

    public static class PolicyRow implements Serializable {
        private final int index;
        private final Long id;
        private final String name;
        private final String expression;
        private final String parsedTree;

        public PolicyRow(int index, Long id, String name, String expression, String parsedTree) {
            this.index = index;
            this.id = id;
            this.name = name;
            this.expression = expression;
            this.parsedTree = parsedTree;
        }

        public int getIndex() { return index; }
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getExpression() { return expression; }
        public String getParsedTree() { return parsedTree; }
    }

    @EJB
    private PolicyService service;

    private Long selectedCheckPolicyId;
    private String checkValues = "hour=23, soundLevel=62";
    private String checkResult;
    private Boolean checkMatched;

    private Long selectedUpdatePolicyId;
    private String newRuleText;

    private String customRule = "soundLevel <= 55 AND hour < 22";
    private String customValues = "soundLevel=50, hour=14";
    private String customResult;
    private Boolean customMatched;
    private String customAst;

    // Backwards compatibility
    private CityPolicy policy = new CityPolicy();
    private double noiseDb = 65;
    private double hour = 22;
    private String testResult;

    @PostConstruct
    public void init() {
        List<CityPolicy> list = service.all();
        if (!list.isEmpty()) {
            selectedCheckPolicyId = list.get(0).getId();
            selectedUpdatePolicyId = list.get(0).getId();
            newRuleText = list.get(0).getExpression();
        }
    }

    public List<PolicyRow> getPolicyRows() {
        List<CityPolicy> policies = service.all();
        List<PolicyRow> rows = new ArrayList<>();
        int idx = 1;
        for (CityPolicy p : policies) {
            String tree = service.parseTree(p.getExpression());
            rows.add(new PolicyRow(idx++, p.getId(), p.getName(), p.getExpression(), tree));
        }
        return rows;
    }

    public List<CityPolicy> getPolicies() {
        return service.all();
    }

    public List<PolicyHistory> getHistory() {
        return service.getHistory();
    }

    public Long getSelectedCheckPolicyId() { return selectedCheckPolicyId; }
    public void setSelectedCheckPolicyId(Long selectedCheckPolicyId) { this.selectedCheckPolicyId = selectedCheckPolicyId; }

    public String getCheckValues() { return checkValues; }
    public void setCheckValues(String checkValues) { this.checkValues = checkValues; }

    public String getCheckResult() { return checkResult; }
    public Boolean getCheckMatched() { return checkMatched; }

    public Long getSelectedUpdatePolicyId() { return selectedUpdatePolicyId; }
    public void setSelectedUpdatePolicyId(Long selectedUpdatePolicyId) {
        this.selectedUpdatePolicyId = selectedUpdatePolicyId;
        CityPolicy p = service.findById(selectedUpdatePolicyId);
        if (p != null) {
            this.newRuleText = p.getExpression();
        }
    }

    public String getNewRuleText() { return newRuleText; }
    public void setNewRuleText(String newRuleText) { this.newRuleText = newRuleText; }

    public String getCustomRule() { return customRule; }
    public void setCustomRule(String customRule) { this.customRule = customRule; }

    public String getCustomValues() { return customValues; }
    public void setCustomValues(String customValues) { this.customValues = customValues; }

    public String getCustomResult() { return customResult; }
    public Boolean getCustomMatched() { return customMatched; }
    public String getCustomAst() { return customAst; }

    // Backward-compatible properties
    public CityPolicy getPolicy() { return policy; }
    public double getNoiseDb() { return noiseDb; }
    public void setNoiseDb(double noiseDb) { this.noiseDb = noiseDb; }
    public double getHour() { return hour; }
    public void setHour(double hour) { this.hour = hour; }
    public String getTestResult() { return testResult; }

    public void checkEvent() {
        try {
            CityPolicy p = service.findById(selectedCheckPolicyId);
            if (p == null) {
                FacesMessages.error("Please select a valid municipal ordinance");
                return;
            }
            Map<String, Double> values = parseValues(checkValues);
            boolean matched = service.evaluate(p.getExpression(), values);
            checkMatched = matched;
            checkResult = matched ? "COMPLIANT (Ordinance Satisfied)" : "VIOLATION DETECTED (Alert Triggered)";
        } catch (RuntimeException e) {
            checkMatched = null;
            checkResult = "Evaluation Error: " + e.getMessage();
            FacesMessages.error(e);
        }
    }

    public void updateRule() {
        try {
            if (selectedUpdatePolicyId == null || newRuleText == null || newRuleText.isBlank()) {
                FacesMessages.error("Please select an ordinance and specify the updated DSL syntax");
                return;
            }
            service.updateRule(selectedUpdatePolicyId, newRuleText.trim());
            FacesMessages.info("Ordinance updated live in MySQL (zero server downtime required)");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void resetRules() {
        try {
            service.resetDefaults();
            init();
            checkResult = null;
            checkMatched = null;
            customResult = null;
            FacesMessages.info("Municipal ordinances restored to official SmartCity360 standards");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void evaluateCustom() {
        try {
            customAst = service.parseTree(customRule);
            Map<String, Double> values = parseValues(customValues);
            boolean matched = service.evaluate(customRule, values);
            customMatched = matched;
            customResult = matched ? "TRUE (Satisfied)" : "FALSE (Violated)";
        } catch (RuntimeException e) {
            customMatched = null;
            customResult = "Syntax Error: " + e.getMessage();
            FacesMessages.error(e);
        }
    }

    private Map<String, Double> parseValues(String text) {
        Map<String, Double> map = new HashMap<>();
        if (text == null || text.isBlank()) return map;
        String[] parts = text.split("[,;\\s]+");
        for (String part : parts) {
            if (part.contains("=")) {
                String[] kv = part.split("=", 2);
                try {
                    String key = kv[0].trim();
                    double val = Double.parseDouble(kv[1].trim());
                    map.put(key, val);
                    if ("soundLevel".equalsIgnoreCase(key) || "noiseDb".equalsIgnoreCase(key) || "noise".equalsIgnoreCase(key)) {
                        map.put("soundLevel", val);
                        map.put("noiseDb", val);
                        map.put("noise", val);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid telemetry reading: '" + part + "'");
                }
            }
        }
        return map;
    }

    public void save() {
        try {
            service.save(policy);
            FacesMessages.info("Ordinance '" + policy.getName() + "' saved");
            policy = new CityPolicy();
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void evaluate() {
        try {
            boolean matched = service.evaluate(policy.getExpression(), Map.of("noiseDb", noiseDb, "hour", hour));
            testResult = (matched ? "Matched" : "Not matched") + " for noiseDb=" + noiseDb + ", hour=" + hour;
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }
}
