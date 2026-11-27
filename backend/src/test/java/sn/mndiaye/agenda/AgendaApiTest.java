package sn.mndiaye.agenda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Tests de bout en bout de l'API, sur un vrai PostgreSQL démarré dans Docker (mêmes migrations Flyway qu'en production). */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AgendaApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void registerThenLoginReturnsToken() throws Exception {
        String email = newEmail();
        register(email);

        mvc.perform(json(post("/api/auth/login"), "{\"email\":\"%s\",\"password\":\"motdepasse123\"}".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String email = newEmail();
        register(email);

        mvc.perform(json(post("/api/auth/register"), registerBody(email)))
                .andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        String email = newEmail();
        register(email);

        mvc.perform(json(post("/api/auth/login"), "{\"email\":\"%s\",\"password\":\"mauvais-mdp\"}".formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void appointmentsRequireAToken() throws Exception {
        mvc.perform(get("/api/appointments")).andExpect(status().isUnauthorized());
    }

    @Test
    void createListUpdateAndDeleteAppointment() throws Exception {
        String token = tokenFor(newEmail());

        String created = mvc.perform(auth(json(post("/api/appointments"),
                        slot("Consultation", "2026-12-01T09:00:00Z", "2026-12-01T09:30:00Z")), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Consultation"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mvc.perform(auth(get("/api/appointments"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(auth(json(put("/api/appointments/" + id),
                        slot("Consultation (décalée)", "2026-12-01T10:00:00Z", "2026-12-01T10:30:00Z")), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startsAt").value("2026-12-01T10:00:00Z"));

        mvc.perform(auth(delete("/api/appointments/" + id), token))
                .andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/appointments"), token))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void overlappingSlotIsRejected() throws Exception {
        String token = tokenFor(newEmail());
        mvc.perform(auth(json(post("/api/appointments"),
                        slot("A", "2026-12-02T09:00:00Z", "2026-12-02T10:00:00Z")), token))
                .andExpect(status().isCreated());

        mvc.perform(auth(json(post("/api/appointments"),
                        slot("B", "2026-12-02T09:30:00Z", "2026-12-02T10:30:00Z")), token))
                .andExpect(status().isConflict());

        // Un créneau qui commence exactement à la fin du précédent est accepté.
        mvc.perform(auth(json(post("/api/appointments"),
                        slot("C", "2026-12-02T10:00:00Z", "2026-12-02T10:30:00Z")), token))
                .andExpect(status().isCreated());
    }

    @Test
    void endBeforeStartIsRejected() throws Exception {
        String token = tokenFor(newEmail());
        mvc.perform(auth(json(post("/api/appointments"),
                        slot("X", "2026-12-03T10:00:00Z", "2026-12-03T09:00:00Z")), token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userCannotSeeAnotherUsersAppointment() throws Exception {
        String alice = tokenFor(newEmail());
        String bob = tokenFor(newEmail());

        String created = mvc.perform(auth(json(post("/api/appointments"),
                        slot("Privé", "2026-12-04T09:00:00Z", "2026-12-04T09:30:00Z")), alice))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mvc.perform(auth(delete("/api/appointments/" + id), bob)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/appointments"), bob)).andExpect(jsonPath("$.length()").value(0));
    }

    private static String newEmail() {
        return "user-" + UUID.randomUUID() + "@exemple.sn";
    }

    private static String registerBody(String email) {
        return "{\"email\":\"%s\",\"password\":\"motdepasse123\",\"fullName\":\"Awa Diop\"}".formatted(email);
    }

    private static String slot(String title, String start, String end) {
        return "{\"title\":\"%s\",\"startsAt\":\"%s\",\"endsAt\":\"%s\"}".formatted(title, start, end);
    }

    private void register(String email) throws Exception {
        mvc.perform(json(post("/api/auth/register"), registerBody(email))).andExpect(status().isCreated());
    }

    private String tokenFor(String email) throws Exception {
        register(email);
        String body = mvc.perform(json(post("/api/auth/login"),
                        "{\"email\":\"%s\",\"password\":\"motdepasse123\"}".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
}
