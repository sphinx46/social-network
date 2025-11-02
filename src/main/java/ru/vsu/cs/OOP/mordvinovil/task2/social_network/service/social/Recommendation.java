package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public final class Recommendation {
    private final Long recommendedUserId;
    private final Double score;
    private final LocalDateTime createdAt;
}
