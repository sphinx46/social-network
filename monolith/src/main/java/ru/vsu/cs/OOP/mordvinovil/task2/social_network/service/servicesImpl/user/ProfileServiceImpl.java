package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.profile.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.profile.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.ProfileRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.ProfileService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.storage.FileStorageServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.ProfileAgeCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ProfileFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.ProfileValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final ProfileRepository profileRepository;
    private final UserServiceImpl userServiceImpl;
    private final EntityMapper entityMapper;
    private final ProfileFactory profileFactory;
    private final ProfileAgeCalculator ageCalculator;
    private final ProfileValidator profileValidator;
    private final EntityUtils entityUtils;
    private final CentralLogger centralLogger;

    /**
     * Создает профиль для пользователя
     *
     * @param request данные для создания профиля
     * @param user пользователь, для которого создается профиль
     * @return созданный профиль
     */
    @Override
    public Profile create(ProfileRequest request, User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("username", user.getUsername());

        centralLogger.logInfo("ПРОФИЛЬ_СОЗДАНИЕ",
                "Создание профиля для пользователя", context);

        try {
            profileValidator.validate(request, user);

            Profile profile = profileFactory.createProfile(user, request);
            Profile savedProfile = profileRepository.save(profile);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("profileId", savedProfile.getId());

            centralLogger.logInfo("ПРОФИЛЬ_СОЗДАН",
                    "Профиль успешно создан", successContext);

            return savedProfile;
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании профиля", context, e);
            throw e;
        }
    }

    /**
     * Получает профиль текущего пользователя
     *
     * @param user текущий пользователь
     * @return ответ с данными профиля
     */
    @Override
    public ProfileResponse getProfileByUser(User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("username", user.getUsername());

        centralLogger.logInfo("ПРОФИЛЬ_ПОЛУЧЕНИЕ_ПО_ПОЛЬЗОВАТЕЛЮ",
                "Получение профиля по пользователю", context);

        try {
            Profile profile = entityUtils.getProfileByUser(user);
            ProfileResponse response = entityMapper.map(profile, ProfileResponse.class);
            response.setAge(ageCalculator.calculateAge(profile.getDateOfBirth()));

            centralLogger.logInfo("ПРОФИЛЬ_ПОЛУЧЕН_ПО_ПОЛЬЗОВАТЕЛЮ",
                    "Профиль успешно получен по пользователю", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ОШИБКА_ПОЛУЧЕНИЯ_ПО_ПОЛЬЗОВАТЕЛЮ",
                    "Ошибка при получении профиля по пользователю", context, e);
            throw e;
        }
    }

    /**
     * Получает профиль по идентификатору пользователя
     *
     * @param id идентификатор пользователя
     * @return ответ с данными профиля
     */
    @Override
    public ProfileResponse getProfileByUserId(Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", id);

        centralLogger.logInfo("ПРОФИЛЬ_ПОЛУЧЕНИЕ_ПО_ID",
                "Получение профиля по ID пользователя", context);

        try {
            User user = userServiceImpl.getById(id);
            ProfileResponse response = getProfileByUser(user);

            centralLogger.logInfo("ПРОФИЛЬ_ПОЛУЧЕН_ПО_ID",
                    "Профиль успешно получен по ID пользователя", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ОШИБКА_ПОЛУЧЕНИЯ_ПО_ID",
                    "Ошибка при получении профиля по ID пользователя", context, e);
            throw e;
        }
    }

    /**
     * Загружает аватар для профиля пользователя
     *
     * @param user текущий пользователь
     * @param imageFile файл изображения для аватара
     * @return ответ с обновленными данными профиля
     */
    @Transactional
    @Override
    public ProfileResponse uploadAvatar(User user, MultipartFile imageFile) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("username", user.getUsername());
        context.put("originalFilename", imageFile.getOriginalFilename());
        context.put("fileSize", imageFile.getSize());

        centralLogger.logInfo("АВАТАР_ЗАГРУЗКА",
                "Загрузка аватара для профиля", context);

        try {
            fileStorageServiceImpl.validateImageFile(imageFile);
            profileValidator.validateAvatarUpload(user);

            Profile profile = entityUtils.getProfileByUser(user);

            if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
                fileStorageServiceImpl.deleteFile(profile.getImageUrl());
            }

            String avatarUrl = fileStorageServiceImpl.saveAvatar(imageFile, user.getId());
            profile.setImageUrl(avatarUrl);
            Profile updatedProfile = profileRepository.save(profile);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("avatarUrl", avatarUrl);

            centralLogger.logInfo("АВАТАР_ЗАГРУЖЕН",
                    "Аватар успешно загружен", successContext);

            return entityMapper.map(updatedProfile, ProfileResponse.class);
        } catch (Exception e) {
            centralLogger.logError("АВАТАР_ОШИБКА_ЗАГРУЗКИ",
                    "Ошибка при загрузке аватара", context, e);
            throw e;
        }
    }

    /**
     * Удаляет аватар профиля пользователя
     *
     * @param user текущий пользователь
     * @return ответ с обновленными данными профиля
     */
    @Transactional
    @Override
    public ProfileResponse removeAvatar(User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("username", user.getUsername());

        centralLogger.logInfo("АВАТАР_УДАЛЕНИЕ",
                "Удаление аватара профиля", context);

        try {
            profileValidator.validateAvatarUpload(user);

            Profile profile = entityUtils.getProfileByUser(user);

            if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
                fileStorageServiceImpl.deleteFile(profile.getImageUrl());
            }

            profile.setImageUrl(null);
            Profile updatedProfile = profileRepository.save(profile);

            centralLogger.logInfo("АВАТАР_УДАЛЕН",
                    "Аватар успешно удален", context);

            return entityMapper.map(updatedProfile, ProfileResponse.class);
        } catch (Exception e) {
            centralLogger.logError("АВАТАР_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении аватара", context, e);
            throw e;
        }
    }

    /**
     * Обновляет данные профиля пользователя
     *
     * @param user текущий пользователь
     * @param request данные для обновления профиля
     * @return ответ с обновленными данными профиля
     */
    @Transactional
    @Override
    public ProfileResponse updateProfile(User user, ProfileRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("username", user.getUsername());
        context.put("bioUpdated", request.getBio() != null);
        context.put("cityUpdated", request.getCity() != null);
        context.put("dateOfBirthUpdated", request.getDateOfBirth() != null);
        context.put("imageUrlUpdated", request.getImageUrl() != null);

        centralLogger.logInfo("ПРОФИЛЬ_ОБНОВЛЕНИЕ",
                "Обновление данных профиля", context);

        try {
            profileValidator.validateProfileUpdate(request, user);

            Profile profile = entityUtils.getProfileByUser(user);
            updateProfileFromRequest(profile, user, request);

            Profile updatedProfile = profileRepository.save(profile);

            centralLogger.logInfo("ПРОФИЛЬ_ОБНОВЛЕН",
                    "Данные профиля успешно обновлены", context);

            return entityMapper.map(updatedProfile, ProfileResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ОШИБКА_ОБНОВЛЕНИЯ",
                    "Ошибка при обновлении данных профиля", context, e);
            throw e;
        }
    }

    /**
     * Создает профиль по умолчанию для пользователя
     *
     * @param user пользователь, для которого создается профиль
     * @return созданный профиль по умолчанию
     */
    @Transactional
    @Override
    public Profile createDefaultProfile(User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("username", user.getUsername());

        centralLogger.logInfo("ПРОФИЛЬ_ПО_УМОЛЧАНИЮ_СОЗДАНИЕ",
                "Создание профиля по умолчанию", context);

        try {
            Profile profile = profileRepository.findByUser(user)
                    .orElseGet(() -> profileRepository.save(profileFactory.createDefaultProfile(user)));

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("profileId", profile.getId());

            centralLogger.logInfo("ПРОФИЛЬ_ПО_УМОЛЧАНИЮ_СОЗДАН",
                    "Профиль по умолчанию успешно создан", successContext);

            return profile;
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ПО_УМОЛЧАНИЮ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании профиля по умолчанию", context, e);
            throw e;
        }
    }

    /**
     * Обновляет данные профиля из запроса
     *
     * @param profile сущность профиля для обновления
     * @param user сущность пользователя
     * @param request запрос с данными для обновления
     */
    private void updateProfileFromRequest(Profile profile, User user, ProfileRequest request) {
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getCity() != null) {
            profile.setCity(request.getCity());
            user.setCity(request.getCity());
        }

        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getImageUrl() != null) {
            if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
                fileStorageServiceImpl.deleteFile(profile.getImageUrl());
            }
            profile.setImageUrl(request.getImageUrl());
        }
    }
}