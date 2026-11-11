package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "street_light_type")
public class StreetLightType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private int wattage;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String colour;

    @Column(name = "dimming_profile", nullable = false, length = 255)
    private String dimmingProfile = "";

    public StreetLightType() {}

    public StreetLightType(String code, String model, int wattage, String colour, String dimmingProfile) {
        this.code = code;
        this.model = model;
        this.wattage = wattage;
        this.colour = colour;
        this.dimmingProfile = dimmingProfile;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getWattage() { return wattage; }
    public void setWattage(int wattage) { this.wattage = wattage; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public String getDimmingProfile() { return dimmingProfile; }
    public void setDimmingProfile(String dimmingProfile) { this.dimmingProfile = dimmingProfile; }
}
