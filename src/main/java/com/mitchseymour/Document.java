package com.mitchseymour;

import com.mitchseymour.similarity.CosineSimilarity;
import com.mitchseymour.similarity.Similarity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Document {
  private String id = "";
  private String text;

  public Document(String text) {
    this.text = text;
  }

  public Document(String id, String text) {
    this.id = id;
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  /**
   * Convert this document's text to a stream of words. An idea for a future implementation of this
   * method is to remove stop words, since they add very little value to the meaning of the
   * underlying text ({@see https://en.wikipedia.org/wiki/Stop_word}).
   *
   * <p>I did not remove stop words here since the example term frequencies in the instructions
   * included stop words.
   *
   * @return A stream of trimmed / lower cased words, with punctuation removed
   */
  Stream<String> words() {
    return Arrays.stream(
            // remove punctuation
            getText().replaceAll("\\p{P}", "").split(" "))
        // clean the individual words
        .map(String::toLowerCase)
        .map(String::trim)
        .filter(str -> !str.isEmpty());
  }

  /**
   * Get a map of term frequencies (i.e. how many times each term occurs in the current document).
   *
   * @return A map where the keys are terms and the values are frequencies
   */
  public Map<CharSequence, Double> getTermFrequencies() {
    Map<CharSequence, Double> terms = new HashMap<>();
    return words()
        .collect(
            Collectors.groupingBy(
                k -> k, () -> terms, Collectors.reducing(0.0, e -> 1.0, Double::sum)));
  }

  /**
   * Compare this document to another document using the provided similarity algorithm
   *
   * @param compareTo
   * @return
   */
  public SimilarityScore similarityToDocument(Document compareTo) {
    return similarityToDocument(compareTo, new CosineSimilarity());
  }

  /**
   * Compare this document to another document using the provided similarity algorithm
   *
   * @param compareTo
   * @return
   */
  public SimilarityScore similarityToDocument(Document compareTo, Similarity similarity) {
    Double score =
        similarity.calculate(
            // term frequencies for the current doc
            getTermFrequencies(),
            // term frequences for the comparison doc
            compareTo.getTermFrequencies());
    return new SimilarityScore("", score);
  }
}
