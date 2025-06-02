package com.example.netology_diplom_backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String filename;
    private long size; // Убедитесь, что тип данных соответствует
}
