package com.mitchseymour;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestHandlerTest {
  private RequestHandler requestHandler;

  @BeforeEach
  void setup() {
    requestHandler = new RequestHandler();
  }

  @Test
  @DisplayName("Closest genres returns the expected results")
  void testNClosestGenres() {
    requestHandler.addDocumentToGenre(
        "music", "123", "I love working to music. chillwave, synthwave, you name it.");
    requestHandler.addDocumentToGenre(
        "film", "456", "movies are cool. especially those that have good music");

    // assert that both genres are returned in the expected order
    List<String> closestGenres =
        requestHandler.getNClosestGenres("synthwave is my favorite music genre", 2);
    assertThat(closestGenres).containsExactly("music", "film");

    // apply a limit, and assert that only the music genre is returned
    closestGenres = requestHandler.getNClosestGenres("synthwave is my favorite music genre", 1);
    assertThat(closestGenres).containsExactly("music");
  }
}
