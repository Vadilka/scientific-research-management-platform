package pl.san.articlesubmission.user.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.san.articlesubmission.user.RoleName;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void loginReturnsJwtAndAllowsCurrentUserLookup() throws Exception {
        String email = createUser(RoleName.AUTHOR);

        String token = login(email);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roleName").value(RoleName.AUTHOR.name()));
    }

    @Test
    void rejectsProtectedEndpointWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deniesAdminEndpointForAuthorToken() throws Exception {
        String email = createUser(RoleName.AUTHOR);
        String token = login(email);

        mockMvc.perform(get("/api/users/management")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminTokenToOpenUserManagement() throws Exception {
        String email = createUser(RoleName.ADMIN);
        String token = login(email);

        mockMvc.perform(get("/api/users/management")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == '%s')]".formatted(email)).exists());
    }

    @Test
    void rejectsAdminEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/management"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void rejectsAdminRoleChangeForAuthorToken() throws Exception {
        String authorEmail = createUser(RoleName.AUTHOR);
        String targetEmail = createUser(RoleName.REVIEWER);
        Long targetUserId = userRepository.findByEmailIgnoreCase(targetEmail).orElseThrow().getId();
        String authorToken = login(authorEmail);

        mockMvc.perform(patch("/api/users/{userId}/role", targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(authorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "ADMIN"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminToChangeAnotherUserRole() throws Exception {
        String adminEmail = createUser(RoleName.ADMIN);
        String targetEmail = createUser(RoleName.AUTHOR);
        Long targetUserId = userRepository.findByEmailIgnoreCase(targetEmail).orElseThrow().getId();
        String adminToken = login(adminEmail);

        mockMvc.perform(patch("/api/users/{userId}/role", targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "REVIEWER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(targetEmail))
                .andExpect(jsonPath("$.roleName").value(RoleName.REVIEWER.name()));
    }

    @Test
    void rejectsAdminChangingOwnRole() throws Exception {
        String adminEmail = createUser(RoleName.ADMIN);
        Long adminUserId = userRepository.findByEmailIgnoreCase(adminEmail).orElseThrow().getId();
        String adminToken = login(adminEmail);

        mockMvc.perform(patch("/api/users/{userId}/role", adminUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "AUTHOR"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Administrators cannot change their own role"));
    }

    @Test
    void rejectsInvalidJwtForAuthenticatedEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer("invalid.jwt.token")))
                .andExpect(status().is4xxClientError());
    }

    private String createUser(RoleName roleName) {
        String email = roleName.name().toLowerCase() + "." + UUID.randomUUID() + "@san.local";
        User user = new User();
        user.setFullName(roleName.name() + " Security Test");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRoleName(roleName);
        user.setEnabled(true);
        userRepository.save(user);
        return email;
    }

    private String login(String email) throws Exception {
        String payload = """
                {
                  "email": "%s",
                  "password": "password"
                }
                """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
