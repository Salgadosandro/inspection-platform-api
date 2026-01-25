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
        name = "rule_sections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rulesection_rule_code",
                        columnNames = {"rule_id", "code"}
                ),
                @UniqueConstraint(
                        name = "uk_rulesection_rule_sequence",
                        columnNames = {"rule_id", "sequence"}
                )
        },
        indexes = {
                @Index(name = "ix_rulesection_rule_sequence", columnList = "rule_id, sequence"),
                @Index(name = "ix_rulesection_rule_code", columnList = "rule_id, code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RuleSection extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @Column(name = "code", length = 30, nullable = false)
    private String code;

    @Column(name = "name", length = 400, nullable = false)
    private String name;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
