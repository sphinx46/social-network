package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import lombok.Data;

import java.util.Objects;

@Data
public final class SocialNode implements Comparable<SocialNode> {
    private final Long userId;
    private final Double distance;

    public SocialNode(Long userId, Double distance) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.distance = Objects.requireNonNull(distance, "Distance cannot be null");
    }

    @Override
    public int compareTo(SocialNode other) {
        return Double.compare(this.distance, other.distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocialNode that = (SocialNode) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(distance, that.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, distance);
    }
}
