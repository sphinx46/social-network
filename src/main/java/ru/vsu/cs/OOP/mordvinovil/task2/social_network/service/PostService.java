package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.List;

public interface PostService extends Service<PostRequest, User, PostResponse> {
    PostResponse create(PostRequest request, User user);
    List<PostResponse> getAllPostsByUser(User user);
    PostResponse editPost(PostRequest request, Long id, User currentUser);
    PostResponse uploadImage(Long id, MultipartFile imageFile, User currentUser);
    PostResponse removeImage(Long id, User currentUser);
    PostResponse getPostById(Long postId);
}
