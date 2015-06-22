package com.cyc.baseclient.inference.params;

/*
 * #%L
 * File: DefaultUntypedInferenceParameter.java
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
import java.util.Map;

/**
 * <P>DefaultUntypedInferenceParameter is designed to...
 *
 * <P>Copyright (c) 2004 - 2009 Cycorp, Inc.  All rights reserved.
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>Use is subject to license terms.
 *
 * @author baxter
 * @date June 19, 2009
 * @version $Id: DefaultUntypedInferenceParameter.java 155703 2015-01-05 23:15:30Z nwinant $
 */
class DefaultUntypedInferenceParameter extends AbstractInferenceParameter {

  public DefaultUntypedInferenceParameter(Map<CycSymbol, Object> propertyMap) {
    super(propertyMap);
  }

  @Override
  public boolean isValidValue(Object potentialValue) {
    return true;
  }

 
  @Override
  public Object parameterValueCycListApiValue(Object val) {
    return val;
  }
}
