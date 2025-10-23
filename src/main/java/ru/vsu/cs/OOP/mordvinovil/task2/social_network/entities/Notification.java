package ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.converters.MapToJsonConverter;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = User.class)
    @JoinColumn(name = "userAction_id", referencedColumnName = "id")
    private User userAction;

    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "additional_data", length = 2000)
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> additionalData;

    @Column(name = "time_create", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "time_update", nullable = false)
    private LocalDateTime updatedAt;
}










