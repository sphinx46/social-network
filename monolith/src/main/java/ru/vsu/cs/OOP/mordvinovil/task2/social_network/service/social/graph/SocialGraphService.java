package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.graph;

import java.util.Map;

public interface SocialGraphService {
    Map<Long, Double> findSocialDistances(Long startUserId, int maxDepth);
}