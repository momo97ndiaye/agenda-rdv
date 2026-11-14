package sn.mndiaye.agenda.appointment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByOwnerIdOrderByStartsAt(Long ownerId);

    Optional<Appointment> findByIdAndOwnerId(Long id, Long ownerId);

    /** Vrai si un autre rendez-vous du même utilisateur chevauche le créneau [startsAt, endsAt). */
    @Query("""
            select count(a) > 0 from Appointment a
            where a.ownerId = :ownerId
              and a.id <> :excludedId
              and a.startsAt < :endsAt
              and a.endsAt > :startsAt
            """)
    boolean overlaps(@Param("ownerId") Long ownerId,
                     @Param("startsAt") Instant startsAt,
                     @Param("endsAt") Instant endsAt,
                     @Param("excludedId") Long excludedId);
}
