package ru.cs.vsu.social_network.contents_service.events.cache;

/**
 * Типы событий для инвалидации кеша.
 * Определяет все возможные события, которые влияют на кеш.
 */
public enum CacheEventType {
    POST_CREATED,
    POST_UPDATED,
    COMMENT_ADDED,
    COMMENT_UPDATED,
    COMMENT_DELETED,
    LIKE_ADDED,
    LIKE_DELETED;

    /**
     * Возвращает строковое представление типа события.
     *
     * @return строковое значение типа события
     */
    public String getValue() {
        return this.name();
    }

    /**
     * Проверяет валидность строкового представления типа события.
     *
     * @param eventType строковое представление типа события
     * @return true если тип события валиден, false в противном случае
     */
    public static boolean isValid(String eventType) {
        if (eventType == null) {
            return false;
        }
        try {
            CacheEventType.valueOf(eventType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}