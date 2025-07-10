package com.example.netology_diplom_backend.repository;


import com.example.netology_diplom_backend.model.FileMetadata;
import com.example.netology_diplom_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findAllByOwner(User owner);

    Optional<FileMetadata> findByFilenameAndOwner(String filename, User owner);

    void deleteByFilenameAndOwner(String filename, User owner);
}
