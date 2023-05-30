package com.emmanuel.development.application.image.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table @Entity @NoArgsConstructor
@Setter @Getter
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "image_name", nullable = false)
    private String name;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "media_type", nullable = false)
    private String image_type;

}
