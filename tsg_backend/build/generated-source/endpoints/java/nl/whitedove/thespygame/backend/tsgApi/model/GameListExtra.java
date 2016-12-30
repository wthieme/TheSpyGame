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
 * Model definition for GameListExtra.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the tsgApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class GameListExtra extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<GameInfo> games;

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<GameInfo> getGames() {
    return games;
  }

  /**
   * @param games games or {@code null} for none
   */
  public GameListExtra setGames(java.util.List<GameInfo> games) {
    this.games = games;
    return this;
  }

  @Override
  public GameListExtra set(String fieldName, Object value) {
    return (GameListExtra) super.set(fieldName, value);
  }

  @Override
  public GameListExtra clone() {
    return (GameListExtra) super.clone();
  }

}
