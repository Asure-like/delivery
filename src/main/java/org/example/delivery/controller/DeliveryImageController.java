package org.example.delivery.controller;

import org.example.delivery.component.OcrDataParser;
import org.example.delivery.service.IDeliveryService;
import org.example.delivery.service.impl.OcrService;
import org.example.delivery.pojo.DeliveryOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin; // 添加跨域支持
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*") // 允许所有来源的跨域请求
public class DeliveryImageController {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private OcrDataParser dataParser;

    @PostMapping("/imageUpload")
    public DeliveryOrder uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String ocrResult = ocrService.recognizeTable(file);
            return dataParser.parseOcrResult(ocrResult);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}