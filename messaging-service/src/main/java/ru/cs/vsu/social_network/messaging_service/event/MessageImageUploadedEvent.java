package ru.cs.vsu.social_network.messaging_service.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageImageUploadedEvent {
    private UUID eventId;
    private final String eventType = "MESSAGE_IMAGE_UPLOADED";
    private LocalDateTime eventTimeStamp;

    private UUID ownerId;
    private UUID messageId;
    private String publicUrl;
    private String objectName;
    private String mimeType;
    private Long size;
    private String description;
    private String originalFileName;
}
