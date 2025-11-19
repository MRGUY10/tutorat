package com.iiil.tutoring.entity;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Junction table for User-Role many-to-many relationship
 */
@Table("user_roles")
public class UserRole {

    @Id
    private Long id;

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    @Column("user_id")
    private Long userId;

    @NotNull(message = "L'ID du rôle est obligatoire")
    @Column("role_id")
    private Long roleId;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Version
    private Long version;

    // Constructors
    public UserRole() {}

    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + id +
                ", userId=" + userId +
                ", roleId=" + roleId +
                '}';
    }
}