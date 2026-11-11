package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import lk.smartcity360.service.CityService;
import lk.smartcity360.service.ComplaintService;
import lk.smartcity360.service.EnergyService;
import lk.smartcity360.service.StreetlightService;

@Named
@RequestScoped
public class DashboardBean {

    @EJB
    private CityService cityService;
    @EJB
    private ComplaintService complaintService;
    @EJB
    private StreetlightService streetlightService;
    @EJB
    private EnergyService energyService;

    public long getZoneCount() { return cityService.zoneCount(); }

    public long getPendingComplaints() { return complaintService.pendingCount(); }

    public long getActiveStreetlights() { return streetlightService.activeCount(); }

    public String getTotalEnergy() { return String.format("%,.1f", energyService.totalKilowattHours()); }
}
