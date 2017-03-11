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
 * (build: 2017-02-15 17:18:02 UTC)
 * on 2017-03-11 at 23:28:20 UTC 
 * Modify at your own risk.
 */

package nl.whitedove.thespygame.backend.tsgApi.model;

/**
 * Model definition for DateTimeZone.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the tsgApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class DateTimeZone extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean fixed;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String id;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getFixed() {
    return fixed;
  }

  /**
   * @param fixed fixed or {@code null} for none
   */
  public DateTimeZone setFixed(java.lang.Boolean fixed) {
    this.fixed = fixed;
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
  public DateTimeZone setId(java.lang.String id) {
    this.id = id;
    return this;
  }

  @Override
  public DateTimeZone set(String fieldName, Object value) {
    return (DateTimeZone) super.set(fieldName, value);
  }

  @Override
  public DateTimeZone clone() {
    return (DateTimeZone) super.clone();
  }

}
