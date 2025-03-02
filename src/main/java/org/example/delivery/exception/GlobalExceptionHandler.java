package org.example.delivery.exception;

import com.aliyuncs.exceptions.ClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<String> handleAliyunError(ClientException ex) {
        return ResponseEntity.status(502)
                .body("OCR服务错误: " + ex.getErrCode());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleFileError() {
        return ResponseEntity.badRequest().body("文件大小超过限制");
    }
}