package com.example.netology_diplom_backend.dto;

import lombok.Data;

@Data
public class FileResponse {
    private String filename;
    private long size; // Убедитесь, что тип данных соответствует
}
