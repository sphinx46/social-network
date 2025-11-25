package ru.cs.vsu.social_network.user_profile_service.events;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvatarUploadedEvent {
    private UUID eventId;
    private final String eventType = "AVATAR_UPLOADED";
    private LocalDateTime eventTimeStamp;

    private UUID ownerId;
    private String publicUrl;
    private String objectName;
    private String mimeType;
    private Long size;
    private String description;
    private String originalFileName;
}
