package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A physical streetlight. {@code model}, {@code wattage} and {@code colour} are the shared
 * (intrinsic) state that the Flyweight pattern pools; {@code code}, {@code location} and
 * {@code active} are per-light (extrinsic) state.
 */
@Entity
@Table(name = "streetlights")
public class Streetlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Light code is required")
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @NotBlank(message = "Location is required")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String location;

    @NotBlank(message = "Model is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String model = "LED-Standard";

    @Min(value = 1, message = "Wattage must be at least 1")
    @Max(value = 1000, message = "Wattage must be at most 1000")
    @Column(nullable = false)
    private int wattage = 60;

    @NotBlank(message = "Colour is required")
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String colour = "white";

    @Column(nullable = false)
    private boolean active = true;

    public Streetlight() {
    }

    public Streetlight(String code, String location) {
        this.code = code;
        this.location = location;
    }

    public Long getId() { return id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getWattage() { return wattage; }
    public void setWattage(int wattage) { this.wattage = wattage; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
