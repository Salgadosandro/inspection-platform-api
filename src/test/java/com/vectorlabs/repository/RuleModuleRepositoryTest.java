package com.vectorlabs.repository;

import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.model.RuleSection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RuleModuleRepositoryTest {

    @Autowired
    private RuleModuleRepository repository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleSectionRepository sectionRepository;

    // ========================= existsBySection_IdAndModuleCode =========================

    @Test
    void existsBySection_IdAndModuleCode_shouldReturnTrue_whenExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        repository.save(newModule(section, "MOD-01", "Module 1", 1, true));

        boolean exists = repository.existsBySection_IdAndModuleCode(section.getId(), "MOD-01");

        assertTrue(exists);
    }

    @Test
    void existsBySection_IdAndModuleCode_shouldReturnFalse_whenNotExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));

        boolean exists = repository.existsBySection_IdAndModuleCode(section.getId(), "NOPE");

        assertFalse(exists);
    }

    // ========================= existsBySection_IdAndModuleSequence =========================

    @Test
    void existsBySection_IdAndModuleSequence_shouldReturnTrue_whenExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        repository.save(newModule(section, "MOD-01", "Module 1", 10, true));

        boolean exists = repository.existsBySection_IdAndModuleSequence(section.getId(), 10);

        assertTrue(exists);
    }

    @Test
    void existsBySection_IdAndModuleSequence_shouldReturnFalse_whenNotExists() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));

        boolean exists = repository.existsBySection_IdAndModuleSequence(section.getId(), 999);

        assertFalse(exists);
    }

    // ========================= existsBySection_IdAndModuleCodeAndIdNot =========================

    @Test
    void existsBySection_IdAndModuleCodeAndIdNot_shouldIgnoreSameId_andReturnFalse() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        RuleModule saved = repository.save(newModule(section, "MOD-01", "Module 1", 1, true));

        boolean exists = repository.existsBySection_IdAndModuleCodeAndIdNot(
                section.getId(), "MOD-01", saved.getId()
        );

        assertFalse(exists);
    }

    @Test
    void existsBySection_IdAndModuleCodeAndIdNot_shouldDetectOtherId_andReturnTrue() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        RuleModule m1 = repository.save(newModule(section, "MOD-01", "Module 1", 1, true));
        RuleModule m2 = repository.save(newModule(section, "MOD-02", "Module 2", 2, true));

        boolean exists = repository.existsBySection_IdAndModuleCodeAndIdNot(
                section.getId(), "MOD-01", m2.getId()
        );

        assertTrue(exists);
    }

    // ========================= existsBySection_IdAndModuleSequenceAndIdNot =========================

    @Test
    void existsBySection_IdAndModuleSequenceAndIdNot_shouldIgnoreSameId_andReturnFalse() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        RuleModule saved = repository.save(newModule(section, "MOD-01", "Module 1", 7, true));

        boolean exists = repository.existsBySection_IdAndModuleSequenceAndIdNot(
                section.getId(), 7, saved.getId()
        );

        assertFalse(exists);
    }

    @Test
    void existsBySection_IdAndModuleSequenceAndIdNot_shouldDetectOtherId_andReturnTrue() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        RuleModule m1 = repository.save(newModule(section, "MOD-01", "Module 1", 7, true));
        RuleModule m2 = repository.save(newModule(section, "MOD-02", "Module 2", 8, true));

        boolean exists = repository.existsBySection_IdAndModuleSequenceAndIdNot(
                section.getId(), 7, m2.getId()
        );

        assertTrue(exists);
    }

    // ========================= existsBySection_Id =========================

    @Test
    void existsBySection_Id_shouldReturnTrue_whenThereIsAnyModuleForSection() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        repository.save(newModule(section, "MOD-01", "Module 1", 1, true));

        boolean exists = repository.existsBySection_Id(section.getId());

        assertTrue(exists);
    }

    @Test
    void existsBySection_Id_shouldReturnFalse_whenNoModuleForSection() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));

        boolean exists = repository.existsBySection_Id(section.getId());

        assertFalse(exists);
    }

    // ========================= HELPERS =========================

    private Rule newRule(String code) {
        Rule r = new Rule();
        // ajuste se tiver obrigatórios:
        r.setCode(code);
        r.setTitle("Rule " + code);
        r.setActive(true);
        return r;
    }

    private RuleSection newSection(Rule rule, String code, String name, Integer sequence, Boolean active) {
        RuleSection s = new RuleSection();
        // ajuste se tiver obrigatórios:
        s.setRule(rule);
        s.setCode(code);
        s.setName(name);
        s.setSequence(sequence);
        s.setActive(active);
        return s;
    }

    private RuleModule newModule(RuleSection section, String moduleCode, String moduleName, Integer moduleSequence, Boolean active) {
        RuleModule m = new RuleModule();
        // ajuste se tiver obrigatórios:
        m.setSection(section);
        m.setModuleCode(moduleCode);
        m.setModuleName(moduleName);
        m.setModuleSequence(moduleSequence);
        m.setActive(active);
        return m;
    }
}
