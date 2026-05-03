package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.UsageReservationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ReservationRepository {
    private final ConcurrentHashMap<UUID, UsageReservationRecord> reservations = new ConcurrentHashMap<>();

    public void save(UsageReservationRecord record) {
        if (record.id == null) {
            record.id = UUID.randomUUID();
        }
        reservations.put(record.id, record);
    }

    public List<UsageReservationRecord> findAll() {
        return new ArrayList<>(reservations.values());
    }
}
