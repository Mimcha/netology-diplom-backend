package com.example.netology_diplom_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FileMetadata {
    @Id
    @GeneratedValue
    private Long id;
    private String originalName;
    private String storedName;
    private Long size;
    @ManyToOne
    private User user;

}
