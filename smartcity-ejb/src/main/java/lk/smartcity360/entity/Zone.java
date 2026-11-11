package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A node in the city hierarchy (city, district, street or building). */
@Entity
@Table(name = "zones")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ZoneType type = ZoneType.CITY;

    @Column(name = "parent_id")
    private Long parentId;

    public Zone() {
    }

    public Zone(String name, ZoneType type, Long parentId) {
        this.name = name;
        this.type = type;
        this.parentId = parentId;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ZoneType getType() { return type; }
    public void setType(ZoneType type) { this.type = type; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
