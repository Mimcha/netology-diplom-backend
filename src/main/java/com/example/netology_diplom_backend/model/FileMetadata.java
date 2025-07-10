package com.example.netology_diplom_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "files")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private Long size;

    private String hash;

    @ManyToOne
    @JoinColumn(name = "user_login")
    private User owner;

    public FileMetadata() {}

    public FileMetadata(String filename, Long size, String hash, User owner) {
        this.filename = filename;
        this.size = size;
        this.hash = hash;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
