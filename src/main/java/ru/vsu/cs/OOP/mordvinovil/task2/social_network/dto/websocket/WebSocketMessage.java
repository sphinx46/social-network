package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {
    private String type;
    private T payload;
    private LocalDateTime dateTime;

    public static <T> WebSocketMessage<T> success(String type, T payload) {
        return WebSocketMessage.<T>builder()
                .type(type)
                .payload(payload)
                .dateTime(LocalDateTime.now())
                .build();

    }

    public static <T> WebSocketMessage<T> error(T payload) {
        return WebSocketMessage.<T>builder()
                .type("ERROR")
                .payload(payload)
                .dateTime(LocalDateTime.now())
                .build();
    }
}
