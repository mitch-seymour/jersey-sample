package com.mitchseymour;

import com.mitchseymour.classifier.DocumentClassifier;
import com.mitchseymour.similarity.CosineSimilarity;
import com.mitchseymour.similarity.SimilarityScore;
import com.mitchseymour.store.InMemoryStore;
import com.mitchseymour.store.Store;
import com.mitchseymour.store.StoreException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.*;

/** Handler for operations related to calculating similarity scores */
public class RequestHandler {
  static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

  /**
   * The store to be used for saving documents. In this example, we use a simple in-memory store. We
   * would want to swap this out with a persistent store before going to production. The store can
   * be interacted with directly, but we also configure our document classifiers with this store
   * since they require some mechanism of saving previously seen documents for a given genre.
   */
  private Store store = new InMemoryStore();

  /**
   * A map of classifiers than be used for retrieving similarity scores between a provided document
   * and a given genre. Keys are genre names
   */
  private Map<String, DocumentClassifier> genreClassifiers = new ConcurrentHashMap<>();

  /**
   * Implement for part 1
   *
   * @param documentText text of the document on which calculate term frequencies
   * @return a map where the keys are the terms from the document and the value is the frequency of
   *     that term.
   */
  public Map<CharSequence, Double> getTermFrequencies(String documentText) {
    Document doc = new Document(documentText);
    return doc.getTermFrequencies();
  }

  /**
   * Implement for part 2 Calculates the similarity score of the documents to each other.
   *
   * @param doc1Text text of the first document
   * @param doc2Text text of the second document
   * @return The similarity score. The range of values will be algorithm specific.
   */
  public Double getSimilarityScore(String doc1Text, String doc2Text) {
    Document doc1 = new Document(doc1Text);
    Document doc2 = new Document(doc2Text);
    return doc1.similarityToDocument(doc2, new CosineSimilarity()).getScore();
  }

  /**
   * Implement for part 3 Add a document to the internal documents which getPopularSimilarity will
   * compare against
   *
   * @param genre the genre
   * @param docId
   * @param documentText
   */
  public void addDocumentToGenre(String genre, String docId, String documentText) {
    Document doc = new Document(docId, documentText);

    try {
      // add the document to the classifier and store
      genreClassifiers.putIfAbsent(
          genre,
          // initialize a new classifier if this genre hasn't been seen before
          new DocumentClassifier(genre, store));
      DocumentClassifier classifier = genreClassifiers.get(genre);
      classifier.addDocument(doc);
    } catch (Exception e) {
      log.error("Could not add document: {}", docId, e);
      throw e;
    }
  }

  /**
   * Implement for part 3 Removes a document from the specified genre
   *
   * @param genre genre to modify
   * @param docId document to remove
   */
  public void removeDocumentFromGenre(String genre, String docId) {
    try {
      // remove the document from the classifier and store
      DocumentClassifier classifier = genreClassifiers.get(genre);
      if (classifier == null) {
        log.warn("Genre does not exist: {}", genre);
        return;
      }
      classifier.removeDocument(docId);
    } catch (Exception e) {
      log.error("Could not remove document: {}", docId, e);
      throw e;
    }
  }

  /**
   * Implement for part 3
   *
   * @param genre
   * @return list of document ids in a specific genre
   */
  public List<String> getDocumentsInGenre(String genre) {
    // retrieve documents for the specified genre from the store
    try {
      return store.get(genre);
    } catch (StoreException se) {
      log.error("Could get documents for genre: {}", genre, se);
      throw se;
    }
  }

  /**
   * Implement for part 4. Returns the list of genres which are most similar to the document text
   * specified
   *
   * @param documentText the text of the document to compare
   * @param n the number of genres to return in the list
   * @return list of the closest Genres sorted from most similar genre to least similar
   */
  public List<String> getNClosestGenres(String documentText, Integer n) {
    // create a set that is sorted based on the centroid similarity
    TreeSet<SimilarityScore> scores = new TreeSet<>();
    genreClassifiers.forEach(
        (genre, classifier) -> {
          if (classifier.getDocCount() > 0) {
            // classifier.setSimilarity(...);
            Document compareTo = new Document(documentText);
            scores.add(classifier.similarityToDocumentCentroid(compareTo));
          }
        });

    // return the n closest genres
    return scores
        .stream()
        // .filter(score -> score.getScore() > 0.0)
        .map(score -> score.getGenre())
        .limit(n)
        .collect(Collectors.toList());
  }
}
