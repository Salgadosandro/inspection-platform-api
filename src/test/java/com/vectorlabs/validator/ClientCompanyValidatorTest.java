package com.vectorlabs.validator;

import com.vectorlabs.dto.clientcompany.RegisterClientCompanyAdminDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.ClientCompany;
import com.vectorlabs.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCompanyValidatorTest {

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private ClientCompanyValidator validator;

    // =================== CREATE ===================

    @Test
    void validateCreate_shouldThrow_whenDtoNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreate(null));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateCreate_shouldThrow_whenCorporateNameBlank() {
        RegisterClientCompanyDTO dto = mock(RegisterClientCompanyDTO.class);
        when(dto.corporateName()).thenReturn("   "); // blank -> quebra cedo

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreate(dto));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateCreate_shouldThrow_whenAddressNull() {
        RegisterClientCompanyDTO dto = mock(RegisterClientCompanyDTO.class);
        when(dto.corporateName()).thenReturn("VectorLabs LTDA");
        when(dto.address()).thenReturn(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreate(dto));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateCreate_shouldPass_whenValid() {
        // RETURNS_MOCKS garante dto.address() != null sem precisar saber o tipo
        RegisterClientCompanyDTO dto = mock(RegisterClientCompanyDTO.class, RETURNS_MOCKS);
        when(dto.corporateName()).thenReturn("VectorLabs LTDA");

        assertDoesNotThrow(() -> validator.validateCreate(dto));
        verifyNoInteractions(appUserService);
    }

    // =================== ADMIN CREATE ===================

    @Test
    void validateAdminCreate_shouldThrow_whenDtoNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateAdminCreate(null));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateAdminCreate_shouldThrow_whenUserIdNull() {
        RegisterClientCompanyAdminDTO dto = mock(RegisterClientCompanyAdminDTO.class);
        when(dto.userId()).thenReturn(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateAdminCreate(dto));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateAdminCreate_shouldThrow_whenCorporateNameBlank() {
        RegisterClientCompanyAdminDTO dto = mock(RegisterClientCompanyAdminDTO.class);
        when(dto.userId()).thenReturn(UUID.randomUUID());
        when(dto.corporateName()).thenReturn("   "); // quebra aqui

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateAdminCreate(dto));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateAdminCreate_shouldThrow_whenAddressNull() {
        RegisterClientCompanyAdminDTO dto = mock(RegisterClientCompanyAdminDTO.class);
        when(dto.userId()).thenReturn(UUID.randomUUID());
        when(dto.corporateName()).thenReturn("VectorLabs LTDA");
        when(dto.address()).thenReturn(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateAdminCreate(dto));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateAdminCreate_shouldPass_whenValid() {
        RegisterClientCompanyAdminDTO dto = mock(RegisterClientCompanyAdminDTO.class, RETURNS_MOCKS);
        when(dto.userId()).thenReturn(UUID.randomUUID());
        when(dto.corporateName()).thenReturn("VectorLabs LTDA");

        assertDoesNotThrow(() -> validator.validateAdminCreate(dto));
        verifyNoInteractions(appUserService);
    }

    // =================== UPDATE ===================

    @Test
    void validateUpdate_shouldThrow_whenDtoNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(null));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateUpdate_shouldThrow_whenAllFieldsMissingOrBlank() {
        UpdateClientCompanyDTO dto = mock(UpdateClientCompanyDTO.class);
        when(dto.corporateName()).thenReturn("   ");
        when(dto.tradeName()).thenReturn(null);
        when(dto.cnpj()).thenReturn(" ");
        when(dto.phone()).thenReturn(null);
        when(dto.email()).thenReturn("   ");
        when(dto.address()).thenReturn(null);

        assertThrows(InvalidFieldException.class, () -> validator.validateUpdate(dto));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateUpdate_shouldPass_whenAtLeastOneFieldProvided() {
        UpdateClientCompanyDTO dto = mock(UpdateClientCompanyDTO.class);

        // Stub mínimo: como tradeName já “salva” o update, não stub mais nada
        when(dto.corporateName()).thenReturn(null);
        when(dto.tradeName()).thenReturn("Vector");

        assertDoesNotThrow(() -> validator.validateUpdate(dto));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateUpdate_shouldPass_whenAddressProvided() {
        // aqui queremos garantir address != null, então RETURNS_MOCKS é útil
        UpdateClientCompanyDTO dto = mock(UpdateClientCompanyDTO.class, RETURNS_MOCKS);

        // deixa os Strings nulos para garantir que quem “salva” é o address
        when(dto.corporateName()).thenReturn(null);
        when(dto.tradeName()).thenReturn(null);
        when(dto.cnpj()).thenReturn(null);
        when(dto.phone()).thenReturn(null);
        when(dto.email()).thenReturn(null);

        assertDoesNotThrow(() -> validator.validateUpdate(dto));
        verifyNoInteractions(appUserService);
    }

    // =================== SOFT DELETE / NOT DELETED ===================

    @Test
    void validateSoftDelete_shouldThrow_whenCompanyNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateSoftDelete(null));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateSoftDelete_shouldThrow_whenAlreadyDeleted() {
        ClientCompany company = new ClientCompany();
        company.setDeleted(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateSoftDelete(company));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateSoftDelete_shouldPass_whenNotDeleted() {
        ClientCompany company = new ClientCompany();
        company.setDeleted(false);

        assertDoesNotThrow(() -> validator.validateSoftDelete(company));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateNotDeleted_shouldThrow_whenCompanyNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateNotDeleted(null));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateNotDeleted_shouldThrow_whenDeleted() {
        ClientCompany company = new ClientCompany();
        company.setDeleted(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateNotDeleted(company));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateNotDeleted_shouldPass_whenNotDeleted() {
        ClientCompany company = new ClientCompany();
        company.setDeleted(false);

        assertDoesNotThrow(() -> validator.validateNotDeleted(company));
        verifyNoInteractions(appUserService);
    }

    // =================== SEARCH ===================

    @Test
    void validateSearch_shouldThrow_whenPageNegative() {
        assertThrows(InvalidFieldException.class, () -> validator.validateSearch(-1, 10));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateSearch_shouldThrow_whenPageSizeZeroOrNegative() {
        assertThrows(InvalidFieldException.class, () -> validator.validateSearch(0, 0));
        assertThrows(InvalidFieldException.class, () -> validator.validateSearch(0, -5));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateSearch_shouldThrow_whenPageSizeTooLarge() {
        assertThrows(InvalidFieldException.class, () -> validator.validateSearch(0, 101));
        verifyNoInteractions(appUserService);
    }

    @Test
    void validateSearch_shouldPass_whenValid() {
        assertDoesNotThrow(() -> validator.validateSearch(0, 10));
        assertDoesNotThrow(() -> validator.validateSearch(null, null));
        assertDoesNotThrow(() -> validator.validateSearch(5, 100));
        verifyNoInteractions(appUserService);
    }

    // =================== resolveOwner ===================

    @Test
    void resolveOwner_shouldThrow_whenUserIdNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.resolveOwner(null));
        verifyNoInteractions(appUserService);
    }

    @Test
    void resolveOwner_shouldReturnUser_whenFound() {
        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser();
        user.setId(userId);

        when(appUserService.findById(userId)).thenReturn(user);

        AppUser out = validator.resolveOwner(userId);

        assertNotNull(out);
        assertEquals(userId, out.getId());
        verify(appUserService).findById(userId);
        verifyNoMoreInteractions(appUserService);
    }
}
