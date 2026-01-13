package com.msa.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "boards")
public class Board {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Board() {
    }

    public Board(String name, BoardVisibility visibility) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.visibility = visibility;
        this.status = BoardStatus.ACTIVE;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String name, BoardVisibility visibility, BoardStatus status) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BoardVisibility getVisibility() {
        return visibility;
    }

    public BoardStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
