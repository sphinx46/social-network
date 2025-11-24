package ru.cs.vsu.social_network.upload_service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Клиент для взаимодействия с внешним хранилищем медиа.
 */
public interface MediaStorageClient {

    /**
     * Загружает объект в хранилище.
     *
     * @param objectName уникальное имя объекта
     * @param file       исходный файл
     */
    void upload(String objectName, MultipartFile file);

    /**
     * Возвращает поток для скачивания объекта.
     *
     * @param objectName уникальное имя объекта
     * @return поток данных
     */
    InputStream download(String objectName);

    /**
     * Удаляет объект из хранилища.
     *
     * @param objectName уникальное имя объекта
     */
    void delete(String objectName);
}

