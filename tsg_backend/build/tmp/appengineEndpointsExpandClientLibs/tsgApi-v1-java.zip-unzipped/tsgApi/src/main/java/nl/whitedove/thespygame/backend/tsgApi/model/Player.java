/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2016-10-17 16:43:55 UTC)
 * on 2016-12-24 at 19:27:52 UTC 
 * Modify at your own risk.
 */

package nl.whitedove.thespygame.backend.tsgApi.model;

/**
 * Model definition for Player.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the tsgApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Player extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String answer;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean correctAnswer;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean isSpy;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String name;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer points;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String role;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String token;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getAnswer() {
    return answer;
  }

  /**
   * @param answer answer or {@code null} for none
   */
  public Player setAnswer(java.lang.String answer) {
    this.answer = answer;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getCorrectAnswer() {
    return correctAnswer;
  }

  /**
   * @param correctAnswer correctAnswer or {@code null} for none
   */
  public Player setCorrectAnswer(java.lang.Boolean correctAnswer) {
    this.correctAnswer = correctAnswer;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public Player setId(java.lang.String id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getIsSpy() {
    return isSpy;
  }

  /**
   * @param isSpy isSpy or {@code null} for none
   */
  public Player setIsSpy(java.lang.Boolean isSpy) {
    this.isSpy = isSpy;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getName() {
    return name;
  }

  /**
   * @param name name or {@code null} for none
   */
  public Player setName(java.lang.String name) {
    this.name = name;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getPoints() {
    return points;
  }

  /**
   * @param points points or {@code null} for none
   */
  public Player setPoints(java.lang.Integer points) {
    this.points = points;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getRole() {
    return role;
  }

  /**
   * @param role role or {@code null} for none
   */
  public Player setRole(java.lang.String role) {
    this.role = role;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getToken() {
    return token;
  }

  /**
   * @param token token or {@code null} for none
   */
  public Player setToken(java.lang.String token) {
    this.token = token;
    return this;
  }

  @Override
  public Player set(String fieldName, Object value) {
    return (Player) super.set(fieldName, value);
  }

  @Override
  public Player clone() {
    return (Player) super.clone();
  }

}