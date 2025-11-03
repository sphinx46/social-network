package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils.CommentsCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils.LikesCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils.MutualFriendsCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.ProfileAgeCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionStrengthCalculatorImpl implements ConnectionStrengthCalculator {
    private final MutualFriendsCalculator mutualFriendsCalculator;
    private final LikesCalculator likesCalculator;
    private final CommentsCalculator commentsCalculator;
    private final EntityUtils entityUtils;
    private final ProfileAgeCalculator ageCalculator;
    private final MessageRepository messageRepository;

    @Override
    public Map<String, Double> resultConnectionStrength(Long userId, Long targetUserId) {
        log.info("Calculating connection strength between user {} and user {}", userId, targetUserId);

        Map<String, Double> result = new HashMap<>();

        int commonLikes = likesCalculator.calculateCommonLikesCount(userId, targetUserId);
        double commonLikesScore = Math.min(commonLikes / 10.0, 1.0) * 0.25;
        result.put("commonLikes", commonLikesScore);
        log.debug("Common likes score: {} (count: {})", commonLikesScore, commonLikes);

        int commonComments = commentsCalculator.calculateCommonCommentsCount(userId, targetUserId);
        double commonCommentsScore = Math.min(commonComments / 10.0, 1.0) * 0.18;
        result.put("commonComments", commonCommentsScore);
        log.debug("Common comments score: {} (count: {})", commonCommentsScore, commonCommentsScore);

        int commonFriends = mutualFriendsCalculator.calculateMutualFriendsCount(userId, targetUserId);
        double friendsScore = Math.min(commonFriends / 5.0, 1.0) * 0.6;
        result.put("commonFriends", friendsScore);
        log.debug("Common friends score: {} (count: {})", friendsScore, commonFriends);

        double commonCityScore = haveCommonCity(userId, targetUserId) ? 0.15 : 0;
        result.put("commonCity", commonCityScore);
        log.debug("Common city score: {}", commonCityScore);

        double similarAgeScore = haveSimilarAge(userId, targetUserId) ? 0.13 : 0;
        result.put("similarAge", similarAgeScore);
        log.debug("Similar age score: {}", similarAgeScore);

        boolean commonMessages = messageRepository.existsConversationBetweenUsers(userId, targetUserId);
        double commonMessagesScore = commonMessages ? 0.4 : 0;
        result.put("commonMessages", commonMessagesScore);
        log.debug("commonMessages score: {}", commonMessagesScore);


        double totalStrength = result.values().stream().mapToDouble(Double::doubleValue).sum();
        log.info("Total connection strength between user {} and user {}: {}", userId, targetUserId, totalStrength);

        return result;
    }

    @Override
    public Double calculateConnectionStrength(Long userId, Long targetUserId) {
        Map<String, Double> strengthMap = resultConnectionStrength(userId, targetUserId);
        double calculatedStrength = strengthMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double finalStrength = Math.max(0.1, calculatedStrength);

        log.debug("Final connection strength: {}", finalStrength);
        return finalStrength;
    }

    private boolean haveCommonCity(Long userId, Long targetUserId) {
        Profile user1 = entityUtils.getProfileByUserId(userId);
        Profile user2 = entityUtils.getProfileByUserId(targetUserId);

        return user1.getCity().equals(user2.getCity());
    }

    private boolean haveSimilarAge(Long userId, Long targetUserId) {
        Profile user1 = entityUtils.getProfileByUserId(userId);
        Profile user2 = entityUtils.getProfileByUserId(targetUserId);

        if (user1.getDateOfBirth() == null || user2.getDateOfBirth() == null) {
            return false;
        }

        Integer ageUser1 = ageCalculator.calculateAge(user1.getDateOfBirth());
        Integer ageUser2 = ageCalculator.calculateAge(user2.getDateOfBirth());

        return Math.abs(ageUser1 - ageUser2) <= 5;
    }
}