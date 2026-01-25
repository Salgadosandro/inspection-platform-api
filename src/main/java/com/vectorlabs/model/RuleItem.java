package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import java.util.UUID;

@Entity
@Table(
        name = "rule_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ruleitem_module_code",
                        columnNames = {"module_id", "item_code"}
                )
        },
        indexes = {
                @Index(name = "ix_ruleitem_module_sequence", columnList = "module_id, sequence"),
                @Index(name = "ix_ruleitem_parent", columnList = "parent_id"),
                @Index(name = "ix_ruleitem_item_code", columnList = "item_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RuleItem extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Módulo ao qual o item pertence (ex: 12.4) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private RuleModule module;

    /** Item pai (ex: 12.4.1 é pai de 12.4.1.1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RuleItem parent;

    /** Código do item na norma (ex: 12.4.1, 12.4.1.1) */
    @Column(name = "item_code", length = 50, nullable = false)
    private String itemCode;

    /** Texto do requisito / conteúdo normativo */
    @Column(name = "description", length = 8000, nullable = false)
    private String description;

    /** Ordem dentro do mesmo módulo ou do mesmo pai */
    @Builder.Default
    @Column(name = "sequence", nullable = false)
    private Integer sequence = 0;

    /** Ativo/inativo (soft versioning / controle) */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
