package com.atom.spring.ai.service;

import com.atom.spring.ai.properties.DocumentProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStoreContent;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Service responsible for embedding documents and storing them in a vector store. This service
 * implements the {@link Callable} interface to perform document embedding as a background task.
 */
@Slf4j
public class DocumentEmbeddingService implements Callable<Boolean> {
  private final String documentName;
  private final SimpleVectorStore simpleVectorStore;
  private final DocumentProperties documentProperties;

  /**
   * Constructs a new {@code DocumentEmbeddingService}.
   *
   * @param documentName The name of the document to be embedded.
   * @param simpleVectorStore The {@link SimpleVectorStore} to store the document embeddings.
   * @param documentProperties The {@link DocumentProperties} containing configuration for document
   *     handling.
   */
  public DocumentEmbeddingService(
      String documentName,
      SimpleVectorStore simpleVectorStore,
      DocumentProperties documentProperties) {
    this.documentName = documentName;
    this.simpleVectorStore = simpleVectorStore;
    this.documentProperties = documentProperties;
  }

  /**
   * Executes the document embedding process.
   *
   * <p>This method reads a PDF document, splits it into smaller documents, and stores them in the
   * configured {@link SimpleVectorStore}. It also handles loading existing data from the vector
   * store and deleting the vector store file after loading.
   *
   * @return {@code true} if the embedding process is successful, {@code false} otherwise.
   * @throws Exception if any error occurs during the embedding process.
   */
  @Override
  public Boolean call() {
    try {
      String vectorStoreLocation = documentProperties.vectorStoreFileLocation();
      Resource resource =
          new FileSystemResource(
              documentProperties.uploadLocation() + File.separator + this.documentName);
      List<Document> documents = new ArrayList<>(loadDataAndDeleteFile());
      PdfDocumentReaderConfig documentReaderConfig =
          PdfDocumentReaderConfig.builder().withPagesPerDocument(1).build();
      PagePdfDocumentReader pagePdfDocumentReader =
          new PagePdfDocumentReader(resource, documentReaderConfig);
      TextSplitter textSplitter = new TokenTextSplitter();
      documents.addAll(textSplitter.split(pagePdfDocumentReader.get()));
      writeVectorStoreToFile(documents, vectorStoreLocation);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  /**
   * Loads existing vector data from the vector store file and deletes the file.
   *
   * <p>This method reads the content of the vector store file, deserializes it into a list of
   * {@link Document} objects, and then deletes the vector store file. This is typically used to
   * load previously stored embeddings before adding new ones.
   *
   * @return A list of {@link Document} objects loaded from the vector store, or an empty list if
   *     the file does not exist or an error occurs.
   * @throws RuntimeException if an error occurs during file reading, deserialization, or deletion.
   */
  private List<Document> loadDataAndDeleteFile() {
    List<Document> existingData = new ArrayList<>();
    try {
      Path vectorStorePathLocation = Path.of(documentProperties.vectorStoreFileLocation());
      boolean vectorFileExists = Files.exists(vectorStorePathLocation);
      if (vectorFileExists) {
        String data = Files.readString(vectorStorePathLocation);
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jsonObject = new JSONObject(data);
        JSONArray names = jsonObject.names();
        for (int i = 0; i < names.length(); i++) {
          String pageString = jsonObject.getString(names.getString(i));
          SimpleVectorStoreContent simpleVectorStoreContent =
              objectMapper.readValue(pageString, SimpleVectorStoreContent.class);
          Document document =
              Document.builder()
                  .id(simpleVectorStoreContent.getId())
                  .text(simpleVectorStoreContent.getText())
                  .metadata(simpleVectorStoreContent.getMetadata())
                  .build();
          existingData.add(document);
        }
        Files.delete(vectorStorePathLocation);
      }
      return existingData;
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }

  private synchronized void writeVectorStoreToFile(
      List<Document> documents, String vectorStoreLocation) {
    File vectorFile = new File(vectorStoreLocation);
    simpleVectorStore.add(documents);
    simpleVectorStore.save(vectorFile);
  }
}
