package com.vectorlabs.validator;

import com.vectorlabs.dto.appuser.UpdateAppUserDTO;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserValidatorTest {

    @Mock
    AppUserRepository repository;

    AppUserValidator validator;

    @BeforeEach
    void setup() {
        validator = new AppUserValidator(repository);
    }

    // -------------------------
    // requireId
    // -------------------------

    @Test
    void requireId_shouldThrowWhenNull() {
        var ex = assertThrows(IllegalArgumentException.class, () -> validator.requireId(null));
        assertEquals("id cannot be null", ex.getMessage());
    }

    @Test
    void requireId_shouldNotThrowWhenNotNull() {
        assertDoesNotThrow(() -> validator.requireId(UUID.randomUUID()));
    }

    // -------------------------
    // normalizeEmail
    // -------------------------

    @Test
    void normalizeEmail_shouldReturnEmptyStringWhenNull() {
        assertEquals("", validator.normalizeEmail(null));
    }

    @Test
    void normalizeEmail_shouldTrimAndLowercase() {
        assertEquals("test@email.com", validator.normalizeEmail("  TeSt@Email.Com  "));
    }

    @Test
    void normalizeEmail_shouldReturnEmptyStringWhenBlankInput() {
        // pelo seu código atual, isso vira "" (trim => "", lower => "")
        assertEquals("", validator.normalizeEmail("   "));
    }

    // -------------------------
    // normalizeEmailNullable
    // -------------------------

    @Test
    void normalizeEmailNullable_shouldReturnNullWhenNull() {
        assertNull(validator.normalizeEmailNullable(null));
    }

    @Test
    void normalizeEmailNullable_shouldReturnNullWhenBlankAfterTrim() {
        assertNull(validator.normalizeEmailNullable("   "));
    }

    @Test
    void normalizeEmailNullable_shouldTrimAndLowercaseAndReturnValue() {
        assertEquals("a@b.com", validator.normalizeEmailNullable("  A@B.COM  "));
    }

    // -------------------------
    // validateRegister
    // -------------------------

    @Test
    void validateRegister_shouldThrowWhenEmailAlreadyExists() {
        when(repository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);

        var ex = assertThrows(IllegalArgumentException.class, () -> validator.validateRegister("a@b.com"));
        assertEquals("email already in use", ex.getMessage());

        verify(repository).existsByEmailIgnoreCase("a@b.com");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void validateRegister_shouldNotThrowWhenEmailNotExists() {
        when(repository.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateRegister("a@b.com"));

        verify(repository).existsByEmailIgnoreCase("a@b.com");
        verifyNoMoreInteractions(repository);
    }

    // -------------------------
    // validateAdminUpdate / validateMeUpdate
    // (por enquanto sem regras, só garantir que não explode)
    // -------------------------

    @Test
    void validateAdminUpdate_shouldNotThrowCurrently() {
        UpdateAppUserDTO dto = mock(UpdateAppUserDTO.class);
        AppUser current = mock(AppUser.class);

        assertDoesNotThrow(() -> validator.validateAdminUpdate(dto, current));
        verifyNoInteractions(repository);
    }

    @Test
    void validateMeUpdate_shouldNotThrowCurrently() {
        UpdateAppUserDTO dto = mock(UpdateAppUserDTO.class);

        assertDoesNotThrow(() -> validator.validateMeUpdate(dto));
        verifyNoInteractions(repository);
    }

    // -------------------------
    // ensureNotDeletedForRead / ensureNotDeletedForWrite
    // -------------------------

    @Test
    void ensureNotDeletedForRead_shouldThrowWhenDeleted() {
        AppUser user = mock(AppUser.class);
        when(user.isDeleted()).thenReturn(true);

        var ex = assertThrows(IllegalArgumentException.class, () -> validator.ensureNotDeletedForRead(user));
        assertEquals("user is deleted", ex.getMessage());
    }

    @Test
    void ensureNotDeletedForRead_shouldNotThrowWhenNotDeleted() {
        AppUser user = mock(AppUser.class);
        when(user.isDeleted()).thenReturn(false);

        assertDoesNotThrow(() -> validator.ensureNotDeletedForRead(user));
    }

    @Test
    void ensureNotDeletedForWrite_shouldThrowWhenDeleted() {
        AppUser user = mock(AppUser.class);
        when(user.isDeleted()).thenReturn(true);

        var ex = assertThrows(IllegalArgumentException.class, () -> validator.ensureNotDeletedForWrite(user));
        assertEquals("user is deleted", ex.getMessage());
    }

    @Test
    void ensureNotDeletedForWrite_shouldNotThrowWhenNotDeleted() {
        AppUser user = mock(AppUser.class);
        when(user.isDeleted()).thenReturn(false);

        assertDoesNotThrow(() -> validator.ensureNotDeletedForWrite(user));
    }

    // -------------------------
    // ensureCanHardDelete
    // -------------------------

    @Test
    void ensureCanHardDelete_shouldThrowWhenNotSoftDeleted() {
        AppUser user = mock(AppUser.class);
        when(user.isDeleted()).thenReturn(false);

        var ex = assertThrows(IllegalArgumentException.class, () -> validator.ensureCanHardDelete(user));
        assertEquals("hard delete only allowed after soft delete", ex.getMessage());
    }

    @Test
    void ensureCanHardDelete_shouldNotThrowWhenSoftDeleted() {
        AppUser user = mock(AppUser.class);
        when(user.isDeleted()).thenReturn(true);

        assertDoesNotThrow(() -> validator.ensureCanHardDelete(user));
    }
}
