package com.fitg.user.model;

import com.fitg.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA only
public class UserEntity extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String displayName;

    private Integer dailyCalorieGoal; // nullable -> wrapper type, never a primitive

    @Column(nullable = false)
    private String timezone = "UTC"; // IANA zone id, e.g. "Asia/Kolkata"

    public UserEntity(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
