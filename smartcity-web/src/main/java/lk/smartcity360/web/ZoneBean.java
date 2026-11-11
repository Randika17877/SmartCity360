package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;
import lk.smartcity360.entity.Zone;
import lk.smartcity360.entity.ZoneType;
import lk.smartcity360.service.CityService;

@Named
@RequestScoped
public class ZoneBean {

    @EJB
    private CityService service;

    private Zone zone = new Zone();
    private Long targetZoneId;
    private String command = "SWITCH_TO_ECO_MODE";
    private List<String> broadcastLog;

    public Zone getZone() { return zone; }

    public ZoneType[] getTypes() { return ZoneType.values(); }

    public List<Zone> getZones() { return service.zones(); }

    public Long getTargetZoneId() { return targetZoneId; }
    public void setTargetZoneId(Long targetZoneId) { this.targetZoneId = targetZoneId; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public List<String> getBroadcastLog() { return broadcastLog; }

    public void save() {
        try {
            service.saveZone(zone);
            FacesMessages.info("Zone '" + zone.getName() + "' saved");
            zone = new Zone();
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void broadcast() {
        try {
            broadcastLog = service.broadcast(targetZoneId, command);
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }
}
