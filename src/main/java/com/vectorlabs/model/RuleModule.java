package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(
        name = "rule_modules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rulemodule_section_code",
                        columnNames = {"section_id", "module_code"}
                )
        },
        indexes = {
                @Index(
                        name = "ix_rulemodule_section_sequence",
                        columnList = "section_id, module_sequence"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RuleModule extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Seção / Anexo da norma (já versionado por Rule)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private RuleSection section;

    /**
     * Código do módulo dentro da seção
     * Ex: 12.1, 12.2, 3.4, etc.
     */
    @Column(name = "module_code", length = 50, nullable = false)
    private String moduleCode;

    /**
     * Título do módulo conforme a norma
     */
    @Column(name = "module_name", length = 300, nullable = false)
    private String moduleName;

    /**
     * Ordem do módulo dentro da seção
     */
    @Column(name = "module_sequence", nullable = false)
    private Integer moduleSequence;

    /**
     * Controle operacional (não versionamento)
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
