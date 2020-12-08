package com.mitchseymour.similarity;

import java.util.Map;

public interface Similarity {

  public Double calculate(
      final Map<CharSequence, Double> leftVector, final Map<CharSequence, Double> rightVector);
}
