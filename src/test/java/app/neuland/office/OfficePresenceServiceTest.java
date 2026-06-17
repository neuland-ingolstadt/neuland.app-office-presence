package app.neuland.office;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfficePresenceServiceTest {

    private OfficePresenceService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new OfficePresenceService();
        Field timeoutField = OfficePresenceService.class.getDeclaredField("timeoutHours");
        timeoutField.setAccessible(true);
        timeoutField.set(service, 5);
    }

    @Test
    void checkInAddsUserOnce() {
        service.checkIn("user-1");
        service.checkIn("user-1");
        assertEquals(1, service.count());
    }

    @Test
    void checkOutRemovesUser() {
        service.checkIn("user-1");
        service.checkOut("user-1");
        assertEquals(0, service.count());
        assertTrue(service.expiresAt("user-1").isEmpty());
    }

    @Test
    void expiredEntriesArePurged() throws Exception {
        Field presentField = OfficePresenceService.class.getDeclaredField("present");
        presentField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var present = (java.util.concurrent.ConcurrentHashMap<String, Instant>) presentField.get(service);
        present.put("user-1", Instant.now().minus(Duration.ofMinutes(1)));

        service.purgeExpired();

        assertEquals(0, service.count());
        assertTrue(service.expiresAt("user-1").isEmpty());
    }

    @Test
    void multipleUsersAreCounted() {
        service.checkIn("user-1");
        service.checkIn("user-2");
        assertEquals(2, service.count());
        assertTrue(service.expiresAt("user-1").isPresent());
        assertTrue(service.expiresAt("user-2").isPresent());
    }
}
