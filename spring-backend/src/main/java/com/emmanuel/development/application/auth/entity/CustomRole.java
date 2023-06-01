package com.emmanuel.development.application.auth.entity;

import com.emmanuel.development.application.enumeration.RoleEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table
@Entity
@NoArgsConstructor
@Getter
@Setter
public class CustomRole implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "role_id", updatable = false, nullable = false)
    private Long roleID;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private RoleEnum roleEnum;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false,
            referencedColumnName = "user_id"
    )
    private AppUser appUser;

    public CustomRole(RoleEnum roleEnum) {
        this.roleEnum = roleEnum;
    }

}
