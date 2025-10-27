package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.PostService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.PostValidator;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final PostRepository postRepository;
    private final EntityMapper entityMapper;
    private final ContentFactory contentFactory;
    private final PostValidator postValidator;
    private final EntityUtils entityUtils;

    @Transactional
    @Override
    public PostResponse create(PostRequest request, User user) {
        postValidator.validate(request, user);

        Post post = contentFactory.createPost(user, request.getContent(), request.getImageUrl());
        Post savedPost = postRepository.save(post);

        return entityMapper.map(savedPost, PostResponse.class);
    }

    @Override
    public PageResponse<PostResponse> getAllPostsByUser(User user, PageRequest pageRequest) {
        Page<Post> posts =
                postRepository.getAllPostsByUserWithCommentsAndLikes(user, pageRequest.toPageable());
        return PageResponse.of(posts.map(
                post -> entityMapper.mapWithName(post, PostResponse.class, "withDetails")
        ));
    }

    @Transactional
    @Override
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
    @Override
    public PostResponse uploadImage(Long id, MultipartFile imageFile, User currentUser) {
        fileStorageServiceImpl.validateImageFile(imageFile);
        postValidator.validatePostOwnership(id, currentUser);

        Post post = entityUtils.getPost(id);

        if (post.getImageUrl() != null) {
            fileStorageServiceImpl.deleteFile(post.getImageUrl());
        }

        String imageUrl = fileStorageServiceImpl.savePostImage(imageFile, id);
        post.setImageUrl(imageUrl);
        Post updatedPost = postRepository.save(post);

        return entityMapper.map(updatedPost, PostResponse.class);
    }

    @Transactional
    @Override
    public PostResponse removeImage(Long id, User currentUser) {
        postValidator.validatePostOwnership(id, currentUser);

        Post post = entityUtils.getPost(id);

        if (post.getImageUrl() != null) {
            fileStorageServiceImpl.deleteFile(post.getImageUrl());
        }

        post.setImageUrl(null);
        Post updatedPost = postRepository.save(post);

        return entityMapper.map(updatedPost, PostResponse.class);
    }

    @Override
    public PostResponse getPostById(Long postId) {
        Post post = entityUtils.getPost(postId);
        return entityMapper.map(post, PostResponse.class);
    }
}