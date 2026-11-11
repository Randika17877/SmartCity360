package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;
import lk.smartcity360.entity.EnergyReading;
import lk.smartcity360.service.EnergyService;

@Named
@RequestScoped
public class EnergyBean {

    @EJB
    private EnergyService service;

    private EnergyReading reading = new EnergyReading("MTR-001", 12.5);
    private String protectedOutput;

    public EnergyReading getReading() { return reading; }

    public List<EnergyReading> getReadings() { return service.all(); }

    public String getProtectedOutput() { return protectedOutput; }

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
