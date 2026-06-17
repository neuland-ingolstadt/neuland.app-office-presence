package app.neuland.office;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class OfficePresenceService {

    private final ConcurrentHashMap<String, Instant> present = new ConcurrentHashMap<>();

    @ConfigProperty(name = "office.presence.timeout-hours")
    int timeoutHours;

    public Instant checkIn(String sub) {
        Instant expiresAt = Instant.now().plus(Duration.ofHours(timeoutHours));
        present.put(sub, expiresAt);
        return expiresAt;
    }

    public void checkOut(String sub) {
        present.remove(sub);
    }

    public int count() {
        purgeExpired();
        return present.size();
    }

    public Optional<Instant> expiresAt(String sub) {
        purgeExpired();
        Instant expiresAt = present.get(sub);
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(expiresAt);
    }

    @Scheduled(every = "5m")
    void purgeExpiredScheduled() {
        purgeExpired();
    }

    void purgeExpired() {
        Instant now = Instant.now();
        for (Map.Entry<String, Instant> entry : present.entrySet()) {
            if (entry.getValue().isBefore(now)) {
                present.remove(entry.getKey(), entry.getValue());
            }
        }
    }
}
