package com.vectorlabs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.model.Address;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.model.Location;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.InspectionLocationType;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.repository.ClientCompanyRepository;
import com.vectorlabs.repository.LocationRepository;
import com.vectorlabs.security.SecurityService;
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

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class LocationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired LocationRepository locationRepository;
    @Autowired ClientCompanyRepository clientCompanyRepository;
    @Autowired AppUserRepository appUserRepository;

    @MockitoBean JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;
    @MockitoBean SecurityService securityService;

    @BeforeEach
    void setup() {
        locationRepository.deleteAll();
        clientCompanyRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    private void mockLoggedUser(AppUser user) {
        // stub do método inteiro (não encadeia .getId() dentro do when)
        when(securityService.getLoggedUser()).thenReturn(user);

        // Se seu SecurityService tiver esse método, habilita:
        // when(securityService.getCurrentUserId()).thenReturn(user.getId());
    }

    private AppUser createUser() {
        var user = AppUser.builder()
                .email("test-" + UUID.randomUUID() + "@vectorlabs.local")
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .deleted(false)
                .build();

        return appUserRepository.saveAndFlush(user);
    }

    private ClientCompany createCompany(String corporateName, UUID userId) {
        var user = appUserRepository.getReferenceById(userId);

        var company = ClientCompany.builder()
                .corporateName(corporateName)
                .user(user)
                .active(true)
                .deleted(false)
                .build();

        return clientCompanyRepository.saveAndFlush(company);
    }

    private Location createLocation(ClientCompany company, String name, String code) {
        Address address = new Address();
        address.setStreet("Av. Paulista, 1000");
        address.setCity("São Paulo");
        address.setState("SP");
        address.setZipCode("01310-100");

        Location location = new Location();
        location.setCompany(company);
        location.setAddress(address);
        location.setName(name);
        location.setCode(code);
        location.setDescription("Unidade SP");
        location.setType(InspectionLocationType.CONSTRUCTION_SITE);

        return locationRepository.save(location);
    }

    // ------------------- CREATE -------------------

    @Test
    void shouldReturnForbiddenOnCreateWhenNotAuthenticated() throws Exception {
        var payload = """
            {
              "companyId": "%s",
              "name": "Matriz RJ",
              "code": "RJ-001",
              "type": "CONSTRUCTION_SITE",
              "description": "Unidade principal",
              "street": "Rua A, 123",
              "city": "Rio de Janeiro",
              "state": "RJ",
              "zipCode": "20000-000"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldCreateLocationSuccessfully() throws Exception {
        AppUser user = createUser();
        mockLoggedUser(user);

        ClientCompany company = createCompany("Empresa XPTO", user.getId());

        var payload = """
        {
          "clientCompanyId": "%s",
          "name": "Matriz RJ",
          "code": "RJ-001",
          "type": "CONSTRUCTION_SITE",
          "description": "Unidade principal",
          "street": "Rua A, 123",
          "city": "Rio de Janeiro",
          "state": "RJ",
          "zipCode": "20000-000"
        }
        """.formatted(company.getId());

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    // ------------------- DETAILS -------------------

    @Test
    void shouldReturnForbiddenOnDetailsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/locations/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldGetDetailsSuccessfully() throws Exception {
        AppUser user = createUser();
        mockLoggedUser(user);

        ClientCompany company = createCompany("Empresa XPTO", user.getId());
        Location saved = createLocation(company, "Filial SP", "SP-010");

        mockMvc.perform(get("/api/locations/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value("Filial SP"))
                .andExpect(jsonPath("$.code").value("SP-010"))
                .andExpect(jsonPath("$.description").value("Unidade SP"));
    }

    // ------------------- LIST (SEARCH) -------------------

    @Test
    void shouldReturnForbiddenOnListWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldListLocationsSuccessfully_withFilters() throws Exception {
        AppUser user = createUser();
        mockLoggedUser(user);

        ClientCompany company = createCompany("Empresa XPTO", user.getId());

        createLocation(company, "Matriz RJ", "RJ-001");
        createLocation(company, "Matriz RJ - Anexo", "RJ-002");

        mockMvc.perform(get("/api/locations")
                        .param("clientCompanyId", company.getId().toString())
                        .param("name", "Matriz")
                        .param("city", "São Paulo")
                        .param("state", "SP")
                        .param("page", "0")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // ------------------- UPDATE -------------------

    @Test
    void shouldReturnForbiddenOnUpdateWhenNotAuthenticated() throws Exception {
        var payload = """
            {
              "name": "Matriz RJ Atualizada",
              "code": "RJ-001-A",
              "type": "CONSTRUCTION_SITE",
              "description": "Atualizado",
              "street": "Rua B, 456",
              "city": "Rio de Janeiro",
              "state": "RJ",
              "zipCode": "20000-111"
            }
            """;

        mockMvc.perform(put("/api/locations/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldUpdateLocationSuccessfully() throws Exception {
        AppUser user = createUser();
        mockLoggedUser(user);

        ClientCompany company = createCompany("Empresa XPTO", user.getId());
        Location saved = createLocation(company, "Matriz RJ", "RJ-001");

        var payload = """
            {
              "name": "Matriz RJ Atualizada",
              "code": "RJ-001-A",
              "type": "CONSTRUCTION_SITE",
              "description": "Atualizado",
              "street": "Rua B, 456",
              "city": "Rio de Janeiro",
              "state": "RJ",
              "zipCode": "20000-111"
            }
            """;

        mockMvc.perform(put("/api/locations/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value("Matriz RJ Atualizada"))
                .andExpect(jsonPath("$.code").value("RJ-001-A"))
                .andExpect(jsonPath("$.description").value("Atualizado"));
    }

    // ------------------- DELETE -------------------

    @Test
    void shouldReturnForbiddenOnDeleteWhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/locations/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldDeleteLocationSuccessfully() throws Exception {
        AppUser user = createUser();
        mockLoggedUser(user);

        ClientCompany company = createCompany("Empresa XPTO", user.getId());
        Location saved = createLocation(company, "Unidade Temporária", "TMP-001");

        UUID id = saved.getId();

        mockMvc.perform(delete("/api/locations/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(locationRepository.findById(id).isPresent(),
                "Esperava que o registro fosse removido do banco");
    }
}
