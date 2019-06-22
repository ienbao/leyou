package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> ExceptionHandler(LyException e){
        return ResponseEntity.status(new ExceptionResult(e.getExceptionEnums()).getStatus()).body(new ExceptionResult(e.getExceptionEnums()));
    }
}
