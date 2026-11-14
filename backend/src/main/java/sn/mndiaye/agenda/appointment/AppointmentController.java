package sn.mndiaye.agenda.appointment;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AppointmentResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(userId(jwt)).stream().map(AppointmentResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AppointmentRequest r) {
        return AppointmentResponse.from(
                service.create(userId(jwt), r.title(), r.startsAt(), r.endsAt(), r.notes()));
    }

    @PutMapping("/{id}")
    public AppointmentResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                      @Valid @RequestBody AppointmentRequest r) {
        return AppointmentResponse.from(
                service.update(userId(jwt), id, r.title(), r.startsAt(), r.endsAt(), r.notes()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(userId(jwt), id);
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    public record AppointmentRequest(@NotBlank @Size(max = 200) String title,
                                     @NotNull Instant startsAt,
                                     @NotNull Instant endsAt,
                                     @Size(max = 1000) String notes) {
    }

    public record AppointmentResponse(Long id, String title, Instant startsAt, Instant endsAt, String notes) {
        static AppointmentResponse from(Appointment a) {
            return new AppointmentResponse(a.getId(), a.getTitle(), a.getStartsAt(), a.getEndsAt(), a.getNotes());
        }
    }
}
