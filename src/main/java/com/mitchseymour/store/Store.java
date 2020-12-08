package com.mitchseymour.store;

import com.mitchseymour.Document;
import java.util.List;

/** An interface for storing documents and genres */
public interface Store {

  public List<String> get(String genre) throws StoreException;

  public void put(String genre, Document doc) throws StoreException;

  public Document remove(String genre, String docId) throws StoreException;

  public void close();
}
