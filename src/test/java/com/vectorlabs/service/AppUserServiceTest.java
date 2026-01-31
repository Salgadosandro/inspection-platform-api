package com.vectorlabs.service;

import com.vectorlabs.dto.address.RegisterAddressDTO;
import com.vectorlabs.dto.address.UpdateAddressDTO;
import com.vectorlabs.dto.appuser.RegisterAppUserDTO;
import com.vectorlabs.dto.appuser.SearchAppUserDTO;
import com.vectorlabs.dto.appuser.UpdateAppUserDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.AddressMapper;
import com.vectorlabs.model.Address;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.validator.AppUserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;


import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock private AppUserRepository repository;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock private AppUserValidator validator;
    @Mock private AddressMapper addressMapper;

    @InjectMocks private AppUserService service;

    private UUID id;
    private AppUser existing;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        existing = new AppUser();
        existing.setId(id);
        existing.setName("Sandro");
        existing.setEmail("sandro@x.com");
        existing.setEnabled(true);
        existing.setDeleted(false);
        existing.setAuthProvider(AuthProvider.LOCAL);
        existing.setRoles(Set.of(UserRole.CLIENT));
    }

    // ------------------------------------------------------------
    // register
    // ------------------------------------------------------------

    @Test
    void register_shouldNormalizeValidateEncodeAndSave_withoutAddress() {
        // arrange
        RegisterAppUserDTO dto = new RegisterAppUserDTO(
                "  SANDRO@X.COM  ",
                "12345678",
                "Sandro",
                null
        );

        when(validator.normalizeEmail("  SANDRO@X.COM  ")).thenReturn("sandro@x.com");
        when(passwordEncoder.encode("12345678")).thenReturn("ENC");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0, AppUser.class));

        // act
        AppUser saved = service.register(dto);

        // assert
        verify(validator).normalizeEmail("  SANDRO@X.COM  ");
        verify(validator).validateRegister("sandro@x.com");
        verify(passwordEncoder).encode("12345678");
        verify(repository).save(captor.capture());
        verifyNoInteractions(addressMapper);

        AppUser u = captor.getValue();
        assertEquals("Sandro", u.getName());
        assertEquals("sandro@x.com", u.getEmail());
        assertEquals("ENC", u.getPassword());
        assertTrue(u.isEnabled());
        assertFalse(u.isDeleted());
        assertEquals(AuthProvider.LOCAL, u.getAuthProvider());
        assertEquals(Set.of(UserRole.CLIENT), u.getRoles());
        assertNull(u.getAddress());

        assertSame(u, saved);
    }

    @Test
    void register_shouldMapAddress_whenAddressProvided() {
        // arrange
        RegisterAddressDTO addrDto = new RegisterAddressDTO(
                "Rua A",
                "10",
                "Centro",
                "Rio",
                "RJ",
                "20000-000",
                "Brasil",
                ""

        );

        RegisterAppUserDTO dto = new RegisterAppUserDTO(
                "sandro@x.com",
                "12345678",
                "Sandro",
                addrDto
        );

        when(validator.normalizeEmail("sandro@x.com")).thenReturn("sandro@x.com");
        when(passwordEncoder.encode("12345678")).thenReturn("ENC");

        Address mapped = new Address();
        mapped.setCity("Rio");
        when(addressMapper.toEntity(addrDto)).thenReturn(mapped);

        when(repository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        AppUser saved = service.register(dto);

        // assert
        verify(addressMapper).toEntity(addrDto);
        assertNotNull(saved.getAddress());
        assertEquals("Rio", saved.getAddress().getCity());
    }

    @Test
    void register_shouldThrowNpe_whenDtoNull() {
        assertThrows(NullPointerException.class, () -> service.register(null));
    }

    // ------------------------------------------------------------
    // findById
    // ------------------------------------------------------------

    @Test
    void findById_shouldReturnUser_whenExists() {
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        AppUser result = service.findById(id);

        verify(validator).requireId(id);
        verify(validator).ensureNotDeletedForRead(existing);
        assertSame(existing, result);
    }

    @Test
    void findById_shouldThrowObjectNotFound_whenMissing() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.findById(id));

        verify(validator).requireId(id);
        verify(validator, never()).ensureNotDeletedForRead(any());
    }

    // ------------------------------------------------------------
    // update
    // ------------------------------------------------------------

    @Test
    void update_shouldApplyNameCpfCnpj_andSave() {
        // arrange
        UpdateAppUserDTO dto = new UpdateAppUserDTO(
                "Novo Nome",
                null,
                "11111111111111",
                "222222222222222222",
                null
        );

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        AppUser updated = service.update(id, dto);

        // assert
        verify(validator).requireId(id);
        verify(validator).ensureNotDeletedForWrite(existing);
        verify(validator).validateAdminUpdate(dto, existing);
        verify(repository).save(existing);

        assertEquals("Novo Nome", updated.getName());
        assertEquals("11111111111111", updated.getCpf());
        assertEquals("222222222222222222", updated.getCnpj());
    }

    @Test
    void update_shouldCreateAddress_whenUserAddressNull_andDtoHasAddress() {
        // arrange
        existing.setAddress(null);

        UpdateAddressDTO addrDto = new UpdateAddressDTO(
                "Rua B",
                "55",
                "Bairro X",
                "São Paulo",
                "SP",
                "01000-000",
                "Brasil",
                ""
        );

        UpdateAppUserDTO dto = new UpdateAppUserDTO(
                null,
                null,
                null,
                null,
                addrDto
        );

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        AppUser updated = service.update(id, dto);

        // assert
        assertNotNull(updated.getAddress(), "Deveria criar Address quando user.getAddress() == null");
        assertEquals("São Paulo", updated.getAddress().getCity());
        assertEquals("SP", updated.getAddress().getState());
        assertEquals("Brasil", updated.getAddress().getCountry());
        assertEquals("01000-000", updated.getAddress().getZipCode());
        assertEquals("Rua B", updated.getAddress().getStreet());
        assertEquals("55", updated.getAddress().getNumber());
        assertEquals("Bairro X", updated.getAddress().getDistrict());
    }

    @Test
    void update_shouldThrowNpe_whenDtoNull() {
        assertThrows(NullPointerException.class, () -> service.update(id, null));
    }

    @Test
    void update_shouldThrowObjectNotFound_whenMissing() {
        UpdateAppUserDTO dto = new UpdateAppUserDTO(null, null, null, null, null);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.update(id, dto));

        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------
    // updateMe
    // ------------------------------------------------------------

    @Test
    void updateMe_shouldLoadByLoggedId_validate_andSave() {
        // arrange
        AppUser logged = new AppUser();
        logged.setId(id);

        UpdateAppUserDTO dto = new UpdateAppUserDTO(
                "Meu Nome",
                "http://img",
                null,
                null,
                null
        );

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        AppUser updated = service.updateMe(dto, logged);

        // assert
        verify(validator).validateMeUpdate(dto);
        verify(validator).ensureNotDeletedForWrite(existing);
        verify(repository).save(existing);

        assertEquals("Meu Nome", updated.getName());
        // NOTE: seu applyUpdate não aplica pictureUrl (ele existe no DTO mas não está sendo setado)
        // então não dá pra testar pictureUrl aqui, a menos que você implemente no applyUpdate.
    }

    @Test
    void updateMe_shouldThrowNpe_whenLoggedNull() {
        UpdateAppUserDTO dto = new UpdateAppUserDTO(null, null, null, null, null);
        assertThrows(NullPointerException.class, () -> service.updateMe(dto, null));
    }

    @Test
    void updateMe_shouldThrowObjectNotFound_whenLoggedNotFound() {
        AppUser logged = new AppUser();
        logged.setId(id);
        UpdateAppUserDTO dto = new UpdateAppUserDTO(null, null, null, null, null);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.updateMe(dto, logged));
        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------
    // search (sem mock static)
    // ------------------------------------------------------------

    @Test
    void search_shouldCallRepositoryFindAll_andReturnPage() {
        // arrange
        SearchAppUserDTO filter = new SearchAppUserDTO(
                null,
                "Sandro",
                "sandro@x.com",
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                AuthProvider.LOCAL,
                Set.of(UserRole.CLIENT),
                Instant.now().minusSeconds(3600),
                Instant.now()
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<AppUser> page = new PageImpl<>(List.of(existing), pageable, 1);

        when(repository.findAll(Mockito.<Specification<AppUser>>any(), eq(pageable))).thenReturn(page);

        // act
        Page<AppUser> result = service.search(filter, pageable);

        // assert
        assertEquals(1, result.getTotalElements());
        assertSame(existing, result.getContent().get(0));
        verify(repository).findAll(Mockito.<Specification<AppUser>>any(), eq(pageable));

    }

    @Test
    void search_shouldThrowNpe_whenFilterNull() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(NullPointerException.class, () -> service.search(null, pageable));
    }

    @Test
    void search_shouldThrowNpe_whenPageableNull() {
        SearchAppUserDTO filter = new SearchAppUserDTO(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );
        assertThrows(NullPointerException.class, () -> service.search(filter, null));
    }

    // ------------------------------------------------------------
    // patch delegates to update
    // ------------------------------------------------------------

    @Test
    void patch_shouldDelegateToUpdate() {
        // arrange
        UpdateAppUserDTO dto = new UpdateAppUserDTO("X", null, null, null, null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        AppUser result = service.patch(id, dto);

        // assert
        verify(repository).findById(id);
        verify(repository).save(existing);
        assertSame(existing, result);
    }

    // ------------------------------------------------------------
    // softDelete
    // ------------------------------------------------------------

    @Test
    void softDelete_shouldMarkDeletedDisable_andSave() {
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.softDelete(id);

        verify(validator).requireId(id);
        verify(validator).ensureNotDeletedForWrite(existing);
        verify(repository).save(existing);

        assertTrue(existing.isDeleted());
        assertFalse(existing.isEnabled());
    }

    @Test
    void softDelete_shouldThrowObjectNotFound_whenMissing() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.softDelete(id));
        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------
    // deleteById
    // ------------------------------------------------------------

    @Test
    void deleteById_shouldHardDelete_andReturnUser() {
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        AppUser deleted = service.deleteById(id);

        verify(validator).requireId(id);
        verify(validator).ensureCanHardDelete(existing);
        verify(repository).delete(existing);
        assertSame(existing, deleted);
    }

    @Test
    void deleteById_shouldThrowObjectNotFound_whenMissing() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.deleteById(id));
        verify(repository, never()).delete(Mockito.<AppUser>any());

    }

    // ------------------------------------------------------------
    // softDeleteMe
    // ------------------------------------------------------------

    @Test
    void softDeleteMe_shouldMarkDeletedDisable_andSave() {
        AppUser logged = new AppUser();
        logged.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = service.softDeleteMe(logged);

        verify(validator).ensureNotDeletedForWrite(existing);
        verify(repository).save(existing);

        assertTrue(result.isDeleted());
        assertFalse(result.isEnabled());
    }

    @Test
    void softDeleteMe_shouldThrowNpe_whenLoggedNull() {
        assertThrows(NullPointerException.class, () -> service.softDeleteMe(null));
    }
}
