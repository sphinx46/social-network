package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.post.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.security.AccessDeniedException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final FileStorageService fileStorageService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PostResponse create(User user, PostRequest request) {
        var post = Post.builder()
                .user(user)
                .content(request.getContent())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl() : null)
                .time(LocalDate.now())
                .build();

        Post savedPost = postRepository.save(post);
        PostResponse response = modelMapper.map(savedPost, PostResponse.class);
        response.setUsername(user.getUsername());
        return response;
    }

    public List<PostResponse> getAllPostsByUser(User user) {
        List<Post> posts = postRepository.getAllPostsByUser(user);
        return posts.stream()
                .map(post -> {
                    PostResponse response = modelMapper.map(post, PostResponse.class);
                    response.setUsername(post.getUser().getUsername());
                    return response;
                })
                .toList();
    }

    @Transactional
    public PostResponse editPost(PostRequest request, Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Пост не найден"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("У вас нет прав для редактирования этого поста");
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        Post updatedPost = postRepository.save(post);
        PostResponse response = modelMapper.map(updatedPost, PostResponse.class);
        response.setUsername(post.getUser().getUsername());
        return response;
    }


    @Transactional
    public PostResponse uploadImage(Long id, MultipartFile imageFile, User currentUser) throws FileUploadException {
        try {
            fileStorageService.validateImageFile(imageFile);

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new PostNotFoundException("Пост не найден"));

            if (!post.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("У вас нет прав для изменения этого поста");
            }

            if (post.getImageUrl() != null) {
                fileStorageService.deleteFile(post.getImageUrl());
            }

            String imageUrl = fileStorageService.saveFile(imageFile, "post-images");
            post.setImageUrl(imageUrl);
            Post updatedPost = postRepository.save(post);

            PostResponse response = modelMapper.map(updatedPost, PostResponse.class);
            response.setUsername(post.getUser().getUsername());
            return response;
        } catch (IOException e) {
            throw new FileUploadException("Ошибка при сохранении файла", e);
        }
    }

    @Transactional
    public PostResponse removeImage(Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Пост не найден"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("У вас нет прав для изменения этого поста");
        }

        post.setImageUrl(null);
        Post updatedPost = postRepository.save(post);

        PostResponse response = modelMapper.map(updatedPost, PostResponse.class);
        response.setUsername(post.getUser().getUsername());
        return response;
    }

    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Пост не найден"));

        PostResponse response = modelMapper.map(post, PostResponse.class);
        response.setUsername(post.getUser().getUsername());
        return response;
    }
}
