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
 * on 2017-04-02 at 14:10:17 UTC 
 * Modify at your own risk.
 */

package nl.whitedove.thespygame.backend.tsgApi.model;

/**
 * Model definition for DateTime.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the tsgApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class DateTime extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean afterNow;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean beforeNow;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer centuryOfEra;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Chronology chronology;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer dayOfMonth;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer dayOfWeek;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer dayOfYear;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean equalNow;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer era;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer hourOfDay;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long millis;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer millisOfDay;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer millisOfSecond;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer minuteOfDay;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer minuteOfHour;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer monthOfYear;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer secondOfDay;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer secondOfMinute;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer weekOfWeekyear;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer weekyear;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer year;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer yearOfCentury;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer yearOfEra;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTimeZone zone;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getAfterNow() {
    return afterNow;
  }

  /**
   * @param afterNow afterNow or {@code null} for none
   */
  public DateTime setAfterNow(java.lang.Boolean afterNow) {
    this.afterNow = afterNow;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getBeforeNow() {
    return beforeNow;
  }

  /**
   * @param beforeNow beforeNow or {@code null} for none
   */
  public DateTime setBeforeNow(java.lang.Boolean beforeNow) {
    this.beforeNow = beforeNow;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getCenturyOfEra() {
    return centuryOfEra;
  }

  /**
   * @param centuryOfEra centuryOfEra or {@code null} for none
   */
  public DateTime setCenturyOfEra(java.lang.Integer centuryOfEra) {
    this.centuryOfEra = centuryOfEra;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public Chronology getChronology() {
    return chronology;
  }

  /**
   * @param chronology chronology or {@code null} for none
   */
  public DateTime setChronology(Chronology chronology) {
    this.chronology = chronology;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getDayOfMonth() {
    return dayOfMonth;
  }

  /**
   * @param dayOfMonth dayOfMonth or {@code null} for none
   */
  public DateTime setDayOfMonth(java.lang.Integer dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getDayOfWeek() {
    return dayOfWeek;
  }

  /**
   * @param dayOfWeek dayOfWeek or {@code null} for none
   */
  public DateTime setDayOfWeek(java.lang.Integer dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getDayOfYear() {
    return dayOfYear;
  }

  /**
   * @param dayOfYear dayOfYear or {@code null} for none
   */
  public DateTime setDayOfYear(java.lang.Integer dayOfYear) {
    this.dayOfYear = dayOfYear;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getEqualNow() {
    return equalNow;
  }

  /**
   * @param equalNow equalNow or {@code null} for none
   */
  public DateTime setEqualNow(java.lang.Boolean equalNow) {
    this.equalNow = equalNow;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getEra() {
    return era;
  }

  /**
   * @param era era or {@code null} for none
   */
  public DateTime setEra(java.lang.Integer era) {
    this.era = era;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getHourOfDay() {
    return hourOfDay;
  }

  /**
   * @param hourOfDay hourOfDay or {@code null} for none
   */
  public DateTime setHourOfDay(java.lang.Integer hourOfDay) {
    this.hourOfDay = hourOfDay;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getMillis() {
    return millis;
  }

  /**
   * @param millis millis or {@code null} for none
   */
  public DateTime setMillis(java.lang.Long millis) {
    this.millis = millis;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getMillisOfDay() {
    return millisOfDay;
  }

  /**
   * @param millisOfDay millisOfDay or {@code null} for none
   */
  public DateTime setMillisOfDay(java.lang.Integer millisOfDay) {
    this.millisOfDay = millisOfDay;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getMillisOfSecond() {
    return millisOfSecond;
  }

  /**
   * @param millisOfSecond millisOfSecond or {@code null} for none
   */
  public DateTime setMillisOfSecond(java.lang.Integer millisOfSecond) {
    this.millisOfSecond = millisOfSecond;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getMinuteOfDay() {
    return minuteOfDay;
  }

  /**
   * @param minuteOfDay minuteOfDay or {@code null} for none
   */
  public DateTime setMinuteOfDay(java.lang.Integer minuteOfDay) {
    this.minuteOfDay = minuteOfDay;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getMinuteOfHour() {
    return minuteOfHour;
  }

  /**
   * @param minuteOfHour minuteOfHour or {@code null} for none
   */
  public DateTime setMinuteOfHour(java.lang.Integer minuteOfHour) {
    this.minuteOfHour = minuteOfHour;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getMonthOfYear() {
    return monthOfYear;
  }

  /**
   * @param monthOfYear monthOfYear or {@code null} for none
   */
  public DateTime setMonthOfYear(java.lang.Integer monthOfYear) {
    this.monthOfYear = monthOfYear;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getSecondOfDay() {
    return secondOfDay;
  }

  /**
   * @param secondOfDay secondOfDay or {@code null} for none
   */
  public DateTime setSecondOfDay(java.lang.Integer secondOfDay) {
    this.secondOfDay = secondOfDay;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getSecondOfMinute() {
    return secondOfMinute;
  }

  /**
   * @param secondOfMinute secondOfMinute or {@code null} for none
   */
  public DateTime setSecondOfMinute(java.lang.Integer secondOfMinute) {
    this.secondOfMinute = secondOfMinute;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getWeekOfWeekyear() {
    return weekOfWeekyear;
  }

  /**
   * @param weekOfWeekyear weekOfWeekyear or {@code null} for none
   */
  public DateTime setWeekOfWeekyear(java.lang.Integer weekOfWeekyear) {
    this.weekOfWeekyear = weekOfWeekyear;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getWeekyear() {
    return weekyear;
  }

  /**
   * @param weekyear weekyear or {@code null} for none
   */
  public DateTime setWeekyear(java.lang.Integer weekyear) {
    this.weekyear = weekyear;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getYear() {
    return year;
  }

  /**
   * @param year year or {@code null} for none
   */
  public DateTime setYear(java.lang.Integer year) {
    this.year = year;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getYearOfCentury() {
    return yearOfCentury;
  }

  /**
   * @param yearOfCentury yearOfCentury or {@code null} for none
   */
  public DateTime setYearOfCentury(java.lang.Integer yearOfCentury) {
    this.yearOfCentury = yearOfCentury;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getYearOfEra() {
    return yearOfEra;
  }

  /**
   * @param yearOfEra yearOfEra or {@code null} for none
   */
  public DateTime setYearOfEra(java.lang.Integer yearOfEra) {
    this.yearOfEra = yearOfEra;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public DateTimeZone getZone() {
    return zone;
  }

  /**
   * @param zone zone or {@code null} for none
   */
  public DateTime setZone(DateTimeZone zone) {
    this.zone = zone;
    return this;
  }

  @Override
  public DateTime set(String fieldName, Object value) {
    return (DateTime) super.set(fieldName, value);
  }

  @Override
  public DateTime clone() {
    return (DateTime) super.clone();
  }

}
