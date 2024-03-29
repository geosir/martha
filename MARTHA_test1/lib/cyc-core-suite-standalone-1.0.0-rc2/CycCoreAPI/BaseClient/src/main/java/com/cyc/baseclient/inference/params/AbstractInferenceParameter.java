package com.cyc.baseclient.inference.params;

/*
 * #%L
 * File: AbstractInferenceParameter.java
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

//// External Imports
import com.cyc.base.BaseClientRuntimeException;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.inference.InferenceParameterValue;
import com.cyc.base.inference.InferenceParameterValueDescription;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycSymbolImpl;


/**
 * <P>AbstractInferenceParameter is designed to...
 *
 * <P>Copyright (c) 2004 - 2006 Cycorp, Inc.  All rights reserved.
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>Use is subject to license terms.
 *
 * @author zelal
 * @date August 9, 2005, 8:49 PM
 * @version $Id: AbstractInferenceParameter.java 155703 2015-01-05 23:15:30Z nwinant $
 */
public abstract class AbstractInferenceParameter implements InferenceParameter {

  //// Constructors
  /** Creates a new instance of AbstractInferenceParameter.
   * @param propertyMap a map of inference parameters to their values.
   */
  public AbstractInferenceParameter(Map<CycSymbol, Object> propertyMap) {
    if (propertyMap == null) {
      throw new BaseClientRuntimeException("Got null parameter map");
    }
    if (propertyMap.size() < REQUIRED_SYMBOLS.length) {
      throw new BaseClientRuntimeException("Got too few symbols in map");
    }
    for (final CycSymbol property : REQUIRED_SYMBOLS) {
      if (!propertyMap.containsKey(property)) {
        throw new BaseClientRuntimeException("Expected key not found in map "
                + property + " for inference Parameter " + propertyMap.get(ID_SYMBOL));
      }
    }
    Object nameObj = verifyObjectType(propertyMap, NAME_SYMBOL, CycSymbol.class);
    Object idObj = verifyObjectType(propertyMap, ID_SYMBOL, Fort.class);
    Object shortDescObj = verifyObjectType(propertyMap, SHORT_DESC_SYMBOL, String.class);
    Object longDescObj = verifyObjectType(propertyMap, LONG_DESC_SYMBOL, String.class);
    Object queryStaticParamObj = verifyObjectType(propertyMap, QUERY_STATIC_PARAMETER_SYMBOL, CycSymbol.class);
    Object basicParamObj = verifyObjectType(propertyMap, BASIC_PARAMETER_SYMBOL, CycSymbol.class);
    Object alternateValueObj = propertyMap.get(ALTERNATE_VALUE_SYMBOL);
    if (!(alternateValueObj instanceof CycList)) {
      if (alternateValueObj.equals(CycObjectFactory.nil)) {
        alternateValueObj = null;
      } else {
        throw new BaseClientRuntimeException("Expected a CycList or nil; got " + alternateValueObj);
      }
    }
    init(propertyMap.get(DEFAULT_VALUE_SYMBOL),
            (CycSymbol) nameObj,
            (Fort) idObj,
            (String) shortDescObj,
            (String) longDescObj,
            (CycSymbol) basicParamObj,
            (CycSymbol) queryStaticParamObj,
            (CycList) alternateValueObj);
  }

  protected AbstractInferenceParameter(Object defaultValue, CycSymbol keyword,
          Fort id, String shortDescription, String longDescription,
          CycSymbol isBasicParameter, CycSymbol isQueryStaticParameter, CycList alternateValue) {
    init(defaultValue, keyword, id, shortDescription, longDescription, isBasicParameter,
            isQueryStaticParameter, alternateValue);
  }

  //// Public Area
  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(final Object value) {
    defaultValue = value;
  }

  @Override
  public Object canonicalizeValue(Object value) {
    return value;
  }

  @Override
  public CycSymbol getKeyword() {
    return keyword;
  }

  @Override
  public Fort getId() {
    return id;
  }

  @Override
  public String getLongDescription() {
    return longDescription;
  }

  @Override
  public String getShortDescription() {
    return shortDescription;
  }

  @Override
  public String getPrettyRepresentation(Object value) {
    if (getAlternateValue() != null && isAlternateValue(value)) {
      return getAlternateValue().getShortDescription();
    } else if (value instanceof Integer) {
      return NumberFormat.getInstance().format(value);
    } else if (value instanceof Number) {
      final NumberFormat nf = NumberFormat.getInstance();
      if (nf instanceof DecimalFormat) {
        ((DecimalFormat) nf).setMinimumFractionDigits(1);
      }
      return nf.format(value);
    } else if (value == null) {
      return "None";
    } else if (value instanceof CycSymbol && ((CycSymbol) value).toCanonicalString().equals(":ALL")) {
      return "All";
    } else if (value instanceof CycSymbol && ((CycSymbol) value).toCanonicalString().equals(":NONE")) {
      return "None";
    } else {
      return value.toString();
    }
  }

  @Override
  public boolean isBasicParameter() {
    return isBasicParameter;
  }

  @Override
  public boolean isQueryStaticParameter() {
    return isQueryStaticParameter;
  }

  @Override
  public InferenceParameterValueDescription getAlternateValue() {
    return alternateValue;
  }

  @Override
  public abstract boolean isValidValue(Object potentialValue);

  @Override
  public boolean isAlternateValue(Object value) {
    if (alternateValue == null) {
      return false;
    } else if (alternateValue.getValue() == null) {
      return value == null;
    } else {
      return alternateValue.getValue().equals(value);
    }
  }

  @Override
  public String toString() {
    String str = getKeyword().toString()
            + " shortDescription=\"" + getShortDescription() + "\""
            + " type=" + getClass().getName().replaceAll("^org\\.opencyc\\.inference\\.", "")
            + " isBasicParameter=" + isBasicParameter()
            + " isQueryStaticParameter=" + isQueryStaticParameter()
            + " defaultValue=" + getDefaultValue();
    if (getAlternateValue() != null) {
      str += " alternateValue=" + getAlternateValue();
    }
    return str;
  }

  //// Protected Area
  //// Private Area
  private void init(Object defaultValue, CycSymbol keyword,
          Fort id, String shortDescription, String longDescription,
          CycSymbol isBasicParameter, CycSymbol isQueryStaticParameter, CycList alternateValue) {
    this.defaultValue = canonicalizeValue(defaultValue);
    this.keyword = keyword;
    this.id = id;
    this.longDescription = longDescription;
    this.shortDescription = shortDescription;
    if (alternateValue != null) {
      this.alternateValue =
              new DefaultInferenceParameterValueDescription(DefaultInferenceParameterDescriptions.parsePropertyList(alternateValue));
      this.alternateValue.setValue(canonicalizeValue(this.alternateValue.getValue()));
    }

    if (CycObjectFactory.t.equals(isBasicParameter)) {
      this.isBasicParameter = true;
    } else if (CycObjectFactory.nil.equals(isBasicParameter)) {
      this.isBasicParameter = false;
    } else {
      throw new BaseClientRuntimeException("Got unexpected boolean value " + isBasicParameter);
    }

    if (CycObjectFactory.t.equals(isQueryStaticParameter)) {
      this.isQueryStaticParameter = true;
    } else if (CycObjectFactory.nil.equals(isQueryStaticParameter)) {
      this.isQueryStaticParameter = false;
    } else {
      throw new BaseClientRuntimeException("Got unexpected boolean value " + isQueryStaticParameter);
    }
  }

  private Object verifyObjectType(Map<CycSymbol, Object> propertyMap, CycSymbol symbol, Class aClass) {
    return DefaultInferenceParameterValueDescription.verifyObjectType(propertyMap, symbol, aClass);
  }
  //// Internal Rep
  private Object defaultValue;
  private CycSymbol keyword;
  private Fort id;
  private String shortDescription;
  private String longDescription;
  private boolean isBasicParameter;
  private boolean isQueryStaticParameter;
  private InferenceParameterValueDescription alternateValue = null;
  private final static CycSymbol DEFAULT_VALUE_SYMBOL = new CycSymbolImpl(":DEFAULT-VALUE");
  final static CycSymbol NAME_SYMBOL = new CycSymbolImpl(":NAME");
  final static CycSymbol ID_SYMBOL = new CycSymbolImpl(":ID");
  final static CycSymbol SHORT_DESC_SYMBOL = new CycSymbolImpl(":SHORT-DESC");
  final static CycSymbol LONG_DESC_SYMBOL = new CycSymbolImpl(":LONG-DESC");
  private final static CycSymbol BASIC_PARAMETER_SYMBOL = new CycSymbolImpl(":BASIC?");
  private final static CycSymbol QUERY_STATIC_PARAMETER_SYMBOL = new CycSymbolImpl(":QUERY-STATIC?");
  private final static CycSymbol ALTERNATE_VALUE_SYMBOL = new CycSymbolImpl(":ALTERNATE-VALUE");
  private final static CycSymbol[] REQUIRED_SYMBOLS = {DEFAULT_VALUE_SYMBOL,
    NAME_SYMBOL, ID_SYMBOL, SHORT_DESC_SYMBOL, LONG_DESC_SYMBOL,
    BASIC_PARAMETER_SYMBOL, QUERY_STATIC_PARAMETER_SYMBOL, ALTERNATE_VALUE_SYMBOL};

  //// Main
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
  }

  public Object parameterValueCycListApiValue(final InferenceParameterValue val) {
    return val.cycListApiValue();
  }
}
