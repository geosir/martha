package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: ELMtNart.java
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

import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.ELMt;
import com.cyc.baseclient.CycObjectFactory;

/**
 * Provides the container for the ELMt NART (Epistemlogical Level Microtheory
 * Non Atomic Reified Term).<p>
 *
 * @version $Id: ELMtNart.java 155703 2015-01-05 23:15:30Z nwinant $
 * @author Tony Brusseau
 */
public class ELMtNart extends NartImpl implements ELMt {
  
  /** Creates a new instance of ELMtNart */
  private ELMtNart(Nart nart) {
    super(nart.toCycList());
  }
  
  /**
   * Returns a new ELMtNart given a Nart.  Note, use the
   * factory method in the CycAccess to create these.
   */
  public static ELMtNart makeELMtNart(Nart nart) {
    return new ELMtNart(nart);
  }
}
