package ru.cs.vsu.social_network.upload_service.service;


import ru.cs.vsu.social_network.upload_service.dto.request.MediaDeleteRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaDownloadRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaContentResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;

import java.util.UUID;

public interface MediaService {
    /**
     * Загружает файл в MinIO и сохраняет метаданные.
     *
     * @param request параметры загрузки
     * @return данные сохранённого медиа
     */
    MediaResponse uploadFile(MediaUploadRequest request);

    /**
     * Возвращает метаданные медиа по идентификатору.
     *
     * @param id идентификатор медиа
     * @return метаданные
     */
    MediaMetadataResponse getMetaData(UUID id);

    /**
     * Возвращает поток для скачивания медиа.
     *
     * @param request параметры скачивания
     * @return поток и описание файла
     */
    MediaContentResponse download(MediaDownloadRequest request);

    /**
     * Удаляет медиа и объект в MinIO.
     *
     * @param request параметры удаления
     */
    void deleteFile(MediaDeleteRequest request);
}
