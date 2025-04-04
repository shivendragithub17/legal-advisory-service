package com.atom.spring.ai.config;

import com.atom.spring.ai.properties.DocumentProperties;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up the vector store used for document embeddings.
 * This class initializes and configures a SimpleVectorStore bean using OpenAI's embedding model.
 */
@Configuration
public class VectorStoreConfig {
  
  /** Properties containing document-related configurations */
  private final DocumentProperties documentProperties;
  
  /**
   * Constructs a new VectorStoreConfig with the specified document properties.
   *
   * @param documentProperties Configuration properties for document handling
   */
  public VectorStoreConfig(DocumentProperties documentProperties) {
    this.documentProperties = documentProperties;
  }
  
  /**
   * Creates and configures a SimpleVectorStore bean.
   * If a vector store file exists at the configured location, it loads the existing store.
   * Otherwise, creates a new vector store instance.
   *
   * @param openAiEmbeddingModel The OpenAI embedding model to be used for vector operations
   * @return A configured SimpleVectorStore instance
   */
  @Bean
  public SimpleVectorStore simpleVectorStore(OpenAiEmbeddingModel openAiEmbeddingModel) {
    SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(openAiEmbeddingModel).build();
    if (Files.exists(Path.of(documentProperties.vectorStoreFileLocation()))) {
      simpleVectorStore.load(new File(documentProperties.vectorStoreFileLocation()));
    }
    return simpleVectorStore;
  }
}
