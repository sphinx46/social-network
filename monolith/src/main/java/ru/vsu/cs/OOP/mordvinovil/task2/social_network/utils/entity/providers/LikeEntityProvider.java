package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;

@Component
public interface LikeEntityProvider extends EntityProvider<Like, Long> {
}
