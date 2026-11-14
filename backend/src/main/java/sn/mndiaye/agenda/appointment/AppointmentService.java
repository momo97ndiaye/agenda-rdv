package sn.mndiaye.agenda.appointment;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {

    private static final long NONE = -1L;

    private final AppointmentRepository appointments;

    public AppointmentService(AppointmentRepository appointments) {
        this.appointments = appointments;
    }

    @Transactional(readOnly = true)
    public List<Appointment> list(Long ownerId) {
        return appointments.findByOwnerIdOrderByStartsAt(ownerId);
    }

    @Transactional
    public Appointment create(Long ownerId, String title, Instant startsAt, Instant endsAt, String notes) {
        checkSlot(ownerId, startsAt, endsAt, NONE);
        return appointments.save(new Appointment(ownerId, title, startsAt, endsAt, notes));
    }

    @Transactional
    public Appointment update(Long ownerId, Long id, String title, Instant startsAt, Instant endsAt, String notes) {
        Appointment appointment = find(ownerId, id);
        checkSlot(ownerId, startsAt, endsAt, id);
        appointment.update(title, startsAt, endsAt, notes);
        return appointment;
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        appointments.delete(find(ownerId, id));
    }

    /** Un utilisateur ne voit que ses rendez-vous : ceux des autres renvoient 404. */
    private Appointment find(Long ownerId, Long id) {
        return appointments.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rendez-vous introuvable"));
    }

    private void checkSlot(Long ownerId, Instant startsAt, Instant endsAt, Long excludedId) {
        if (!endsAt.isAfter(startsAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin doit être après le début");
        }
        if (appointments.overlaps(ownerId, startsAt, endsAt, excludedId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce créneau chevauche un autre rendez-vous");
        }
    }
}
