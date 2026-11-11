package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import lk.smartcity360.entity.Zone;
import lk.smartcity360.entity.ZoneType;
import lk.smartcity360.service.CityService;
import lk.smartcity360.service.ZoneTreeItem;

@Named("zoneBean")
@RequestScoped
public class ZoneBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CityService service;

    // Power switching
    private Long targetZoneId;

    // Add building form
    private Long selectedStreetId;
    private String newBuildingName;
    private Double newBuildingUsage = 75.0;

    // Legacy form support
    private Zone zone = new Zone();
    private String command = "SWITCH_TO_ECO_MODE";
    private List<String> broadcastLog;

    public Long getTargetZoneId() {
        if (targetZoneId == null) {
            List<Zone> all = service.allSwitchableZones();
            if (!all.isEmpty()) {
                targetZoneId = all.get(0).getId();
            }
        }
        return targetZoneId;
    }

    public void setTargetZoneId(Long targetZoneId) {
        this.targetZoneId = targetZoneId;
    }

    public Long getSelectedStreetId() {
        if (selectedStreetId == null) {
            List<Zone> streets = service.streets();
            if (!streets.isEmpty()) {
                selectedStreetId = streets.get(0).getId();
            }
        }
        return selectedStreetId;
    }

    public void setSelectedStreetId(Long selectedStreetId) {
        this.selectedStreetId = selectedStreetId;
    }

    public String getNewBuildingName() { return newBuildingName; }
    public void setNewBuildingName(String newBuildingName) { this.newBuildingName = newBuildingName; }

    public Double getNewBuildingUsage() { return newBuildingUsage; }
    public void setNewBuildingUsage(Double newBuildingUsage) { this.newBuildingUsage = newBuildingUsage; }

    public List<ZoneTreeItem> getTreeItems() {
        return service.getTreeItems();
    }

    public String getTotalUsageFormatted() {
        return String.format(Locale.US, "%.2f kWh", service.getTotalUsage());
    }

    public List<Zone> getStreets() {
        return service.streets();
    }

    public List<Zone> getSwitchableZones() {
        return service.allSwitchableZones();
    }

    public void shutdownPower() {
        try {
            service.setPower(targetZoneId, false);
            FacesMessages.info("Power shut down successfully for the selected zone and its descendants.");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void restorePower() {
        try {
            service.setPower(targetZoneId, true);
            FacesMessages.info("Power restored successfully for the selected zone and its descendants.");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void addBuilding() {
        try {
            double usage = newBuildingUsage != null ? newBuildingUsage : 0.0;
            Zone b = service.addBuilding(selectedStreetId, newBuildingName, usage);
            FacesMessages.info("Building '" + b.getName() + "' added successfully with usage " + usage + " kWh.");
            newBuildingName = null;
            newBuildingUsage = 75.0;
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void resetCity() {
        try {
            service.resetCity();
            FacesMessages.info("City hierarchy reset to standard initial topology.");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    // Legacy getters / methods
    public Zone getZone() { return zone; }
    public ZoneType[] getTypes() { return ZoneType.values(); }
    public List<Zone> getZones() { return service.zones(); }
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
