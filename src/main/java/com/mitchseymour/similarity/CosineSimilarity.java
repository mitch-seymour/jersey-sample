/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mitchseymour.similarity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Adapted from org.apache.commons:commons-text:1.1. The main change I made was to make the
 * similarity calculation work with Doubles instead of Integers, which is required since we're
 * computing a document centroid (average term frequencies across a document collection) which
 * cannot be represented using Integers. I also implemented an interface to allow us to swap
 * different similarity algorithms in the classifier code.
 */
public class CosineSimilarity implements Similarity {

  /**
   * Calculates the cosine similarity for two given vectors.
   *
   * @param leftVector left vector
   * @param rightVector right vector
   * @return cosine similarity between the two vectors
   */
  @Override
  public Double calculate(
      final Map<CharSequence, Double> leftVector, final Map<CharSequence, Double> rightVector) {
    if (leftVector == null || rightVector == null) {
      throw new IllegalArgumentException("Vectors must not be null");
    }

    final Set<CharSequence> intersection = getIntersection(leftVector, rightVector);

    final double dotProduct = dot(leftVector, rightVector, intersection);
    double d1 = 0.0d;
    for (final Double value : leftVector.values()) {
      d1 += Math.pow(value, 2);
    }
    double d2 = 0.0d;
    for (final Double value : rightVector.values()) {
      d2 += Math.pow(value, 2);
    }
    double cosineSimilarity;
    if (d1 <= 0.0 || d2 <= 0.0) {
      cosineSimilarity = 0.0;
    } else {
      cosineSimilarity = (double) (dotProduct / (double) (Math.sqrt(d1) * Math.sqrt(d2)));
    }
    return cosineSimilarity;
  }

  /**
   * Returns a set with strings common to the two given maps.
   *
   * @param leftVector left vector map
   * @param rightVector right vector map
   * @return common strings
   */
  private Set<CharSequence> getIntersection(
      final Map<CharSequence, Double> leftVector, final Map<CharSequence, Double> rightVector) {
    final Set<CharSequence> intersection = new HashSet<>(leftVector.keySet());
    intersection.retainAll(rightVector.keySet());
    return intersection;
  }

  /**
   * Computes the dot product of two vectors. It ignores remaining elements. It means that if a
   * vector is longer than other, then a smaller part of it will be used to compute the dot product.
   *
   * @param leftVector left vector
   * @param rightVector right vector
   * @param intersection common elements
   * @return the dot product
   */
  private double dot(
      final Map<CharSequence, Double> leftVector,
      final Map<CharSequence, Double> rightVector,
      final Set<CharSequence> intersection) {
    long dotProduct = 0;
    for (final CharSequence key : intersection) {
      dotProduct += leftVector.get(key) * rightVector.get(key);
    }
    return dotProduct;
  }
}
