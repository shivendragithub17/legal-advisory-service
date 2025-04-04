package com.atom.spring.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1")
public class QueryController {

  private final ChatClient chatClient;

  public QueryController(ChatClient.Builder chatClient, SimpleVectorStore simpleVectorStore) {
    this.chatClient =
        chatClient.defaultAdvisors(new QuestionAnswerAdvisor(simpleVectorStore)).build();
  }

  @GetMapping("/query")
  public ResponseEntity<String> query(@RequestParam @NotNull String query) {
    if (query == null || query.isEmpty()) {
      throw new IllegalArgumentException("query is null or empty");
    }
    return ResponseEntity.ok(this.chatClient.prompt().user(query).call().content());
  }
}
