package org.example.delivery.service;

import org.springframework.stereotype.Service;

@Service
public interface IDeliveryService {
    String uploadImage(byte[] imageBase64);
}
