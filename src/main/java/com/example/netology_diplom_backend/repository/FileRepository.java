package com.example.netology_diplom_backend.repository;

import com.example.netology_diplom_backend.model.FileMetadata;
import com.example.netology_diplom_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;

public interface FileRepository extends JpaRepository<FileMetadata, Long> {
    FileMetadata findByUserAndOriginalName(User user, String filename);
    @Query("SELECT f FROM FileMetadata f WHERE f.user = ?1 ORDER BY f.originalName ASC")
    List<FileMetadata> findTopByUserOrderByOriginalNameAsc(User user, Pageable pageable);
}