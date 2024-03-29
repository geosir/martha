package com.cyc.baseclient.util;

/*
 * #%L
 * File: CycWorkerListener.java
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

import com.cyc.baseclient.ui.SwingWorker;
import com.cyc.baseclient.ui.CycWorker;
import java.util.*;

/**
 * This is an interface to be used by classes that wishes to listen
 * to a CycWorker's events.
 */
public interface CycWorkerListener extends EventListener {
    
  /** 
   * Called when the CycWorker is started. 
   * @param evt The CycWorkerEvent object.
   **/
  void notifyWorkerStarted(CycWorkerEvent evt);
    
  /** 
   * Called when the CycWorker is interrupted. 
   * @param evt The CycWorkerEvent object.
   **/
  void notifyWorkerInterrupted(CycWorkerEvent evt);
    
  /** 
   * Called when the CycWorker is finished. 
   * @param evt The CycWorkerEvent object.
   **/
  void notifyWorkerFinished(CycWorkerEvent evt);
}
