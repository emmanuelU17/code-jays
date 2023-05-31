package com.emmanuel.development.application.image.repository;

import com.emmanuel.development.application.image.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
    @Query("SELECT COUNT (i.id) FROM ImageEntity i")
    int total();
}
