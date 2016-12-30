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
 * Model definition for TsgMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the tsgApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class TsgMessage extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime messageDt;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String messageTxt;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String title;

  /**
   * @return value or {@code null} for none
   */
  public DateTime getMessageDt() {
    return messageDt;
  }

  /**
   * @param messageDt messageDt or {@code null} for none
   */
  public TsgMessage setMessageDt(DateTime messageDt) {
    this.messageDt = messageDt;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getMessageTxt() {
    return messageTxt;
  }

  /**
   * @param messageTxt messageTxt or {@code null} for none
   */
  public TsgMessage setMessageTxt(java.lang.String messageTxt) {
    this.messageTxt = messageTxt;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getTitle() {
    return title;
  }

  /**
   * @param title title or {@code null} for none
   */
  public TsgMessage setTitle(java.lang.String title) {
    this.title = title;
    return this;
  }

  @Override
  public TsgMessage set(String fieldName, Object value) {
    return (TsgMessage) super.set(fieldName, value);
  }

  @Override
  public TsgMessage clone() {
    return (TsgMessage) super.clone();
  }

}
