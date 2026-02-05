package com.vectorlabs.repository;

import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleSection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RuleSectionRepositoryTest {

    @Autowired
    private RuleSectionRepository repository;

    @Autowired
    private RuleRepository ruleRepository; // se você tiver RuleRepository
    // Se você NÃO tiver RuleRepository, dá pra persistir Rule via EntityManager/TestEntityManager.
    // Mas na maioria dos projetos você tem.

    @Test
    void existsByRule_IdAndSequence_shouldReturnTrue_whenExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        repository.save(newSection(rule, "SEC-01", "Section 1", 1, true));

        boolean exists = repository.existsByRule_IdAndSequence(rule.getId(), 1);

        assertTrue(exists);
    }

    @Test
    void existsByRule_IdAndSequence_shouldReturnFalse_whenNotExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));

        boolean exists = repository.existsByRule_IdAndSequence(rule.getId(), 99);

        assertFalse(exists);
    }

    @Test
    void existsByRule_IdAndSequenceAndIdNot_shouldIgnoreSameId_andReturnFalse() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection saved = repository.save(newSection(rule, "SEC-01", "Section 1", 1, true));

        boolean exists = repository.existsByRule_IdAndSequenceAndIdNot(rule.getId(), 1, saved.getId());

        // existe registro com sequence=1, mas é o MESMO id, então deve ignorar => false
        assertFalse(exists);
    }

    @Test
    void existsByRule_IdAndSequenceAndIdNot_shouldDetectOtherId_andReturnTrue() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection s1 = repository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        RuleSection s2 = repository.save(newSection(rule, "SEC-02", "Section 2", 2, true));

        boolean exists = repository.existsByRule_IdAndSequenceAndIdNot(rule.getId(), 1, s2.getId());

        // sequence=1 existe em outro registro (s1), e estamos excluindo o id do s2 => true
        assertTrue(exists);
    }

    @Test
    void existsByRule_IdAndCode_shouldReturnTrue_whenExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        repository.save(newSection(rule, "SEC-01", "Section 1", 1, true));

        boolean exists = repository.existsByRule_IdAndCode(rule.getId(), "SEC-01");

        assertTrue(exists);
    }

    @Test
    void existsByRule_IdAndCode_shouldReturnFalse_whenNotExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));

        boolean exists = repository.existsByRule_IdAndCode(rule.getId(), "NOPE");

        assertFalse(exists);
    }

    @Test
    void existsByRule_IdAndCodeAndIdNot_shouldIgnoreSameId_andReturnFalse() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection saved = repository.save(newSection(rule, "SEC-01", "Section 1", 1, true));

        boolean exists = repository.existsByRule_IdAndCodeAndIdNot(rule.getId(), "SEC-01", saved.getId());

        assertFalse(exists);
    }

    @Test
    void existsByRule_IdAndCodeAndIdNot_shouldDetectOtherId_andReturnTrue() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection s1 = repository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        RuleSection s2 = repository.save(newSection(rule, "SEC-02", "Section 2", 2, true));

        boolean exists = repository.existsByRule_IdAndCodeAndIdNot(rule.getId(), "SEC-01", s2.getId());

        assertTrue(exists);
    }

    // ========================= HELPERS =========================

    private Rule newRule(String code) {
        Rule r = new Rule();

        // ✅ Ajuste conforme seu entity Rule:
        // - se tiver campos obrigatórios (title, active, etc), preencha aqui
        r.setCode(code);
        r.setTitle("Rule " + code);
        r.setActive(true);

        return r;
    }

    private RuleSection newSection(Rule rule, String code, String name, Integer sequence, Boolean active) {
        RuleSection s = new RuleSection();

        // ✅ Ajuste conforme seu entity RuleSection:
        s.setRule(rule);
        s.setCode(code);
        s.setName(name);
        s.setSequence(sequence);
        s.setActive(active);

        return s;
    }
}
