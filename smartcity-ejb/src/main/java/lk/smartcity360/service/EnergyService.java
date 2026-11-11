package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import javax.crypto.SecretKey;
import lk.smartcity360.entity.EnergyReading;
import lk.smartcity360.pattern.decorator.AuthenticationDecorator;
import lk.smartcity360.pattern.decorator.BasicEnergyStream;
import lk.smartcity360.pattern.decorator.CompressionDecorator;
import lk.smartcity360.pattern.decorator.DataStream;
import lk.smartcity360.pattern.decorator.EncryptionDecorator;
import lk.smartcity360.pattern.decorator.StreamKey;

/** Stores energy readings and protects data streams with composable wrappers (Decorator pattern). */
@Stateless
public class EnergyService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    public EnergyReading record(EnergyReading reading) {
        em.persist(reading);
        return reading;
    }

    public List<EnergyReading> all() {
        return em.createQuery("SELECT e FROM EnergyReading e ORDER BY e.recordedAt DESC, e.id DESC", EnergyReading.class)
                .getResultList();
    }

    public double totalKilowattHours() {
        return em.createQuery("SELECT COALESCE(SUM(e.kilowattHours), 0.0) FROM EnergyReading e", Double.class)
                .getSingleResult();
    }

    /** Signs, compresses and encrypts a payload, in that order. */
    public String protect(String payload) {
        SecretKey key = StreamKey.resolve();
        DataStream stream = new EncryptionDecorator(
                new CompressionDecorator(
                        new AuthenticationDecorator(new BasicEnergyStream(), key)),
                key);
        return stream.process(payload);
    }
}
