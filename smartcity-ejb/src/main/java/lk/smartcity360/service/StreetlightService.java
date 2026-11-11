package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lk.smartcity360.entity.Streetlight;
import lk.smartcity360.pattern.flyweight.LightType;
import lk.smartcity360.pattern.flyweight.LightTypeFactory;
import lk.smartcity360.pattern.flyweight.StreetLightContext;

/** Manages streetlights and shows how their shared type data is pooled (Flyweight pattern). */
@Stateless
public class StreetlightService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    public Streetlight save(Streetlight light) {
        if (light.getId() == null) {
            long existing = em.createQuery("SELECT COUNT(s) FROM Streetlight s WHERE s.code = :code", Long.class)
                    .setParameter("code", light.getCode())
                    .getSingleResult();
            if (existing > 0) {
                throw new BusinessRuleException("Light code " + light.getCode() + " is already registered");
            }
            em.persist(light);
            return light;
        }
        return em.merge(light);
    }

    public Streetlight setActive(Long id, boolean active) {
        Streetlight light = em.find(Streetlight.class, id);
        if (light == null) {
            throw new BusinessRuleException("Streetlight " + id + " does not exist");
        }
        light.setActive(active);
        return light;
    }

    public List<Streetlight> all() {
        return em.createQuery("SELECT s FROM Streetlight s ORDER BY s.id", Streetlight.class).getResultList();
    }

    public long activeCount() {
        return em.createQuery("SELECT COUNT(s) FROM Streetlight s WHERE s.active = true", Long.class)
                .getSingleResult();
    }

    /**
     * Builds a context object for every stored light, all drawing their model data from the
     * shared pool, and reports how many distinct {@link LightType} objects were needed.
     */
    public FlyweightReport flyweightReport() {
        List<Streetlight> lights = all();
        Set<LightType> distinct = new HashSet<>();
        List<String> lines = lights.stream().map(light -> {
            LightType type = LightTypeFactory.get(light.getModel(), light.getWattage(), light.getColour());
            distinct.add(type);
            return light.getCode() + ": " + new StreetLightContext(light.getLocation(), type, light.isActive()).display();
        }).toList();
        return new FlyweightReport(lines, lights.size(), distinct.size());
    }
}
