package com.mitchseymour;

// not thread safe
public class SimilarityScore implements Comparable<SimilarityScore> {
  private final String genre;
  private final Double score;

  public SimilarityScore(String genre, Double score) {
    this.genre = genre;
    this.score = score;
  }

  public int compareTo(SimilarityScore o) {
    return Double.compare(o.getScore(), getScore());
  }

  public Double getScore() {
    return score;
  }

  public String getGenre() {
    return genre;
  }
}
