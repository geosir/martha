package com.cyc.base.inference;

/*
 * #%L
 * File: InferenceWorkerListener.java
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

import java.util.EventListener; 
import java.util.List;

/** *  <P> This interface is one that must be implemented by anyone
 * wishing to listen in on events generated by a InferenceWorker.
 *
 * @see InferenceWorker
 * @author tbrussea, zelal
 * @version $Id: InferenceWorkerListener.java 155703 2015-01-05 23:15:30Z nwinant $
 */
public interface InferenceWorkerListener extends EventListener {
  
  public void notifyInferenceCreated(InferenceWorker inferenceWorker);
  
  public void notifyInferenceStatusChanged(InferenceStatus oldStatus, InferenceStatus newStatus, 
    InferenceSuspendReason suspendReason, InferenceWorker inferenceWorker);
  
  public void notifyInferenceAnswersAvailable(InferenceWorker inferenceWorker, List newAnswers);

  public void notifyInferenceTerminated(InferenceWorker inferenceWorker, Exception e);

}