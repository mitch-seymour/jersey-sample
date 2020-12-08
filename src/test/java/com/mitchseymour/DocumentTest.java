package com.mitchseymour;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DocumentTest {

  private static String readFile(String file, String dir) {
    String path = String.format("%s/%s", dir, file);
    try {
      URL url = Resources.getResource(path);
      return Resources.toString(url, StandardCharsets.UTF_8);
    } catch (IOException io) {
      throw new RuntimeException("could not read test file: " + path, io);
    }
  }

  @ParameterizedTest(name = "term frequencies can be calculated (sample record: {0})")
  @CsvSource({"en36197495", "en36237371"})
  void testTermFrequencies(String id) {
    // read the test input / expected output for this document ID
    String dir = String.format("tf/%s", id);
    String contents = readFile("in.txt", dir);
    String[] tfLines = readFile("expected.txt", dir).split("\n");

    // build a map to hold the expected term frequencies (tf)
    Map<CharSequence, Double> expectedTf = new HashMap<>();
    for (String line : tfLines) {
      String[] parts = line.split(",");
      if (parts.length == 2) {
        expectedTf.put(parts[0], Double.parseDouble(parts[1]));
      }
    }

    // construct a proper document from the file contents
    Document doc = new Document(id, contents);
    Map<CharSequence, Double> tf = doc.getTermFrequencies();

    // assert that the actual tf equals the expected tf
    assertThat(tf.size()).isEqualTo(expectedTf.size());
    assertThat(tf).containsExactlyEntriesOf(expectedTf);
  }
}
