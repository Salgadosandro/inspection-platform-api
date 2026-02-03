package com.vectorlabs.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.vectorlabs.dto.machine.AnswerMachineDTO;
import com.vectorlabs.dto.machine.RegisterMachineDTO;
import com.vectorlabs.dto.machine.SearchMachineDTO;
import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.mapper.MachineMapper;
import com.vectorlabs.model.Machine;
import com.vectorlabs.service.MachineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineControllerTest {

    @Mock
    private MachineService service;

    @Mock
    private MachineMapper mapper;

    @InjectMocks
    private MachineController controller;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void create_shouldReturn201Created_withLocationHeaderAndBody() {
        // Arrange: precisa de request context por causa do ServletUriComponentsBuilder.fromCurrentRequest()
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/machines");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UUID id = UUID.randomUUID();

        RegisterMachineDTO inDto = mock(RegisterMachineDTO.class);

        Machine entity = new Machine();
        Machine saved = new Machine();
        // se Machine tiver setId(UUID), descomente:
        // saved.setId(id);

        // Como não sabemos se Machine tem setter, garantimos via spy/mock também:
        // (mas vamos assumir que getId() existe e retorna o id)
        saved = spy(saved);
        doReturn(id).when(saved).getId();

        AnswerMachineDTO outDto = mock(AnswerMachineDTO.class);

        when(mapper.toEntity(inDto)).thenReturn(entity);
        when(service.save(entity)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(outDto);

        // Act
        ResponseEntity<AnswerMachineDTO> response = controller.create(inDto);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertSame(outDto, response.getBody());

        URI location = response.getHeaders().getLocation();
        assertNotNull(location);
        assertTrue(location.toString().endsWith("/api/machines/" + id));

        verify(mapper).toEntity(inDto);
        verify(service).save(entity);
        verify(mapper).toDTO(saved);
        verifyNoMoreInteractions(service, mapper);
    }

    @Test
    void update_shouldReturn200Ok_withMappedBody() {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateMachineDTO inDto = mock(UpdateMachineDTO.class);

        Machine updated = new Machine();
        AnswerMachineDTO outDto = mock(AnswerMachineDTO.class);

        when(service.update(id, inDto)).thenReturn(updated);
        when(mapper.toDTO(updated)).thenReturn(outDto);

        // Act
        ResponseEntity<AnswerMachineDTO> response = controller.update(id, inDto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertSame(outDto, response.getBody());

        verify(service).update(id, inDto);
        verify(mapper).toDTO(updated);
        verifyNoMoreInteractions(service, mapper);
    }

    @Test
    void details_shouldReturn200Ok_withMappedBody() {
        // Arrange
        UUID id = UUID.randomUUID();
        Machine entity = new Machine();
        AnswerMachineDTO outDto = mock(AnswerMachineDTO.class);

        when(service.findById(id)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(outDto);

        // Act
        ResponseEntity<AnswerMachineDTO> response = controller.details(id);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertSame(outDto, response.getBody());

        verify(service).findById(id);
        verify(mapper).toDTO(entity);
        verifyNoMoreInteractions(service, mapper);
    }

    @Test
    void delete_shouldReturn204NoContent() {
        // Arrange
        UUID id = UUID.randomUUID();
        doNothing().when(service).deleteById(id);

        // Act
        ResponseEntity<Void> response = controller.delete(id);

        // Assert
        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(service).deleteById(id);
        verifyNoMoreInteractions(service, mapper);
        verifyNoInteractions(mapper);
    }

    @Test
    void list_shouldReturn200Ok_withMappedPage() {
        // Arrange
        SearchMachineDTO filters = mock(SearchMachineDTO.class);
        int page = 0;
        int pageSize = 10;

        Machine m1 = new Machine();
        Machine m2 = new Machine();
        Page<Machine> pageResult = new PageImpl<>(List.of(m1, m2)); // totalElements = 2

        AnswerMachineDTO d1 = mock(AnswerMachineDTO.class);
        AnswerMachineDTO d2 = mock(AnswerMachineDTO.class);

        when(service.search(filters, page, pageSize)).thenReturn(pageResult);
        when(mapper.toDTO(m1)).thenReturn(d1);
        when(mapper.toDTO(m2)).thenReturn(d2);

        // Act
        ResponseEntity<Page<AnswerMachineDTO>> response = controller.list(filters, page, pageSize);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(2, response.getBody().getContent().size());
        assertSame(d1, response.getBody().getContent().get(0));
        assertSame(d2, response.getBody().getContent().get(1));

        verify(service).search(filters, page, pageSize);
        verify(mapper).toDTO(m1);
        verify(mapper).toDTO(m2);
        verifyNoMoreInteractions(service, mapper);
    }
}
