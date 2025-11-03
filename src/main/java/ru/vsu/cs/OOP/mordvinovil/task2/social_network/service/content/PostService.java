package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content;

import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.Service;

public interface PostService extends Service<PostRequest, User, PostResponse> {
    PostResponse create(PostRequest request, User user);
    PostResponse editPost(PostRequest request, Long id, User currentUser);
    PostResponse uploadImage(Long id, MultipartFile imageFile, User currentUser);
    PostResponse removeImage(Long id, User currentUser);
    PostResponse getPostById(Long postId);
    PageResponse<PostResponse> getAllPostsByUser(User user, PageRequest pageRequest);
}
