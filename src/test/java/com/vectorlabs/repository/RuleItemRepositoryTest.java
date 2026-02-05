package com.vectorlabs.repository;

import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleItem;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.model.RuleSection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RuleItemRepositoryTest {

    @Autowired
    private RuleItemRepository repository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleSectionRepository sectionRepository;

    @Autowired
    private RuleModuleRepository moduleRepository;

    // ========================= existsByParent_Id =========================

    @Test
    void existsByParent_Id_shouldReturnTrue_whenChildExists() {
        RuleModule module = persistRuleGraphAndReturnModule();

        RuleItem parent = repository.save(newItem(module, null, "ITM-ROOT", "Root item", true));
        repository.save(newItem(module, parent, "ITM-CHILD", "Child item", true));

        boolean exists = repository.existsByParent_Id(parent.getId());

        assertTrue(exists);
    }

    @Test
    void existsByParent_Id_shouldReturnFalse_whenNoChildExists() {
        RuleModule module = persistRuleGraphAndReturnModule();

        RuleItem parent = repository.save(newItem(module, null, "ITM-ROOT", "Root item", true));

        boolean exists = repository.existsByParent_Id(parent.getId());

        assertFalse(exists);
    }

    // ========================= existsByModule_IdAndItemCode =========================

    @Test
    void existsByModule_IdAndItemCode_shouldReturnTrue_whenExists() {
        RuleModule module = persistRuleGraphAndReturnModule();
        repository.save(newItem(module, null, "ITM-01", "Item 1", true));

        boolean exists = repository.existsByModule_IdAndItemCode(module.getId(), "ITM-01");

        assertTrue(exists);
    }

    @Test
    void existsByModule_IdAndItemCode_shouldReturnFalse_whenNotExists() {
        RuleModule module = persistRuleGraphAndReturnModule();

        boolean exists = repository.existsByModule_IdAndItemCode(module.getId(), "NOPE");

        assertFalse(exists);
    }

    // ========================= existsByModule_IdAndItemCodeAndIdNot =========================

    @Test
    void existsByModule_IdAndItemCodeAndIdNot_shouldIgnoreSameId_andReturnFalse() {
        RuleModule module = persistRuleGraphAndReturnModule();

        RuleItem saved = repository.save(newItem(module, null, "ITM-01", "Item 1", true));

        boolean exists = repository.existsByModule_IdAndItemCodeAndIdNot(
                module.getId(), "ITM-01", saved.getId()
        );

        assertFalse(exists);
    }

    @Test
    void existsByModule_IdAndItemCodeAndIdNot_shouldDetectOtherId_andReturnTrue() {
        RuleModule module = persistRuleGraphAndReturnModule();

        RuleItem i1 = repository.save(newItem(module, null, "ITM-01", "Item 1", true));
        RuleItem i2 = repository.save(newItem(module, null, "ITM-02", "Item 2", true));

        boolean exists = repository.existsByModule_IdAndItemCodeAndIdNot(
                module.getId(), "ITM-01", i2.getId()
        );

        assertTrue(exists);
    }

    // ========================= HELPERS =========================

    private RuleModule persistRuleGraphAndReturnModule() {
        Rule rule = ruleRepository.save(newRule("NR12"));
        RuleSection section = sectionRepository.save(newSection(rule, "SEC-01", "Section 1", 1, true));
        return moduleRepository.save(newModule(section, "MOD-01", "Module 1", 1, true));
    }

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

    private RuleItem newItem(RuleModule module, RuleItem parent, String itemCode, String description, Boolean active) {
        RuleItem i = new RuleItem();

        // ✅ Ajuste conforme seu entity RuleItem:
        i.setModule(module);
        i.setParent(parent);
        i.setItemCode(itemCode);
        i.setDescription(description);
        i.setActive(active);

        return i;
    }
}
