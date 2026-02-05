package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.dto.machine.RegisterMachineDTO;
import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.model.Machine;
import com.vectorlabs.repository.MachineRepository;
import com.vectorlabs.security.jwt.JwtGrantedAuthoritiesConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class MachineControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MachineRepository machineRepository;

    // Mantém se sua SecurityConfiguration precisa desse bean no contexto de teste
    @MockitoBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @BeforeEach
    void setup() {
        machineRepository.deleteAll();
    }

    // ------------------- CREATE -------------------

    @Test
    void shouldReturnForbiddenOnCreateWhenNotAuthenticated() throws Exception {
        RegisterMachineDTO dto = new RegisterMachineDTO(
                "Compressor",
                "Atlas Copco",
                "GA 37"
        );

        mockMvc.perform(post("/api/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldCreateMachineSuccessfully() throws Exception {
        RegisterMachineDTO dto = new RegisterMachineDTO(
                "Compressor",
                "Atlas Copco",
                "GA 37"
        );

        mockMvc.perform(post("/api/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/machines/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("Compressor"))
                .andExpect(jsonPath("$.manufacturer").value("Atlas Copco"))
                .andExpect(jsonPath("$.model").value("GA 37"));
    }

    // ------------------- DETAILS -------------------

    @Test
    void shouldReturnForbiddenOnDetailsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/machines/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldGetDetailsSuccessfully() throws Exception {
        Machine saved = new Machine();
        saved.setType("Bomba");
        saved.setManufacturer("KSB");
        saved.setModel("Etanorm");
        saved = machineRepository.save(saved);

        mockMvc.perform(get("/api/machines/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.type").value("Bomba"))
                .andExpect(jsonPath("$.manufacturer").value("KSB"))
                .andExpect(jsonPath("$.model").value("Etanorm"));
    }

    // ------------------- LIST (SEARCH) -------------------

    @Test
    void shouldReturnForbiddenOnListWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/machines"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldListMachinesSuccessfully_withFilters() throws Exception {
        Machine a = new Machine();
        a.setType("Compressor");
        a.setManufacturer("Atlas Copco");
        a.setModel("GA 37");

        Machine b = new Machine();
        b.setType("Compressor");
        b.setManufacturer("Atlas Copco");
        b.setModel("GA 55");

        machineRepository.saveAll(List.of(a, b));

        // Ajuste os nomes dos params conforme seus campos em SearchMachineDTO
        // (por padrão, @ModelAttribute usa o nome do atributo do DTO)
        mockMvc.perform(get("/api/machines")
                        .param("type", "Compressor")
                        .param("manufacturer", "Atlas Copco")
                        .param("page", "0")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // ------------------- UPDATE -------------------

    @Test
    void shouldReturnForbiddenOnUpdateWhenNotAuthenticated() throws Exception {
        UpdateMachineDTO dto = new UpdateMachineDTO(
                "Compressor",
                "Atlas Copco",
                "GA 75"
        );

        mockMvc.perform(put("/api/machines/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldUpdateMachineSuccessfully() throws Exception {
        Machine saved = new Machine();
        saved.setType("Compressor");
        saved.setManufacturer("Atlas Copco");
        saved.setModel("GA 37");
        saved = machineRepository.save(saved);

        UpdateMachineDTO dto = new UpdateMachineDTO(
                "Compressor",
                "Atlas Copco",
                "GA 75"
        );

        mockMvc.perform(put("/api/machines/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.type").value("Compressor"))
                .andExpect(jsonPath("$.manufacturer").value("Atlas Copco"))
                .andExpect(jsonPath("$.model").value("GA 75"));
    }

    // ------------------- DELETE -------------------

    @Test
    void shouldReturnForbiddenOnDeleteWhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/machines/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldDeleteMachineSuccessfully() throws Exception {
        Machine saved = new Machine();
        saved.setType("Motor");
        saved.setManufacturer("WEG");
        saved.setModel("W22");
        saved = machineRepository.save(saved);

        UUID id = saved.getId();

        mockMvc.perform(delete("/api/machines/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(machineRepository.findById(id).isPresent(),
                "Esperava que o registro fosse removido do banco");
    }
}
