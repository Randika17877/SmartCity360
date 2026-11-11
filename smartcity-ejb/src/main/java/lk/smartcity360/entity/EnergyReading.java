package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** A single smart-meter reading. */
@Entity
@Table(name = "energy_readings")
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Meter code is required")
    @Size(max = 80)
    @Column(name = "meter_code", nullable = false, length = 80)
    private String meterCode;

    @DecimalMin(value = "0.0", message = "kWh cannot be negative")
    @Column(name = "kilowatt_hours", nullable = false)
    private double kilowattHours;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    public EnergyReading() {
    }

    public EnergyReading(String meterCode, double kilowattHours) {
        this.meterCode = meterCode;
        this.kilowattHours = kilowattHours;
    }

    public Long getId() { return id; }

    public String getMeterCode() { return meterCode; }
    public void setMeterCode(String meterCode) { this.meterCode = meterCode; }

    public double getKilowattHours() { return kilowattHours; }
    public void setKilowattHours(double kilowattHours) { this.kilowattHours = kilowattHours; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
}
