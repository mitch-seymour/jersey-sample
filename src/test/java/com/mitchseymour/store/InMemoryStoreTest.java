package com.mitchseymour.store;

import static org.assertj.core.api.Assertions.assertThat;

import com.mitchseymour.Document;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryStoreTest {
  private InMemoryStore store;

  private final Document doc1 =
      new Document(
          "123",
          "Synthwave is an electronic music microgenre that is based predominately on the music associated with action, science-fiction, and horror film soundtracks of the 1980s");

  private final Document doc2 =
      new Document(
          "456",
          "Chillwave is a music microgenre that emerged in the late 2000s. It is characterized by a faded or dreamy retro pop sound");

  private final String genre = "music";

  @BeforeEach
  void setup() {
    store = new InMemoryStore();
  }

  @AfterEach
  void close() {
    store.close();
  }

  @Test
  @DisplayName("documents can be saved and retrieved")
  void testDocumentsCanBeSaved() {
    // save some documents to the store
    store.put(genre, doc1);
    store.put(genre, doc2);

    // assert that the documents were saved
    List<String> docIds = store.get(genre);
    assertThat(docIds).containsExactly(doc1.getId(), doc2.getId());
  }

  @Test
  @DisplayName("documents can be removed")
  void testDocumentsCanBeRemoved() {
    // save some documents to the store
    store.put(genre, doc1);
    store.put(genre, doc2);

    // assert that the documents were saved
    List<String> docIds = store.get(genre);
    assertThat(docIds).containsExactly(doc1.getId(), doc2.getId());

    // remove doc1
    store.remove(genre, doc1.getId());
    docIds = store.get(genre);
    assertThat(docIds).containsExactly(doc2.getId());

    // remove doc2
    store.remove(genre, doc2.getId());
    docIds = store.get(genre);
    assertThat(docIds).isEmpty();

    // removing a non-existent doc ID shouldn't throw an error
    store.remove(genre, "000");
  }
}
