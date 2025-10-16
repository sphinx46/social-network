package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.RelationshipEntityProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RelationshipEntityProviderImpl implements RelationshipEntityProvider {

    private final RelationshipRepository relationshipRepository;

    @Override
    public Relationship getById(Long id) {
        return relationshipRepository.findById(id)
                .orElseThrow(() -> new RelationshipNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    @Override
    public Optional<Relationship> findById(Long id) {
        return relationshipRepository.findById(id);
    }
}