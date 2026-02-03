package com.vectorlabs.repository;

import com.vectorlabs.model.Machine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MachineRepositoryTest {

    @Autowired
    private MachineRepository repository;

    @Test
    @DisplayName("Deve retornar true quando existir máquina com mesmo type, manufacturer e model (ignorando case)")
    void existsByTypeManufacturerAndModel_ignoreCase_shouldReturnTrue() {
        // Arrange
        Machine machine = new Machine();
        // NÃO setar ID aqui
        machine.setType("Prensa");
        machine.setManufacturer("Siemens");
        machine.setModel("XPT-300");

        repository.saveAndFlush(machine);

        // Act
        boolean exists = repository
                .existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCase(
                        "prensa",
                        "SIEMENS",
                        "xpt-300"
                );

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir máquina com os dados informados")
    void existsByTypeManufacturerAndModel_shouldReturnFalse_whenNotExists() {
        // Arrange
        Machine machine = new Machine();
        machine.setType("Torno");
        machine.setManufacturer("Mazak");
        machine.setModel("QT-100");

        repository.saveAndFlush(machine);

        // Act
        boolean exists = repository
                .existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCase(
                        "Prensa",
                        "Siemens",
                        "XPT-300"
                );

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando base estiver vazia")
    void existsByTypeManufacturerAndModel_shouldReturnFalse_whenDatabaseIsEmpty() {
        boolean exists = repository
                .existsByTypeIgnoreCaseAndManufacturerIgnoreCaseAndModelIgnoreCase(
                        "Prensa",
                        "Siemens",
                        "XPT-300"
                );

        assertThat(exists).isFalse();
    }
}
