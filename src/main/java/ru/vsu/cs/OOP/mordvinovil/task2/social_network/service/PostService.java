package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.PostValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final FileStorageService fileStorageService;
    private final PostRepository postRepository;
    private final EntityMapper entityMapper;
    private final ContentFactory contentFactory;
    private final PostValidator postValidator;
    private final EntityUtils entityUtils;

    @Transactional
    public PostResponse create(User user, PostRequest request) {
        postValidator.validate(request, user);

        Post post = contentFactory.createPost(user, request.getContent(), request.getImageUrl());
        Post savedPost = postRepository.save(post);

        return entityMapper.map(savedPost, PostResponse.class);
    }

    public List<PostResponse> getAllPostsByUser(User user) {
        List<Post> posts = postRepository.getAllPostsByUser(user);
        return entityMapper.mapList(posts, PostResponse.class);
    }

    @Transactional
    public PostResponse editPost(PostRequest request, Long id, User currentUser) {
        postValidator.validatePostUpdate(request, id, currentUser);

        Post post = entityUtils.getPost(id);

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        Post updatedPost = postRepository.save(post);
        return entityMapper.map(updatedPost, PostResponse.class);
    }

    @Transactional
    public PostResponse uploadImage(Long id, MultipartFile imageFile, User currentUser) {
        fileStorageService.validateImageFile(imageFile);
        postValidator.validatePostOwnership(id, currentUser);

        Post post = entityUtils.getPost(id);

        if (post.getImageUrl() != null) {
            fileStorageService.deleteFile(post.getImageUrl());
        }

        String imageUrl = fileStorageService.savePostImage(imageFile, id);
        post.setImageUrl(imageUrl);
        Post updatedPost = postRepository.save(post);

        return entityMapper.map(updatedPost, PostResponse.class);
    }

    @Transactional
    public PostResponse removeImage(Long id, User currentUser) {
        postValidator.validatePostOwnership(id, currentUser);

        Post post = entityUtils.getPost(id);

        if (post.getImageUrl() != null) {
            fileStorageService.deleteFile(post.getImageUrl());
        }

        post.setImageUrl(null);
        Post updatedPost = postRepository.save(post);

        return entityMapper.map(updatedPost, PostResponse.class);
    }

    public PostResponse getPostById(Long postId) {
        Post post = entityUtils.getPost(postId);
        return entityMapper.map(post, PostResponse.class);
    }
}