package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.user.UserAlreadyExistsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.filters.UserDetailsImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final CentralLogger centralLogger;

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("username", user.getUsername());
        context.put("email", user.getEmail());

        centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_СОХРАНЕНИЕ",
                "Сохранение пользователя", context);

        try {
            User savedUser = repository.save(user);

            centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_СОХРАНЕН",
                    "Пользователь успешно сохранен", context);

            return savedUser;
        } catch (Exception e) {
            centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_ОШИБКА_СОХРАНЕНИЯ",
                    "Ошибка при сохранении пользователя", context, e);
            throw e;
        }
    }

    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("username", user.getUsername());
        context.put("email", user.getEmail());

        centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_СОЗДАНИЕ",
                "Создание пользователя", context);

        try {
            if (repository.existsByUsername(user.getUsername())) {
                centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_УЖЕ_СУЩЕСТВУЕТ_ИМЯ",
                        "Пользователь с таким именем уже существует", context, new UserAlreadyExistsException(ResponseMessageConstants.FAILURE_USER_WITH_NAME_ALREADY_EXISTS));
                throw new UserAlreadyExistsException(ResponseMessageConstants.FAILURE_USER_WITH_NAME_ALREADY_EXISTS);
            }

            if (repository.existsByEmail(user.getEmail())) {
                centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_УЖЕ_СУЩЕСТВУЕТ_EMAIL",
                        "Пользователь с таким email уже существует", context, new UserAlreadyExistsException(ResponseMessageConstants.FAILURE_USER_WITH_EMAIL_ALREADY_EXISTS));
                throw new UserAlreadyExistsException(ResponseMessageConstants.FAILURE_USER_WITH_EMAIL_ALREADY_EXISTS);
            }

            User createdUser = save(user);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("userId", createdUser.getId());

            centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_СОЗДАН",
                    "Пользователь успешно создан", successContext);

            return createdUser;
        } catch (Exception e) {
            centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании пользователя", context, e);
            throw e;
        }
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        Map<String, Object> context = new HashMap<>();
        context.put("username", username);

        centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_ПОИСК_ПО_ИМЕНИ",
                "Поиск пользователя по имени", context);

        try {
            User user = repository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("userId", user.getId());

            centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_НАЙДЕН_ПО_ИМЕНИ",
                    "Пользователь найден по имени", successContext);

            return user;
        } catch (Exception e) {
            centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_ОШИБКА_ПОИСКА_ПО_ИМЕНИ",
                    "Ошибка при поиске пользователя по имени", context, e);
            throw e;
        }
    }

    /**
     * Получение пользователя по идентификатору
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    public User getById(Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", id);

        centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_ПОИСК_ПО_ID",
                "Поиск пользователя по ID", context);

        try {
            User user = repository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь с id " + id + " не найден"));

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("username", user.getUsername());

            centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_НАЙДЕН_ПО_ID",
                    "Пользователь найден по ID", successContext);

            return user;
        } catch (Exception e) {
            centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_ОШИБКА_ПОИСКА_ПО_ID",
                    "Ошибка при поиске пользователя по ID", context, e);
            throw e;
        }
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = getByUsername(username);
            return UserDetailsImpl.build(user);
        };
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("ТЕКУЩИЙ_ПОЛЬЗОВАТЕЛЬ_ПОЛУЧЕНИЕ",
                "Получение текущего пользователя", context);

        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                centralLogger.logError("ТЕКУЩИЙ_ПОЛЬЗОВАТЕЛЬ_НЕ_АУТЕНТИФИЦИРОВАН",
                        "Пользователь не аутентифицирован", context, new AccessDeniedException("Пользователь не аутентифицирован"));
                throw new AccessDeniedException("Пользователь не аутентифицирован");
            }
            var username = authentication.getName();
            User user = getByUsername(username);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("userId", user.getId());
            successContext.put("username", user.getUsername());

            centralLogger.logInfo("ТЕКУЩИЙ_ПОЛЬЗОВАТЕЛЬ_ПОЛУЧЕН",
                    "Текущий пользователь успешно получен", successContext);

            return user;
        } catch (Exception e) {
            centralLogger.logError("ТЕКУЩИЙ_ПОЛЬЗОВАТЕЛЬ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении текущего пользователя", context, e);
            throw e;
        }
    }

    /**
     * Выдача прав администратора текущему пользователю
     * <p>
     * Нужен для демонстрации
     */
    @Deprecated
    public void getAdmin() {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("ВЫДАЧА_АДМИН_ПРАВ",
                "Выдача прав администратора текущему пользователю", context);

        try {
            var user = getCurrentUser();
            user.setRole(Role.ROLE_ADMIN);
            save(user);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("userId", user.getId());
            successContext.put("username", user.getUsername());

            centralLogger.logInfo("АДМИН_ПРАВА_ВЫДАНЫ",
                    "Права администратора успешно выданы", successContext);
        } catch (Exception e) {
            centralLogger.logError("АДМИН_ПРАВА_ОШИБКА_ВЫДАЧИ",
                    "Ошибка при выдаче прав администратора", context, e);
            throw e;
        }
    }
}