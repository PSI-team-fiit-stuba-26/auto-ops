package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.PriceChangeLog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PriceChangeLogRepository {
    private final ConcurrentHashMap<UUID, PriceChangeLog> priceChangeLogs = new ConcurrentHashMap<>();

    public PriceChangeLog save(PriceChangeLog log) {
        if (log.id == null) {
            log.id = UUID.randomUUID();
        }
        priceChangeLogs.put(log.id, log);
        return log;
    }

    public List<PriceChangeLog> findAll() {
        return new ArrayList<>(priceChangeLogs.values());
    }
}
