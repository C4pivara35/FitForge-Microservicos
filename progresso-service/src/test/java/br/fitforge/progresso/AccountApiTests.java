package br.fitforge.progresso;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {"app.internal-key=test-only", "app.seed=false"})
class AccountApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void cadastraEAutenticaSemAceitarChamadaExternaOuUsuarioDuplicado() throws Exception {
        String path = "/api/internal/accounts";
        String credentials = json.writeValueAsString(Map.of("username", "Conta", "password", "senha-de-teste"));
        mvc.perform(post(path).servletPath(path).contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).servletPath(path).header("X-FitForge-Internal-Key", "test-only")
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated());
        mvc.perform(post(path).servletPath(path).header("X-FitForge-Internal-Key", "test-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "conta", "password", "outra-senha"))))
                .andExpect(status().isConflict());
        mvc.perform(post(path + "/authenticate").servletPath(path + "/authenticate").header("X-FitForge-Internal-Key", "test-only")
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isNoContent());
        mvc.perform(post(path + "/authenticate").servletPath(path + "/authenticate").header("X-FitForge-Internal-Key", "test-only")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "CONTA", "password", "senha-errada"))))
                .andExpect(status().isUnauthorized());
    }
}
