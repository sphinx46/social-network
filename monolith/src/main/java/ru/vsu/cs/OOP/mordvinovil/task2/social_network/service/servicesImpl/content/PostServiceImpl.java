package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.CacheEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.PostService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.storage.FileStorageServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.PostValidator;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final PostRepository postRepository;
    private final EntityMapper entityMapper;
    private final ContentFactory contentFactory;
    private final PostValidator postValidator;
    private final EntityUtils entityUtils;
    private final CacheEventPublisherService cacheEventPublisherService;
    private final CentralLogger centralLogger;

    /**
     * Создает новый пост
     *
     * @param request запрос на создание поста
     * @param user пользователь, создающий пост
     * @return ответ с созданным постом
     */
    @Transactional
    @Override
    public PostResponse create(PostRequest request, User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("contentLength", request.getContent() != null ? request.getContent().length() : 0);
        context.put("hasImage", request.getImageUrl() != null);

        centralLogger.logInfo("ПОСТ_СОЗДАНИЕ",
                "Создание нового поста", context);

        try {
            postValidator.validate(request, user);

            Post post = contentFactory.createPost(user, request.getContent(), request.getImageUrl());
            Post savedPost = postRepository.save(post);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("postId", savedPost.getId());

            centralLogger.logInfo("ПОСТ_СОЗДАН",
                    "Пост успешно создан", successContext);

            cacheEventPublisherService.publishPostCreate(this, savedPost, savedPost.getId());
            return entityMapper.map(savedPost, PostResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании поста", context, e);
            throw e;
        }
    }

    /**
     * Получает все посты пользователя с пагинацией
     *
     * @param user пользователь, чьи посты запрашиваются
     * @param pageRequest параметры пагинации
     * @return страница с постами пользователя
     */
    @Override
    public PageResponse<PostResponse> getAllPostsByUser(User user, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());

        centralLogger.logInfo("ПОСТЫ_ПОЛЬЗОВАТЕЛЯ_ПОЛУЧЕНИЕ",
                "Получение постов пользователя", context);

        try {
            Page<Post> posts =
                    postRepository.getAllPostsByUserWithCommentsAndLikes(user, pageRequest.toPageable());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalPosts", posts.getTotalElements());

            centralLogger.logInfo("ПОСТЫ_ПОЛЬЗОВАТЕЛЯ_ПОЛУЧЕНЫ",
                    "Посты пользователя успешно получены", resultContext);

            return PageResponse.of(posts.map(
                    post -> entityMapper.mapWithName(post, PostResponse.class, "withDetails")
            ));
        } catch (Exception e) {
            centralLogger.logError("ПОСТЫ_ПОЛЬЗОВАТЕЛЯ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении постов пользователя", context, e);
            throw e;
        }
    }

    /**
     * Редактирует существующий пост
     *
     * @param request запрос на редактирование поста
     * @param id идентификатор поста
     * @param currentUser текущий пользователь
     * @return ответ с отредактированным постом
     */
    @Transactional
    @Override
    public PostResponse editPost(PostRequest request, Long id, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", id);
        context.put("userId", currentUser.getId());
        context.put("contentUpdated", request.getContent() != null);
        context.put("imageUpdated", request.getImageUrl() != null);

        centralLogger.logInfo("ПОСТ_РЕДАКТИРОВАНИЕ",
                "Редактирование поста", context);

        try {
            postValidator.validatePostUpdate(request, id, currentUser);

            Post post = entityUtils.getPost(id);

            if (request.getContent() != null) {
                post.setContent(request.getContent());
            }

            if (request.getImageUrl() != null) {
                post.setImageUrl(request.getImageUrl());
            }

            Post updatedPost = postRepository.save(post);

            centralLogger.logInfo("ПОСТ_ОБНОВЛЕН",
                    "Пост успешно обновлен", context);

            cacheEventPublisherService.publishPostEdit(this, updatedPost, id);
            return entityMapper.map(updatedPost, PostResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_РЕДАКТИРОВАНИЯ",
                    "Ошибка при редактировании поста", context, e);
            throw e;
        }
    }

    /**
     * Загружает изображение для поста
     *
     * @param id идентификатор поста
     * @param imageFile файл изображения
     * @param currentUser текущий пользователь
     * @return ответ с постом с обновленным изображением
     */
    @Transactional
    @Override
    public PostResponse uploadImage(Long id, MultipartFile imageFile, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", id);
        context.put("userId", currentUser.getId());
        context.put("fileName", imageFile.getOriginalFilename());
        context.put("fileSize", imageFile.getSize());

        centralLogger.logInfo("ПОСТ_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ",
                "Загрузка изображения для поста", context);

        try {
            fileStorageServiceImpl.validateImageFile(imageFile);
            postValidator.validatePostOwnership(id, currentUser);

            Post post = entityUtils.getPost(id);

            if (post.getImageUrl() != null) {
                fileStorageServiceImpl.deleteFile(post.getImageUrl());
            }

            String imageUrl = fileStorageServiceImpl.savePostImage(imageFile, id);
            post.setImageUrl(imageUrl);
            Post updatedPost = postRepository.save(post);

            centralLogger.logInfo("ПОСТ_ИЗОБРАЖЕНИЕ_ЗАГРУЖЕНО",
                    "Изображение для поста успешно загружено", context);

            cacheEventPublisherService.publishPostEdit(this, updatedPost, id);
            return entityMapper.map(updatedPost, PostResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_ЗАГРУЗКИ_ИЗОБРАЖЕНИЯ",
                    "Ошибка при загрузке изображения для поста", context, e);
            throw e;
        }
    }

    /**
     * Удаляет изображение из поста
     *
     * @param id идентификатор поста
     * @param currentUser текущий пользователь
     * @return ответ с постом без изображения
     */
    @Transactional
    @Override
    public PostResponse removeImage(Long id, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", id);
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("ПОСТ_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ",
                "Удаление изображения из поста", context);

        try {
            postValidator.validatePostOwnership(id, currentUser);

            Post post = entityUtils.getPost(id);

            if (post.getImageUrl() != null) {
                fileStorageServiceImpl.deleteFile(post.getImageUrl());
            }

            post.setImageUrl(null);
            Post updatedPost = postRepository.save(post);

            centralLogger.logInfo("ПОСТ_ИЗОБРАЖЕНИЕ_УДАЛЕНО",
                    "Изображение из поста успешно удалено", context);

            cacheEventPublisherService.publishPostEdit(this, updatedPost, id);
            return entityMapper.map(updatedPost, PostResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_УДАЛЕНИЯ_ИЗОБРАЖЕНИЯ",
                    "Ошибка при удалении изображения из поста", context, e);
            throw e;
        }
    }

    /**
     * Получает пост по идентификатору
     *
     * @param postId идентификатор поста
     * @return ответ с данными поста
     */
    @Override
    public PostResponse getPostById(Long postId) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);

        centralLogger.logInfo("ПОСТ_ПОЛУЧЕНИЕ",
                "Получение поста по ID", context);

        try {
            Post post = entityUtils.getPost(postId);
            return entityMapper.map(post, PostResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении поста", context, e);
            throw e;
        }
    }
}