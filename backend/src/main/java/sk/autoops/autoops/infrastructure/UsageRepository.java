package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.UsageReservationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UsageRepository {
    private final ConcurrentHashMap<UUID, UsageReservationRecord> records = new ConcurrentHashMap<>();

    public UsageReservationRecord save(UsageReservationRecord record) {
        if (record.id == null) {
            record.id = UUID.randomUUID();
        }
        records.put(record.id, record);
        return record;
    }

    public List<UsageReservationRecord> findAll() {
        return new ArrayList<>(records.values());
    }
}
