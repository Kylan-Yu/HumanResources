package com.hrms.system.exception;

import com.hrms.common.Result;
import com.hrms.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception mapping for system service controllers.
 * Prevents SQL errors from being translated to 401 on /error dispatch.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.hrms.system.controller")
public class SystemGlobalExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleDataAccessException(DataAccessException ex) {
        log.error("[system-exception] database error", ex);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "Database access error");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleUnexpectedException(Exception ex) {
        log.error("[system-exception] unexpected error", ex);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "Internal server error");
    }
}
