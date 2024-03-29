package com.cyc.baseclient.datatype;

/*
 * #%L
 * File: DateConverter.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2015 Cycorp, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.cyc.base.annotation.CycObjectLibrary;
import com.cyc.base.annotation.CycTerm;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.Fort;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.cyc.baseclient.util.ParseException;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.cycobject.NautImpl;

/**
 * <P>DateConverter is designed to convert java-style dates to their corresponding
 * CycL representations and vice versa.
 *
 * @see java.util.Date
 * @see java.util.Calendar
 * @see com.cyc.baseclient.cycobject.Naut
 *
 * <P>Copyright (c) 2007 Cycorp, Inc. (Copyright is assigned to the United States Government under DFARS 252.227-7020).
 *
 *
 * @author baxter
 * @date January 15, 2008, 5:33 PM
 * @version $Id: DateConverter.java 155703 2015-01-05 23:15:30Z nwinant $
 */
@CycObjectLibrary
public class DateConverter extends DataTypeConverter<Date> {

  public static final int DAY_GRANULARITY = TimeGranularity.DAY.intValue();
  public static final int HOUR_GRANULARITY = TimeGranularity.HOUR.intValue();
  public static final int MILLISECOND_GRANULARITY = TimeGranularity.MILLISECOND.intValue();
  public static final int MINUTE_GRANULARITY = TimeGranularity.MINUTE.intValue();
  public static final int MONTH_GRANULARITY = TimeGranularity.MONTH.intValue();
  public static final int SECOND_GRANULARITY = TimeGranularity.SECOND.intValue();
  public static final int WEEK_GRANULARITY = TimeGranularity.WEEK.intValue();
//  public static final int SEASON_GRANULARITY = TimeGranularity.SEASON.intValue();
  public static final int YEAR_GRANULARITY = TimeGranularity.YEAR.intValue();

  //// Constructors
  /** Creates a new instance of DateConverter. */
  private DateConverter() {
    SHARED_INSTANCE = this;
  }

  //// Public Area
  /** Returns an instance of
   * <code>DateConverter</code>.
   *
   * If an instance has already been created, the existing one will be returned.
   * Otherwise, a new one will be created.
   *
   * @return The singleton instance of this class.
   */
  public static synchronized DateConverter getInstance() {
    DateConverter dateConverter = SHARED_INSTANCE;
    if (dateConverter == null) {
      dateConverter = new DateConverter();
    }
    return dateConverter;
  }

  /** Try to parse
   * <code>cycList</code> into a java
   * <code>Date</code>
   *
   * If the parse fails, prints a stack trace iff
   * <code>shouldReportFailure</code>
   * is non-null, and returns null.
   *
   * The Cyc date is assumed to be in the default time zone.
   *
   * @see TimeZone#getDefault
   * @deprecated Use CycNaut version.
   */
  static public Date parseCycDate(final CycArrayList cycList,
          final boolean shouldReportFailure) {
    final Object naut = NautImpl.convertIfPromising(cycList);
    if (naut instanceof Naut) {
      return parseCycDate((Naut) naut, shouldReportFailure);
    } else if (shouldReportFailure) {
      new IllegalArgumentException(cycList + " cannot be interpreted as a NAUT").printStackTrace();
    }
    return null;
  }

  /** Try to parse
   * <code>naut</code> into a java
   * <code>Date</code>
   *
   * @param naut a date-denoting Cyc NAUT.
   * @param shouldReportFailure If true, and the parse fails, prints a stack trace.
   * @return the Date object corresponding to naut, or null if parse fails.
   *
   * The Cyc date is assumed to be in the default time zone.
   * @see TimeZone#getDefault
   */
  static public Date parseCycDate(final Naut naut,
          final boolean shouldReportFailure) {
    return getInstance().parse(naut, shouldReportFailure);
  }

  /** Try to parse
   * <code>cycList</code> into a java
   * <code>Date</code> in a given time zone.
   *
   * If the parse fails, prints a stack trace iff
   * <code>shouldReportFailure</code>
   * is non-null, and returns null.
   *
   * @deprecated Use CycNaut version.
   */
  static public Date parseCycDate(final CycArrayList cycList, final TimeZone timeZone,
          final boolean shouldReportFailure) {
    final Object naut = NautImpl.convertIfPromising(cycList);
    if (naut instanceof Naut) {
      return parseCycDate((Naut) naut, timeZone, shouldReportFailure);
    } else if (shouldReportFailure) {
      new IllegalArgumentException(cycList + " cannot be converted to a NAUT.").printStackTrace();
    }
    return null;
  }

  /** Try to parse
   * <code>naut</code> into a java
   * <code>Date</code> in a given time zone.
   *
   * If the parse fails, prints a stack trace iff
   * <code>shouldReportFailure</code>
   * is non-null, and returns null.
   *
   * @param naut
   * @param timeZone
   * @param shouldReportFailure
   * @return the java.util.Date representation of <tt>naut</tt>
   */
  static public Date parseCycDate(final Naut naut, final TimeZone timeZone,
          final boolean shouldReportFailure) {
    try {
      return naut2Date(naut, timeZone);
    } catch (ParseException ex) {
      return getInstance().handleParseException(ex, shouldReportFailure);
    }
  }

  /** Try to parse
   * <code>cycList</code> into a java
   * <code>Date</code>
   *
   * Prints stack trace and returns null if the parse fails.
   *
   * The Cyc date is assumed to be in the default time zone.
   *
   * @see TimeZone#getDefault
   * @deprecated Use CycNaut version.
   */
  static public Date parseCycDate(final CycArrayList cycList) {
    return getInstance().parse(cycList);
  }

  /** Try to parse
   * <code>naut</code> into a java
   * <code>Date</code>
   *
   * Prints stack trace and returns null if the parse fails.
   *
   * @param naut
   * @return the corresponding Date object.
   *
   * The Cyc date is assumed to be in the default time zone.
   * @see TimeZone#getDefault
   */
  static public Date parseCycDate(final Naut naut) {
    return getInstance().parse(naut);
  }

  /** @return the precision of <tt>cycDate</tt> as a Calendar constant int.
   * @deprecated Use CycNaut version.
   */
  public static int getCycDatePrecision(CycArrayList cycDate) {
    return getCycDatePrecision(new NautImpl(cycDate));
  }

  /**
   * @param cycDate a date-denoting Cyc NAUT.
   * @return the precision of <tt>cycDate</tt> as a Calendar constant int. */
  public static int getCycDatePrecision(Naut cycDate) {
    final Object fn = cycDate.getOperator();
    if (YEAR_FN.equals(fn)) {
      return YEAR_GRANULARITY;
    }
    if (MONTH_FN.equals(fn)) {
      return MONTH_GRANULARITY;
    }
    if (DAY_FN.equals(fn)) {
      return DAY_GRANULARITY;
    }
    if (HOUR_FN.equals(fn)) {
      return HOUR_GRANULARITY;
    }
    if (MINUTE_FN.equals(fn)) {
      return MINUTE_GRANULARITY;
    }
    if (SECOND_FN.equals(fn)) {
      return SECOND_GRANULARITY;
    }
    if (MILLISECOND_FN.equals(fn)) {
      return MILLISECOND_GRANULARITY;
    }
    return -1;
  }

  /** Convert the date in
   * <code>date</code> to a CycL date term.
   *
   * @param date The date to be converted
   * @param granularity Indicates the desired granularity of the CycL term.
   * Should be an
   * <code>int</code> constant from this class,
   * e.g.
   * <code>DateConverter.YEAR_GRANULARITY</code> or
   * <code>DateConverter.SECOND_GRANULARITY</code>.
   * @return The Cyc term corresponding to date.
   * @throws ParseException if date cannot be converted.
   * */
  public static Naut toCycDate(final Date date, final int granularity) {
    return date2Naut(date, granularity);
  }

  public static int guessGranularity(final Date date) {
    return TimeGranularity.guessGranularity(date).intValue();
  }

  public static int guessGranularity(final long millis) {
    return TimeGranularity.guessGranularity(millis).intValue();
  }

  /** Convert the date in
   * <code>date</code> to a CycL date term.
   *
   * @param date The date to be converted
   * @return The Cyc term corresponding to date.
   * @throws ParseException if date cannot be converted.
   * */
  public static CycObject toCycDate(Date date) {
    return date2Naut(date, guessGranularity(date));
  }

  /** Convert the date in
   * <code>calendar</code> to a CycL date term.
   *
   * @param calendar
   * @param granularity Indicates the desired granularity of the CycL term.
   * Should be an
   * <code>int</code> constant from this class,
   * e.g.
   * <code>DateConverter.YEAR_GRANULARITY</code> or
   * <code>DateConverter.SECOND_GRANULARITY</code>.
   * @return the Naut representation of <tt>calendar</tt>
   * @see com.cyc.baseclient.cycobject.Naut
   * */
  public static Naut toCycDate(final Calendar calendar, final int granularity) {
    return calendar2Naut(calendar, granularity);
  }

  //// Protected Area
  @Override
  protected Date fromCycTerm(final CycObject cycObject) throws ParseException {
    final Naut naut;
    try {
      naut = (Naut) NautImpl.convertIfPromising(cycObject);
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException();
    }
    final Calendar calendar = Calendar.getInstance();
    calendar.clear();
    updateCalendar(naut, calendar);
    return calendar.getTime();
  }

  @Override
  protected Naut toCycTerm(Date date) {
    return date2Naut(date, guessGranularity(date));
  }

  //// Private Area
  static private Naut date2Naut(Date date, final int granularity) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar2Naut(calendar, granularity);
  }

  private static Naut calendar2Naut(final Calendar calendar,
          final int granularity) {
    Naut dateNaut = new NautImpl(YEAR_FN, calendar.get(YEAR_GRANULARITY));
//      if (granularity == WEEK_GRANULARITY) {
//        dateNaut = new NautImpl(WEEK_FN, calendar.get(WEEK_GRANULARITY), dateNaut);
//      } else
    if (granularity > YEAR_GRANULARITY) {
      dateNaut = new NautImpl(MONTH_FN, lookupMonth(calendar.get(
              MONTH_GRANULARITY)), dateNaut);
      if (granularity > MONTH_GRANULARITY) {
        dateNaut = new NautImpl(DAY_FN, calendar.get(DAY_GRANULARITY), dateNaut);
        if (granularity > DAY_GRANULARITY) {
          dateNaut = new NautImpl(HOUR_FN, calendar.get(HOUR_GRANULARITY),
                  dateNaut);
          if (granularity > Calendar.HOUR) {
            dateNaut = new NautImpl(MINUTE_FN, calendar.get(MINUTE_GRANULARITY),
                    dateNaut);
            if (granularity > MINUTE_GRANULARITY) {
              dateNaut = new NautImpl(SECOND_FN, calendar.get(SECOND_GRANULARITY),
                      dateNaut);
              if (granularity > SECOND_GRANULARITY) {
                dateNaut = new NautImpl(MILLISECOND_FN, calendar.get(
                        MILLISECOND_GRANULARITY), dateNaut);
              }
            }
          }
        }
      }
    }
    return dateNaut;
  }

  private static Date naut2Date(final Naut naut, final TimeZone timeZone) throws ParseException {
    return naut2Calendar(naut, timeZone).getTime();
  }

  private static Calendar naut2Calendar(final Naut naut,
          final TimeZone timeZone) throws ParseException {
    final Calendar calendar = Calendar.getInstance();
    calendar.clear();
    updateCalendar(naut, calendar);
    calendar.set(Calendar.ZONE_OFFSET, timeZone.getRawOffset());
    return calendar;
  }

  /** Set the time on
   * <code>calendar</code> based on the CycL date
   * <code>naut</code> */
  static private void updateCalendar(final Naut naut, final Calendar calendar) throws ParseException {
    final int arity = naut.getArity();
    final Fort functor = naut.getFunctor();
    if (arity < 1 || arity > 2) {
      throwParseException(naut);
    }
    final Object arg1 = naut.getArg(1);
    if (arity == 1 && YEAR_FN.equals(functor)) {
      final Integer yearNum = parseInteger(arg1, "year number");
      calendar.set(YEAR_GRANULARITY, yearNum);
    } else if (arity == 1) {
      throwParseException(naut);
    } else {
      final Object arg2 = naut.getArg(2);
      if (!(arg2 instanceof Naut)) {
        throwParseException(arg2);
      }
      if (MONTH_FN.equals(functor)) {
        if (!(arg1 instanceof CycConstantImpl)) {
          throw new ParseException(arg1 + " is not a valid CycL month.");
        }
        final int monthNum = lookupMonthNum((CycConstantImpl) arg1);
        if (monthNum < Calendar.JANUARY || monthNum > Calendar.DECEMBER) {
          throw new ParseException(arg1 + " is not a valid CycL month.");
        }
        updateCalendar((Naut) arg2, calendar);
        calendar.set(MONTH_GRANULARITY, monthNum);
      } else if (DAY_FN.equals(functor)) {
        final Object dayNum = parseInteger(arg1, "day number");
        updateCalendar((Naut) arg2, calendar);
        calendar.set(DAY_GRANULARITY, (Integer) dayNum);
      } else if (HOUR_FN.equals(functor)) {
        final Object hourNum = parseInteger(arg1, "hour number");
        updateCalendar((Naut) arg2, calendar);
        calendar.set(HOUR_GRANULARITY, (Integer) hourNum);
      } else if (MINUTE_FN.equals(functor)) {
        final Object minuteNum = parseInteger(arg1, "minute number");
        updateCalendar((Naut) arg2, calendar);
        calendar.set(MINUTE_GRANULARITY, (Integer) minuteNum);
      } else if (SECOND_FN.equals(functor)) {
        final Object secondNum = Integer.valueOf(arg1.toString());
        if (!(secondNum instanceof Integer && (Integer) secondNum >= 0
                && (Integer) secondNum < TimeGranularity.SECONDS_IN_A_MINUTE)) {
          throw new ParseException(secondNum + " is not a valid second number.");
        }
        updateCalendar((Naut) arg2, calendar);
        calendar.set(SECOND_GRANULARITY, (Integer) secondNum);
      } else if (MILLISECOND_FN.equals(functor)) {
        final Object millisecondNum = parseInteger(arg1, "millisecond number");
        updateCalendar((Naut) arg2, calendar);
        calendar.set(MILLISECOND_GRANULARITY, (Integer) millisecondNum);
      } else {
        throwParseException(naut);
      }
    }
  }

  static public CycConstantImpl lookupSeason(final String season) {
    if (season.equals("SU")) {
      return SUMMER;
    } else if (season.equals("SP")) {
      return SPRING;
    } else if (season.equals("FA")) {
      return FALL;
    } else if (season.equals("WI")) {
      return WINTER;
    } else {
      return null;
    }
  }

  static private CycConstantImpl lookupMonth(final int month) {
    ensureMonthArrayInitialized();
    return CYC_MONTH_TERMS[month];
  }

  static private int lookupMonthNum(CycConstantImpl cycMonth) {
    ensureMonthArrayInitialized();
    for (int monthNum = Calendar.JANUARY; monthNum <= Calendar.DECEMBER; monthNum++) {
      if (cycMonth.equals(CYC_MONTH_TERMS[monthNum])) {
        return monthNum;
      }
    }
    return -1;
  }

  private static void ensureMonthArrayInitialized() {
    if (CYC_MONTH_TERMS == null) {
      initializeCycMonthTerms();
    }
  }

  private static void initializeCycMonthTerms() {
    CYC_MONTH_TERMS = new CycConstantImpl[12];
    CYC_MONTH_TERMS[Calendar.JANUARY] = JANUARY;
    CYC_MONTH_TERMS[Calendar.FEBRUARY] = FEBRUARY;
    CYC_MONTH_TERMS[Calendar.MARCH] = MARCH;
    CYC_MONTH_TERMS[Calendar.APRIL] = APRIL;
    CYC_MONTH_TERMS[Calendar.MAY] = MAY;
    CYC_MONTH_TERMS[Calendar.JUNE] = JUNE;
    CYC_MONTH_TERMS[Calendar.JULY] = JULY;
    CYC_MONTH_TERMS[Calendar.AUGUST] = AUGUST;
    CYC_MONTH_TERMS[Calendar.SEPTEMBER] = SEPTEMBER;
    CYC_MONTH_TERMS[Calendar.OCTOBER] = OCTOBER;
    CYC_MONTH_TERMS[Calendar.NOVEMBER] = NOVEMBER;
    CYC_MONTH_TERMS[Calendar.DECEMBER] = DECEMBER;
  }

  public static boolean isCycDate(Object object) {
    if (object instanceof CycArrayList) {
      return parseCycDate((CycArrayList) object, false) != null;
    } else if (object instanceof Naut) {
      return parseCycDate((Naut) object, false) != null;
    } else {
      return false;
    }
  }
  //// Internal Rep
  
  @CycTerm(cycl="#$YearFn")
  public static final CycConstantImpl YEAR_FN = new CycConstantImpl("YearFn",
          new GuidImpl("bd58f29a-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$SeasonFn")
  public static final CycConstantImpl SEASON_FN = new CycConstantImpl("SeasonFn",
          new GuidImpl("c0fbe0cd-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$MonthFn")
  public static final CycConstantImpl MONTH_FN = new CycConstantImpl("MonthFn",
          new GuidImpl("be00fd8d-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$DayFn")
  public static final CycConstantImpl DAY_FN = new CycConstantImpl("DayFn",
          new GuidImpl("be00ff5b-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$HourFn")
  public static final CycConstantImpl HOUR_FN = new CycConstantImpl("HourFn",
          new GuidImpl("be010082-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$MinuteFn")
  public static final CycConstantImpl MINUTE_FN = new CycConstantImpl("MinuteFn",
          new GuidImpl("be0100d7-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$SecondFn")
  public static final CycConstantImpl SECOND_FN = new CycConstantImpl("SecondFn",
          new GuidImpl("be01010a-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$MilliSecondFn")
  public static final CycConstantImpl MILLISECOND_FN = new CycConstantImpl("MilliSecondFn",
          new GuidImpl("8c3206d3-1616-11d8-99b1-0002b361bcfc"));
  
  @CycTerm(cycl="#$January")
  public static final CycConstantImpl JANUARY = new CycConstantImpl("January",
          new GuidImpl("bd58b833-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$February")
  public static final CycConstantImpl FEBRUARY = new CycConstantImpl("February",
          new GuidImpl("bd58c2f7-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$March")
  public static final CycConstantImpl MARCH = new CycConstantImpl("March",
          new GuidImpl("bd58c2bd-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$April")
  public static final CycConstantImpl APRIL = new CycConstantImpl("April",
          new GuidImpl("bd58c279-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$May")
  public static final CycConstantImpl MAY = new CycConstantImpl("May",
          new GuidImpl("bd58c232-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$June")
  public static final CycConstantImpl JUNE = new CycConstantImpl("June",
          new GuidImpl("bd58c1f0-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$July")
  public static final CycConstantImpl JULY = new CycConstantImpl("July",
          new GuidImpl("bd58c1ad-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$August")
  public static final CycConstantImpl AUGUST = new CycConstantImpl("August",
          new GuidImpl("bd58c170-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$September")
  public static final CycConstantImpl SEPTEMBER = new CycConstantImpl("September",
          new GuidImpl("bd58c131-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$October")
  public static final CycConstantImpl OCTOBER = new CycConstantImpl("October",
          new GuidImpl("bd58c0ef-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$November")
  public static final CycConstantImpl NOVEMBER = new CycConstantImpl("November",
          new GuidImpl("bd58c0a5-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$December")
  public static final CycConstantImpl DECEMBER = new CycConstantImpl("December",
          new GuidImpl("bd58b8ba-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$CalendarSpring")
  public static final CycConstantImpl SPRING = new CycConstantImpl("CalendarSpring",
          new GuidImpl("be011735-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$CalendarSummer")
  public static final CycConstantImpl SUMMER = new CycConstantImpl("CalendarSummer",
          new GuidImpl("be011768-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$CalendarAutumn")
  public static final CycConstantImpl FALL = new CycConstantImpl("CalendarAutumn",
          new GuidImpl("be011790-9c29-11b1-9dad-c379636f7270"));
  
  @CycTerm(cycl="#$CalendarWinter")
  public static final CycConstantImpl WINTER = new CycConstantImpl("CalendarWinter",
          new GuidImpl("be0116f3-9c29-11b1-9dad-c379636f7270"));

  // Commented out regarding BASEAPI-63 - nwinant, 2014-08-18
  /*
  @CycTerm(cycl="#$NthSpecifiedDateTypeOfSubsumingDateFn")
  public static final CycConstantImpl NTH_SPECIFIED_DATE_TYPE_OF_SUBSUMING_DATE_FN =
          new CycConstantImpl("NthSpecifiedDateTypeOfSubsumingDateFn",
          new GuidImpl("fa33c621-7b6f-4eeb-9801-3acb990b0c8f"));
  */
  
  @CycTerm(cycl="#$CalendarWeek")
  public static final CycConstantImpl CALENDAR_WEEK = new CycConstantImpl("CalendarWeek",
          new GuidImpl("bd58c064-9c29-11b1-9dad-c379636f7270"));
  
  
  private static CycConstantImpl[] CYC_MONTH_TERMS = null;
  private static DateConverter SHARED_INSTANCE = null;
  //// Main
}
