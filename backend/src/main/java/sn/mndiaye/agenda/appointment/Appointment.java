package sn.mndiaye.agenda.appointment;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(length = 1000)
    private String notes;

    protected Appointment() {
    }

    public Appointment(Long ownerId, String title, Instant startsAt, Instant endsAt, String notes) {
        this.ownerId = ownerId;
        update(title, startsAt, endsAt, notes);
    }

    public void update(String title, Instant startsAt, Instant endsAt, String notes) {
        this.title = title;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Long getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public String getNotes() { return notes; }
}
