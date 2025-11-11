package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.calculator;

public interface SocialInteractionCalculator {
    int calculateMutualFriendsCount(Long user1, Long user2);
    int calculateCommonLikesOnPostCount(Long user1, Long user2);
    int calculateCommonLikesOnCommentCount(Long user1, Long user2);
}