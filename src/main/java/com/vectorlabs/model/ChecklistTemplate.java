package com.vectorlabs.model;

import com.vectorlabs.model.bases.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "checklist_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChecklistTemplate extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}