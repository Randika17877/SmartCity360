package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "street_light")
public class StreetLightRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private StreetLightType type;

    @Column(nullable = false)
    private boolean faulty = false;

    public StreetLightRecord() {}

    public StreetLightRecord(String code, String location, StreetLightType type, boolean faulty) {
        this.code = code;
        this.location = location;
        this.type = type;
        this.faulty = faulty;
    }

    public Long getId() { return id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public StreetLightType getType() { return type; }
    public void setType(StreetLightType type) { this.type = type; }

    public boolean isFaulty() { return faulty; }
    public void setFaulty(boolean faulty) { this.faulty = faulty; }
}
