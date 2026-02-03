package com.vectorlabs.service;
import com.vectorlabs.dto.machine.SearchMachineDTO;
import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.exception.DoubleRegisterException;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.MachineMapper;
import com.vectorlabs.model.Machine;
import com.vectorlabs.repository.MachineRepository;
import com.vectorlabs.validator.MachineValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock private MachineRepository repository;
    @Mock private MachineMapper mapper;
    @Mock private MachineValidator validator;

    @InjectMocks
    private MachineService service;

    // ------------------- SAVE -------------------

    @Test
    void save_shouldNormalize_validate_andPersist_whenNotDuplicate() {
        // Arrange
        Machine entity = new Machine();
        entity.setType("  Prensa   Hidráulica  ");
        entity.setManufacturer("  Siemens   ");
        entity.setModel("  XPT-300  ");

        when(repository.existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCase(
                anyString(), anyString(), anyString()
        )).thenReturn(false);

        when(repository.save(any(Machine.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Machine saved = service.save(entity);

        // Assert
        verify(validator).validateCreation(entity);

        ArgumentCaptor<Machine> captor = ArgumentCaptor.forClass(Machine.class);
        verify(repository).save(captor.capture());

        Machine toSave = captor.getValue();
        assertThat(toSave.getType()).isEqualTo("Prensa Hidráulica");
        assertThat(toSave.getManufacturer()).isEqualTo("Siemens");
        assertThat(toSave.getModel()).isEqualTo("XPT-300");

        assertThat(saved).isSameAs(entity);
        verifyNoMoreInteractions(repository, validator);
        verifyNoInteractions(mapper);
    }

    @Test
    void save_shouldThrowDoubleRegisterException_whenDuplicateExists() {
        // Arrange
        Machine entity = new Machine();
        entity.setType("Prensa");
        entity.setManufacturer("Siemens");
        entity.setModel("XPT-300");

        when(repository.existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCase(
                "Prensa", "Siemens", "XPT-300"
        )).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.save(entity))
                .isInstanceOf(DoubleRegisterException.class);

        verify(validator).validateCreation(entity);
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    // ------------------- UPDATE -------------------

    @Test
    void update_shouldThrowObjectNotFound_whenIdDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateMachineDTO dto = mock(UpdateMachineDTO.class);

        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(ObjectNotFound.class);

        verify(repository).findById(id);
        verifyNoInteractions(mapper, validator);
    }

    @Test
    void update_shouldValidate_applyMapperNormalize_andSave_whenNotDuplicate() {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateMachineDTO dto = mock(UpdateMachineDTO.class);

        Machine before = new Machine();
        before.setId(id);
        before.setType("Antigo");
        before.setManufacturer("Antigo");
        before.setModel("Antigo");

        when(repository.findById(id)).thenReturn(Optional.of(before));

        // mapper altera in-place (com espaços para testar normalize)
        doAnswer(inv -> {
            Machine m = inv.getArgument(0);
            m.setType("  Prensa   ");
            m.setManufacturer("  Siemens  ");
            m.setModel("  XPT-300  ");
            return null;
        }).when(mapper).updateEntity(before, dto);

        when(repository.existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCaseAndIdNot(
                "Prensa", "Siemens", "XPT-300", id
        )).thenReturn(false);

        when(repository.save(before)).thenReturn(before);

        // Act
        Machine updated = service.update(id, dto);

        // Assert
        verify(validator).validateUpdate(before, dto);
        verify(mapper).updateEntity(before, dto);

        assertThat(before.getType()).isEqualTo("Prensa");
        assertThat(before.getManufacturer()).isEqualTo("Siemens");
        assertThat(before.getModel()).isEqualTo("XPT-300");

        verify(repository).save(before);
        assertThat(updated).isSameAs(before);
    }

    @Test
    void update_shouldThrowDoubleRegisterException_whenDuplicateExists_excludingSameId() {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateMachineDTO dto = mock(UpdateMachineDTO.class);

        Machine before = new Machine();
        before.setId(id);
        before.setType("Prensa");
        before.setManufacturer("Siemens");
        before.setModel("XPT-300");

        when(repository.findById(id)).thenReturn(Optional.of(before));
        doNothing().when(mapper).updateEntity(before, dto);

        when(repository.existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCaseAndIdNot(
                "Prensa", "Siemens", "XPT-300", id
        )).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(DoubleRegisterException.class);

        verify(validator).validateUpdate(before, dto);
        verify(mapper).updateEntity(before, dto);
        verify(repository, never()).save(any());
    }

    // ------------------- FIND -------------------

    @Test
    void findById_shouldReturnEntity_whenExists() {
        UUID id = UUID.randomUUID();
        Machine m = new Machine();
        when(repository.findById(id)).thenReturn(Optional.of(m));

        Machine out = service.findById(id);

        assertThat(out).isSameAs(m);
        verify(repository).findById(id);
    }

    @Test
    void findById_shouldThrowObjectNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ObjectNotFound.class);
    }

    // ------------------- DELETE -------------------

    @Test
    void deleteById_shouldValidate_andDelete_whenExists() {
        UUID id = UUID.randomUUID();
        Machine m = new Machine();
        m.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(m));
        doNothing().when(validator).validateDelete(m);

        service.deleteById(id);

        verify(validator).validateDelete(m);
        verify(repository).delete(m);
    }

    @Test
    void deleteById_shouldThrowObjectNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById(id))
                .isInstanceOf(ObjectNotFound.class);

        verify(repository).findById(id);
        verifyNoInteractions(validator);
        verify(repository, never()).delete(any(Machine.class));

    }

    // ------------------- SEARCH -------------------

    @Test
    void search_shouldClampPageAndPageSize_defaultsAndMax100() {
        SearchMachineDTO filters = mock(SearchMachineDTO.class);

        Page<Machine> page = new PageImpl<>(List.of(new Machine()));
        when(repository.findAll(
                any(Specification.class),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<Machine> out = service.search(filters, -1, 1000);

        assertThat(out).isSameAs(page);

        ArgumentCaptor<PageRequest> prCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(repository).findAll(any(Specification.class), prCaptor.capture());

        PageRequest pr = prCaptor.getValue();
        assertThat(pr.getPageNumber()).isEqualTo(0);
        assertThat(pr.getPageSize()).isEqualTo(100);
    }

    @Test
    void search_shouldUseDefaults_whenNulls() {
        SearchMachineDTO filters = mock(SearchMachineDTO.class);

        Page<Machine> page = new PageImpl<>(List.of());
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        service.search(filters, null, null);

        ArgumentCaptor<PageRequest> prCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(repository).findAll(any(Specification.class), prCaptor.capture());

        PageRequest pr = prCaptor.getValue();
        assertThat(pr.getPageNumber()).isEqualTo(0);
        assertThat(pr.getPageSize()).isEqualTo(10);
    }
}
