package com.mitchseymour.classifier;

import com.mitchseymour.Document;
import com.mitchseymour.similarity.CosineSimilarity;
import com.mitchseymour.similarity.Similarity;
import com.mitchseymour.similarity.SimilarityScore;
import com.mitchseymour.store.Store;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple store-backed classifier that compares target documents to a document centroid, which is
 * computed using the average term frequencies across all documents in a given genre
 */
public class DocumentClassifier {
  private final String genre;

  /**
   * A store is used to save the raw term frequencies. We don't save doc-level term frequencies in
   * this class since it would duplicate storage responsibilities of {@link Store} implementations.
   */
  private final Store store;

  private Similarity similarity = new CosineSimilarity();

  /** Global term frequencies, which is needed to compute the document centroid */
  private Map<CharSequence, Double> globalTermCounts = new ConcurrentHashMap<>();

  /** Used for the document centroid calculation */
  private Map<CharSequence, Double> centroid = new ConcurrentHashMap<>();

  /**
   * Constructor
   *
   * @param genre The genre for this classifier
   */
  public DocumentClassifier(String genre, Store store) {
    this.genre = genre;
    this.store = store;
  }

  /**
   * Add a document to the classifier. We will also recompute the document centroid each time a
   * record is added.
   *
   * @param document The document to add
   */
  public void addDocument(Document document) {
    store.put(genre, document);

    Map<CharSequence, Double> tf = document.getTermFrequencies();

    // iterate over the document's term frequencies, and increment the global,
    // genre-level term counts
    for (Map.Entry<CharSequence, Double> entry : tf.entrySet()) {
      CharSequence term = entry.getKey();
      if (globalTermCounts.containsKey(term)) {
        // the term is already being tracked in the global term frequencies
        Double currentCount = globalTermCounts.get(term);
        globalTermCounts.put(term, currentCount + 1.0);
      } else {
        // this is the first time we've seen this term for this genre
        globalTermCounts.put(term, 1.0);
      }
    }

    computeDocumentCentroid();
  }

  /**
   * Remove a document from the classifier. We will also recompute the document centroid each time a
   * record is removed.
   *
   * @param docId the id of the document to remove
   */
  public void removeDocument(String docId) {
    Document doc = store.remove(genre, docId);
    if (doc == null) {
      // no document to remove
      return;
    }

    Map<CharSequence, Double> tf = doc.getTermFrequencies();

    // iterate over the document's term frequencies, and decrement the global,
    // genre-level term counts
    for (Map.Entry<CharSequence, Double> entry : tf.entrySet()) {
      CharSequence term = entry.getKey();
      if (globalTermCounts.containsKey(term)) {
        Double currentCount = globalTermCounts.get(term);
        // remove the term from the global counts if the new term count would
        // resolve to 0
        if (currentCount == 1.0) {
          globalTermCounts.remove(term);
          continue;
        }
        // decrement the global count for this term
        globalTermCounts.put(term, currentCount - 1.0);
      }
    }

    computeDocumentCentroid();
  }

  /**
   * Compute the document centroid. There are different possible implementations for this. This is a
   * simple implmentation that averages the term frequencies across all documents in this
   * classifier. Future implementations may wish to apply additional weighting to the terms (one
   * common approach being TF-IDF: {@see https://en.wikipedia.org/wiki/Tf-idf})
   */
  private void computeDocumentCentroid() {
    Double docCount = getDocCount();

    // Compute the average term frequencies across all documents
    Map<CharSequence, Double> avg = new HashMap<>();
    for (Map.Entry<CharSequence, Double> entry : globalTermCounts.entrySet()) {
      avg.put(entry.getKey(), entry.getValue() / docCount);
    }

    centroid = avg;
  }

  public Double getDocCount() {
    return new Double(store.get(genre).size());
  }

  /**
   * Compare the provided doc with the document centroid. This will tell us how closely a document
   * matches this genre
   *
   * @param compareTo The document to compare
   * @return A SimilarityScore instance containing the genre and score
   */
  public SimilarityScore similarityToDocumentCentroid(Document compareTo) {
    Double score =
        getSimilarity()
            .calculate(
                // term frequencies for the current doc
                getDocumentCentroid(),
                // term frequences for the comparison doc
                compareTo.getTermFrequencies());
    return new SimilarityScore(genre, score);
  }

  public Map<CharSequence, Double> getDocumentCentroid() {
    return centroid;
  }

  public Map<CharSequence, Double> getGlobalTermCounts() {
    return globalTermCounts;
  }

  public Similarity getSimilarity() {
    return similarity;
  }

  public void setSimilarity(Similarity similarity) {
    this.similarity = similarity;
  }
}
