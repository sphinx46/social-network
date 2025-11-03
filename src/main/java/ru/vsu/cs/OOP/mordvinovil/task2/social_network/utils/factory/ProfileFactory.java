package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.profile.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

@Component
public class ProfileFactory {

    public Profile createProfile(User user, ProfileRequest request) {
        return Profile.builder()
                .user(user)
                .bio(request.getBio() != null ? request.getBio() : "")
                .city(request.getCity() != null ? request.getCity() : user.getCity())
                .dateOfBirth(request.getDateOfBirth())

                .imageUrl(request.getImageUrl())
                .build();
    }

    public Profile createDefaultProfile(User user) {
        return Profile.builder()
                .user(user)
                .bio("")
                .city(user.getCity())
                .dateOfBirth(null)
                .imageUrl(null)
                .build();
    }
}