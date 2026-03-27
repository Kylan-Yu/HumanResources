package com.hrms.auth.exception;

import com.hrms.common.Result;
import com.hrms.common.ResultCode;
import com.hrms.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Auth module exception mapping.
 * Ensures database errors are returned as 500 instead of being confused as auth failures.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.hrms.auth.controller")
public class AuthGlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessException(BusinessException ex) {
        log.warn("[auth-exception] business error: {}", ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleDataAccessException(DataAccessException ex) {
        log.error("[auth-exception] database error", ex);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "Database access error");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleUnexpectedException(Exception ex) {
        log.error("[auth-exception] unexpected error", ex);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "Internal server error");
    }
}

