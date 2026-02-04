package com.vectorlabs.repository;

import com.vectorlabs.model.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RuleRepositoryTest {

    @Autowired
    private RuleRepository repository;

    private Rule buildRule(String code) {
        Rule rule = new Rule();
        // NÃO setar id aqui!
        rule.setCode(code);
        rule.setTitle("NR Teste");
        rule.setDescription("Descrição teste");
        rule.setActive(true);
        rule.setDeleted(false);
        return rule;
    }


    @Test
    void existsByCodeIgnoreCase_shouldReturnTrue_whenSameCase() {
        Rule rule = buildRule("NR-12");
        repository.saveAndFlush(rule);

        boolean exists = repository.existsByCodeIgnoreCase("NR-12");

        assertThat(exists).isTrue();
    }


    @Test
    void existsByCodeIgnoreCase_shouldReturnTrue_whenDifferentCase() {
        Rule rule = buildRule("NR-13");
        repository.save(rule);

        boolean existsLower = repository.existsByCodeIgnoreCase("nr-13");
        boolean existsUpper = repository.existsByCodeIgnoreCase("NR-13");

        assertThat(existsLower).isTrue();
        assertThat(existsUpper).isTrue();
    }

    @Test
    void existsByCodeIgnoreCase_shouldReturnFalse_whenCodeDoesNotExist() {
        boolean exists = repository.existsByCodeIgnoreCase("NR-99");

        assertThat(exists).isFalse();
    }
}
