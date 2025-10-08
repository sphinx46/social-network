package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final FileStorageService fileStorageService;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PostResponse create(User user, PostRequest request) {
        var post = Post.builder()
                .user(user)
                .content(request.getContent())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl() : null)
                .time(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, PostResponse.class);
    }

    public List<PostResponse> getAllPostsByUser(User user) {
        List<Post> posts = postRepository.getAllPostsByUser(user);
        return posts.stream()
                .map(post -> modelMapper.map(post, PostResponse.class))
                .toList();
    }

    @Transactional
    public PostResponse editPost(PostRequest request, Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        Post updatedPost = postRepository.save(post);
        return modelMapper.map(updatedPost, PostResponse.class);
    }

    @Transactional
    public PostResponse uploadImage(Long id, MultipartFile imageFile, User currentUser) {
        fileStorageService.validateImageFile(imageFile);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        if (post.getImageUrl() != null) {
            fileStorageService.deleteFile(post.getImageUrl());
        }

        String imageUrl = fileStorageService.savePostImage(imageFile, id);
        post.setImageUrl(imageUrl);
        Post updatedPost = postRepository.save(post);

        return modelMapper.map(updatedPost, PostResponse.class);
    }

    @Transactional
    public PostResponse removeImage(Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        post.setImageUrl(null);
        Post updatedPost = postRepository.save(post);

        return modelMapper.map(updatedPost, PostResponse.class);
    }

    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));

        return modelMapper.map(post, PostResponse.class);
    }
}