package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;
import lk.smartcity360.entity.Streetlight;
import lk.smartcity360.service.FlyweightReport;
import lk.smartcity360.service.StreetlightService;

@Named
@RequestScoped
public class StreetlightBean {

    @EJB
    private StreetlightService service;

    private Streetlight light = new Streetlight();
    private FlyweightReport report;

    public Streetlight getLight() { return light; }

    public List<Streetlight> getLights() { return service.all(); }

    public FlyweightReport getReport() { return report; }

    public void save() {
        try {
            service.save(light);
            FacesMessages.info("Light '" + light.getCode() + "' registered");
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
        report = service.flyweightReport();
    }
}
