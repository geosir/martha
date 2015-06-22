package com.cyc.base.inference;

/*
 * #%L
 * File: InferenceParameterValue.java
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

import com.cyc.base.cycobject.CycSymbol;

/**
 * A representation of a value for an inference parameter.
 * @see com.cyc.baseclient.inference.params.InferenceParameter
 * @author baxter
 */
public interface InferenceParameterValue {

  /**
   * Get the string API representation for this value.
   * @return the string API representation.
   */
  String stringApiValue();

  /**
   * Get the CycList API representation for this value.
   * @return the CycList API representation.
   */
  Object cycListApiValue();

  /**
   * Get the {@link CycSymbol} version of this value, if applicable.
   * @return the symbol.
   */
  CycSymbol getCycSymbol();
}
