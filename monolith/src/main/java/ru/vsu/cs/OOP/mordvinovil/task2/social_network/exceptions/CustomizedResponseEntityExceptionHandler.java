package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.exception.ExceptionResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.comment.CommentContentTooLongException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.comment.CommentEmptyContentException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.comment.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileOversizeException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileProcessingException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileUnsupportedFormat;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.like.LikeAlreadyExistsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.like.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.MessageContentEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.MessageContentTooLongException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.MessageNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.SelfMessageException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.notification.NotificationNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.post.PostContentEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.post.PostContentTooLongException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.post.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.profile.ProfileAlreadyExistsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.profile.ProfileContentTooLongException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.profile.ProfileNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.DuplicateRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipNoPendingRequestsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipToSelfException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.user.UserNotFoundException;

import java.util.Date;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request) {
        String message = "Внутренняя ошибка сервера";
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), message,
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // User exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Пользователь не найден",
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    // Profile exceptions
    @ExceptionHandler(ProfileNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleProfileNotFoundException(ProfileNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProfileContentTooLongException.class)
    public final ResponseEntity<ExceptionResponse> handleProfileContentTooLongException(ProfileContentTooLongException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public final ResponseEntity<ExceptionResponse> handleProfileAlreadyExistsException(ProfileAlreadyExistsException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    // Post exceptions
    @ExceptionHandler(PostNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handlePostNotFoundException(PostNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PostContentEmptyException.class)
    public final ResponseEntity<ExceptionResponse> handlePostContentEmptyException(PostContentEmptyException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PostContentTooLongException.class)
    public final ResponseEntity<ExceptionResponse> handlePostContentTooLongException(PostContentTooLongException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    // Comment exceptions
    @ExceptionHandler(CommentNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleCommentNotFoundException(CommentNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CommentContentTooLongException.class)
    public final ResponseEntity<ExceptionResponse> handleCommentContentTooLongException(CommentContentTooLongException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CommentEmptyContentException.class)
    public final ResponseEntity<ExceptionResponse> handleCommentEmptyContentException(CommentEmptyContentException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }


    // Security exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);
    }

    // File exceptions
    @ExceptionHandler(FileProcessingException.class)
    public final ResponseEntity<ExceptionResponse> handleFileProcessingException(FileProcessingException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileEmptyException.class)
    public final ResponseEntity<ExceptionResponse> handleFileEmptyException(FileEmptyException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileUnsupportedFormat.class)
    public final ResponseEntity<ExceptionResponse> handleFileUnsupportedFormat(FileUnsupportedFormat ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileOversizeException.class)
    public final ResponseEntity<ExceptionResponse> handleFileOversizeException(FileOversizeException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    // IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    // Like exceptions
    @ExceptionHandler(LikeNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleLikeNotFoundException(LikeNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LikeAlreadyExistsException.class)
    public final ResponseEntity<ExceptionResponse> handleLikeAlreadyExistsException(LikeAlreadyExistsException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    // Relationship exceptions
    @ExceptionHandler(DuplicateRelationshipException.class)
    public final ResponseEntity<ExceptionResponse> handleDuplicateRelationshipException(DuplicateRelationshipException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RelationshipNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleRelationshipNotFoundException(RelationshipNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RelationshipToSelfException.class)
    public final ResponseEntity<ExceptionResponse> handleRelationshipToSelfException (RelationshipToSelfException  ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RelationshipNoPendingRequestsException.class)
    public final ResponseEntity<ExceptionResponse> handleRelationshipNoPendingRequestsException(RelationshipNoPendingRequestsException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }


    // Message exceptions
    @ExceptionHandler(SelfMessageException.class)
    public final ResponseEntity<ExceptionResponse> handleSelfMessageException(SelfMessageException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MessageNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleMessageNotFoundException (MessageNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MessageContentEmptyException.class)
    public final ResponseEntity<ExceptionResponse> handleMessageContentEmptyException(MessageContentEmptyException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MessageContentTooLongException.class)
    public final ResponseEntity<ExceptionResponse> handleMessageContentTooLongException(MessageContentTooLongException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }


    // Notification messages
    @ExceptionHandler(NotificationNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleNotificationNotFoundException(NotificationNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
                request.getDescription(false), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }


    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Ошибка валидации");

        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), errorMessage,
                ex.getBindingResult().toString(), false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}