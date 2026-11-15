package com.homeshare.exception;

import org.springframework.http.HttpStatus;

/**
 * Erro de regra de negócio com o status HTTP já definido.
 * Uso: throw BusinessException.notFound("Casa não encontrada.");
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static BusinessException badRequest(String msg) {
        return new BusinessException(HttpStatus.BAD_REQUEST, msg);
    }

    public static BusinessException unauthorized(String msg) {
        return new BusinessException(HttpStatus.UNAUTHORIZED, msg);
    }

    public static BusinessException forbidden(String msg) {
        return new BusinessException(HttpStatus.FORBIDDEN, msg);
    }

    public static BusinessException notFound(String msg) {
        return new BusinessException(HttpStatus.NOT_FOUND, msg);
    }

    public static BusinessException conflict(String msg) {
        return new BusinessException(HttpStatus.CONFLICT, msg);
    }

    public static BusinessException gone(String msg) {
        return new BusinessException(HttpStatus.GONE, msg);
    }

    public static BusinessException unprocessable(String msg) {
        return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, msg);
    }

    public static BusinessException tooManyRequests(String msg) {
        return new BusinessException(HttpStatus.TOO_MANY_REQUESTS, msg);
    }

    public static BusinessException badGateway(String msg) {
        return new BusinessException(HttpStatus.BAD_GATEWAY, msg);
    }
}
