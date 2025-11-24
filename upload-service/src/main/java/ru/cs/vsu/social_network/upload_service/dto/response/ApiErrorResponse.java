package ru.cs.vsu.social_network.upload_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
@Schema(description = "Ответ с информацией об ошибке")
public class ApiErrorResponse {

    @Schema(description = "Время возникновения ошибки")
    Instant timestamp;

    @Schema(description = "HTTP статус")
    int status;

    @Schema(description = "Краткий код ошибки")
    String code;

    @Schema(description = "Сообщение для клиента")
    String message;

    @Schema(description = "Путь запроса")
    String path;

    @Schema(description = "Список деталей ошибки")
    List<String> errors;
}

