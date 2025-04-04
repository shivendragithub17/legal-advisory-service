package com.atom.spring.ai.service;

import com.atom.spring.ai.properties.DocumentProperties;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service class for handling document uploads and initiating document embedding.
 *
 * <p>This service is responsible for: - Initializing the document upload directory upon application
 * startup. - Handling the upload of document files. - Asynchronously triggering the document
 * embedding process after a successful upload.
 */
@Service
@Slf4j
public class DocumentUploadService {

  private final SimpleVectorStore simpleVectorStore;
  private final DocumentProperties documentProperties;

  /**
   * Constructor for DocumentUploadService.
   *
   * @param simpleVectorStore The SimpleVectorStore instance for storing document embeddings.
   * @param documentProperties The DocumentProperties instance containing document related
   *     configurations.
   */
  public DocumentUploadService(
      SimpleVectorStore simpleVectorStore, DocumentProperties documentProperties) {
    this.simpleVectorStore = simpleVectorStore;
    this.documentProperties = documentProperties;
  }

  /**
   * Initializes the DocumentUploadService by creating the document upload directory if it doesn't
   * exist.
   *
   * <p>This method is executed after dependency injection is complete and is responsible for
   * setting up the necessary environment for document uploads.
   *
   * @throws IOException if an I/O error occurs during directory creation.
   */
  @PostConstruct
  public void init() throws IOException {
    log.info("Initializing DocumentUploadService");
    Path docPath = Path.of(documentProperties.uploadLocation());
    Path vectorStoreFilePath = Path.of(documentProperties.vectorStoreFileLocation());
    Path vectorStoreDirPath = vectorStoreFilePath.getParent();
    if (!Files.exists(docPath)) {
      Files.createDirectory(docPath);
    }
    if (!Files.exists(vectorStoreDirPath)) {
      Files.createDirectory(vectorStoreDirPath);
    }
    log.info("DocumentUploadService initialized");
  }

  /**
   * Uploads a document file.
   *
   * <p>This method saves the uploaded MultipartFile to the configured upload location and then
   * asynchronously initiates the document embedding process.
   *
   * @param file The MultipartFile to be uploaded.
   * @return if the file upload and embedding initiation are successful, false otherwise.
   * @throws RuntimeException if any exception occurs during file upload or embedding initiation.
   */
  public String upload(MultipartFile file) {
    try {
      final String documentName = file.getOriginalFilename();
      if (Objects.isNull(documentName) || !documentName.contains("."))
        throw new IllegalArgumentException("Invalid file");
      if (!file.getContentType().equals(MediaType.APPLICATION_PDF_VALUE))
        throw new IllegalArgumentException("Invalid file format. Only PDF files are supported");
      if (fileAlreadyExists(documentName))
        return String.format("Document %s already exists", documentName);

      CompletableFuture.supplyAsync(
              () -> {
                try {
                  log.info("Uploading Document : {}", documentName);
                  return Files.write(
                      Path.of(documentProperties.uploadLocation() + File.separator + documentName),
                      file.getBytes());
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              })
          .thenAccept(
              (filePath) -> {
                if (Files.exists(filePath)) {
                  log.info("Initializing EmbeddingTask for file {}", documentName);
                  CompletableFuture.runAsync(() -> invokeDocumentEmbedding(documentName));
                }
              });
      log.info("file : {} uploaded successfully", documentName);
      return String.format("Document %s uploaded successfully", documentName);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }

  private boolean fileAlreadyExists(String fileName) {
    return Files.exists(Path.of(documentProperties.uploadLocation() + File.separator + fileName));
  }

  /**
   * Invokes the document embedding process for a given document.
   *
   * <p>This method creates a {@link DocumentEmbeddingService} to generate embeddings for the
   * provided document. It uses a {@link CompletableFuture} to perform this task asynchronously. The
   * method also includes a delay of 5 seconds before creating the task.
   *
   * @param documentName The name of the document to be embedded.
   * @throws RuntimeException if any exception occurs during the embedding process.
   */
  private void invokeDocumentEmbedding(String documentName) {
    try {
      Thread.sleep(5000L);
      CompletableFuture.supplyAsync(
              () ->
                  new DocumentEmbeddingService(documentName, simpleVectorStore, documentProperties))
          .thenApply(
              task -> {
                try {
                  return task.call();
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              })
          .thenAccept(
              result -> log.info("{} embedding created successfully: {}", documentName, result));
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }
}
