package lk.smartcity360.web;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import lk.smartcity360.entity.Streetlight;
import lk.smartcity360.service.FlyweightReport;
import lk.smartcity360.service.StreetlightService;

@Named
@RequestScoped
public class StreetlightBean implements Serializable {

    @EJB
    private StreetlightService service;

    private int hour = 20;

    private int simCount = 100000;
    private int simMainRoadPercent = 10;
    private int simFaultyCount = 25;
    private int simHour = 20;

    private Streetlight light = new Streetlight();
    private FlyweightReport report;

    @PostConstruct
    public void init() {
        try {
            report = service.loadFromMysql(hour);
        } catch (Exception e) {
            report = service.simulate(10004, 10, 25, 20);
        }
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getSimCount() { return simCount; }
    public void setSimCount(int simCount) { this.simCount = simCount; }

    public int getSimMainRoadPercent() { return simMainRoadPercent; }
    public void setSimMainRoadPercent(int simMainRoadPercent) { this.simMainRoadPercent = simMainRoadPercent; }

    public int getSimFaultyCount() { return simFaultyCount; }
    public void setSimFaultyCount(int simFaultyCount) { this.simFaultyCount = simFaultyCount; }

    public int getSimHour() { return simHour; }
    public void setSimHour(int simHour) { this.simHour = simHour; }

    public Streetlight getLight() { return light; }

    public List<Streetlight> getLights() { return service.all(); }

    public FlyweightReport getReport() { return report; }

    public void loadFromMysql() {
        try {
            report = service.loadFromMysql(hour);
            FacesMessages.info("Synchronized " + report.getTotalLightsFormatted() + " managed streetlights from MySQL database");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void simulate() {
        try {
            report = service.simulate(simCount, simMainRoadPercent, simFaultyCount, simHour);
            FacesMessages.info("Simulated " + report.getTotalLightsFormatted() + " virtual streetlights in-memory via Flyweight pool");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void save() {
        try {
            service.save(light);
            FacesMessages.info("Luminaire '" + light.getCode() + "' successfully registered in municipal inventory");
            light = new Streetlight();
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void toggle(Long id, boolean active) {
        try {
            service.setActive(id, active);
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void showFlyweight() {
        loadFromMysql();
    }
}
