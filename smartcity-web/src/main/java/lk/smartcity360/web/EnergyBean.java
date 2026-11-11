package lk.smartcity360.web;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import lk.smartcity360.entity.EnergyReading;
import lk.smartcity360.service.DecoratorReport;
import lk.smartcity360.service.EnergyService;

@Named
@RequestScoped
public class EnergyBean implements Serializable {

    @EJB
    private EnergyService service;

    private String selectedMeterCode = "EM-1001";
    private String layer1 = "AUTHENTICATION";
    private String layer2 = "COMPRESSION";
    private String layer3 = "ENCRYPTION";
    private boolean attackerChangesData = false;

    private DecoratorReport report;

    // Backwards compatibility
    private EnergyReading reading = new EnergyReading("MTR-001", 12.5);
    private String protectedOutput;

    @PostConstruct
    public void init() {
        sendData();
    }

    public String getSelectedMeterCode() { return selectedMeterCode; }
    public void setSelectedMeterCode(String selectedMeterCode) { this.selectedMeterCode = selectedMeterCode; }

    public String getLayer1() { return layer1; }
    public void setLayer1(String layer1) { this.layer1 = layer1; }

    public String getLayer2() { return layer2; }
    public void setLayer2(String layer2) { this.layer2 = layer2; }

    public String getLayer3() { return layer3; }
    public void setLayer3(String layer3) { this.layer3 = layer3; }

    public boolean isAttackerChangesData() { return attackerChangesData; }
    public void setAttackerChangesData(boolean attackerChangesData) { this.attackerChangesData = attackerChangesData; }

    public DecoratorReport getReport() { return report; }

    public EnergyReading getReading() { return reading; }
    public List<EnergyReading> getReadings() { return service.all(); }
    public String getProtectedOutput() { return protectedOutput; }

    public void sendData() {
        try {
            report = service.simulatePipeline(selectedMeterCode, layer1, layer2, layer3, attackerChangesData);
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void record() {
        try {
            service.record(reading);
            FacesMessages.info("Reading recorded");
            reading = new EnergyReading("MTR-001", 12.5);
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void protect() {
        protectedOutput = service.protect("meter=" + reading.getMeterCode() + ",kwh=" + reading.getKilowattHours());
    }
}
