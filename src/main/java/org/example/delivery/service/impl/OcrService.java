package org.example.delivery.service.impl;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeBasicRequest;
import com.aliyun.ocr_api20210707.models.RecognizeBasicResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.ocr.model.v20191230.RecognizeTableRequest;
import com.aliyuncs.ocr.model.v20191230.RecognizeTableResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.example.delivery.config.AliYunConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.rmi.ServerException;

@Service
public class OcrService {

    @Autowired
    private AliYunConfig config;

    public String recognizeTable(MultipartFile file) throws Exception {

        Config conf = new Config().setAccessKeyId(config.getAccessKeyId())
                .setAccessKeySecret(config.getAccessKeySecret())
                .setEndpoint(config.getEndpoint());
        Client client = new Client(conf);
        RecognizeBasicRequest request = new RecognizeBasicRequest();
        // 转换图片为Base64
        String imageBase64 = Base64.encodeBase64String(file.getBytes());
        request.setBody(file.getInputStream());
        request.setNeedRotate(false);

        try {
            RecognizeBasicResponse response = client.recognizeBasic(request);
            System.out.println(new Gson().toJson(response.body));
            return new Gson().toJson(response.body);
        } catch (ClientException e) {
            throw new RuntimeException("OCR客户端错误", e);
        }
    }
}
