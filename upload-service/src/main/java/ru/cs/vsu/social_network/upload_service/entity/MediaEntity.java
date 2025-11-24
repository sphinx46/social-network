package ru.cs.vsu.social_network.upload_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaEntity extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "object_name", nullable = false, unique = true, length = 255)
    private String objectName;

    @Column(name = "file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "mime_type", nullable = false, length = 128)
    private String mimeType;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "bucket_name", nullable = false, length = 128)
    private String bucketName;

    @Column(name = "public_url", nullable = false)
    private String publicUrl;

    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 1L;
}
