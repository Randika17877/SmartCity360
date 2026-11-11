package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A named operational policy whose rule is written in the policy mini-language. */
@Entity
@Table(name = "city_policies")
public class CityPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Policy name is required")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Expression is required")
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String expression;

    @Column(nullable = false)
    private boolean enabled = true;

    public CityPolicy() {
    }

    public CityPolicy(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
