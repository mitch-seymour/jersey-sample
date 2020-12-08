package com.mitchseymour.store;

import com.mitchseymour.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple in-memory store for storing documents and their related genres. We could replace this
 * with a persistence store if our requirements change.
 */
public class InMemoryStore implements Store {

  /** A mapping of docIds -> documents */
  private Map<String, Document> documents = new ConcurrentHashMap<>();

  /** A mapping of genre -> docIds */
  private Map<String, TreeSet<String>> genres = new ConcurrentHashMap<>();

  /**
   * Get the document IDs associated with a given genre
   *
   * @param genre The genre to retrieve document IDs for
   * @return a list of document IDs
   */
  @Override
  public List<String> get(String genre) {
    TreeSet<String> docIds = genres.get(genre);
    if (docIds == null) {
      return Collections.emptyList();
    }
    return new ArrayList<String>(docIds);
  }

  /**
   * Associate a given document with a genre
   *
   * @param genre
   * @param doc
   */
  @Override
  public void put(String genre, Document doc) {
    String docId = doc.getId();
    genres.merge(
        genre,
        // initialize the collection if we haven't see the genre before
        new TreeSet<>(Collections.singletonList(docId)),
        (current, incoming) -> {
          current.add(docId);
          documents.put(docId, doc);
          return current;
        });
  }

  /** Disassociate a doc ID from a given genre */
  @Override
  public Document remove(String genre, String docId) {
    Document doc = documents.get(docId);
    genres.merge(
        genre,
        // default collection to operate on if we haven't see the genre before
        new TreeSet<>(),
        (current, incoming) -> {
          // remove the doc ID from the genre -> docIDs mapping
          current.remove(docId);

          // remove the actual document
          documents.remove(docId);
          return current;
        });
    return doc;
  }

  @Override
  public void close() {
    /*!
     * Nothing is needed for this particular implementation. A persistent store
     * implementation may need to explicitly close files / handles
     */
  }
}
