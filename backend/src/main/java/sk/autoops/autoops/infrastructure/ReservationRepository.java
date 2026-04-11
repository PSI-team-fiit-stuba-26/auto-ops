package sk.autoops.autoops.infrastructure;

import org.springframework.stereotype.Repository;
import sk.autoops.autoops.domain.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ReservationRepository {
    private final ConcurrentHashMap<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    public Reservation save(Reservation reservation) {
        if (reservation.id == null) {
            reservation.id = UUID.randomUUID();
        }
        reservations.put(reservation.id, reservation);
        return reservation;
    }

    public Optional<Reservation> findOpenReservation(UUID repairJobId, UUID itemId) {
        return reservations.values().stream()
                .filter(reservation -> repairJobId.equals(reservation.repairJobId))
                .filter(reservation -> itemId.equals(reservation.itemId))
                .filter(reservation -> !reservation.consumed)
                .findFirst();
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }
}
