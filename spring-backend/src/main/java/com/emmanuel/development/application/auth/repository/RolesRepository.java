package com.emmanuel.development.application.auth.repository;

import com.emmanuel.development.application.auth.entity.CustomRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository extends JpaRepository<CustomRoles, Long> { }
