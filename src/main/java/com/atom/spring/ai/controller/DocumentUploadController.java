package com.atom.spring.ai.controller;

import com.atom.spring.ai.service.DocumentUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class DocumentUploadController {

  private final DocumentUploadService documentUploadService;

  public DocumentUploadController(DocumentUploadService documentUploadService) {
    this.documentUploadService = documentUploadService;
  }

  @PostMapping
  public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    log.info("Request to upload Document : {}", originalFilename);
    final String message = documentUploadService.upload(file);
    log.info("Successfully Uploaded Document : {}", originalFilename);
    return ResponseEntity.ok(message);
  }
}
