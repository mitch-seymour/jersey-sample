package com.mitchseymour.classifier;

import static org.assertj.core.api.Assertions.assertThat;

import com.mitchseymour.Document;
import com.mitchseymour.store.InMemoryStore;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentClassifierTest {

  @Test
  @DisplayName("document centroid can be computed")
  void testDocumentCentroidCanBeComputed() throws IOException {
    DocumentClassifier classifier = new DocumentClassifier("programming", new InMemoryStore());

    // add some documents to the classifier
    Document doc1 = new Document("123", "hello, world");
    Document doc2 = new Document("456", "goodbye, world");
    classifier.addDocument(doc1);
    classifier.addDocument(doc2);

    // compute and retrieve the document centroid
    Map<CharSequence, Double> centroid = classifier.getDocumentCentroid();

    // assert that the centoid contains the correct values
    Map<CharSequence, Double> expected = new HashMap<>();
    expected.put("world", 1.0);
    expected.put("goodbye", 0.5);
    expected.put("hello", 0.5);

    assertThat(centroid).hasSameSizeAs(expected);
    assertThat(centroid).containsAllEntriesOf(expected);
  }
}
