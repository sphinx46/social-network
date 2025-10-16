package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface ProfileEntityProvider extends EntityProvider<Profile, Long> {
    Profile getByUser(User user);
}
