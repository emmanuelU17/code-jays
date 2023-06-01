package com.emmanuel.development.application.auth.repository;

import com.emmanuel.development.application.auth.entity.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @Query("SELECT a FROM AppUser a WHERE a.email = :email")
    Optional<AppUser> findByPrincipal(@Param(value = "email") String email);

    @Query("SELECT COUNT (a.email) FROM AppUser a WHERE a.email = :email")
    int principalExists(@Param(value = "email") String email);

    @Query("SELECT a.profilePicture FROM AppUser a WHERE a.email = :email")
    String getProfilePicture(@Param(value = "email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE AppUser a SET a.profilePicture = :path WHERE a.email = :email")
    void update_profile_picture(@Param(value = "email") String email, @Param(value = "path") String path);

    @Modifying
    @Transactional
    @Query("UPDATE AppUser a SET a.password = :password WHERE a.email = :email")
    void updatePassword(@Param(value = "email") String email, @Param(value = "password") String password);

}
