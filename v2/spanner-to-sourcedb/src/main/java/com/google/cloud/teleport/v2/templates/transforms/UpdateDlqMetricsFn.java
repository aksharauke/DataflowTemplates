/*
 * Copyright (C) 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.v2.templates.transforms;

import com.google.cloud.teleport.v2.values.FailsafeElement;
import java.io.Serializable;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Updates the permanent error/ requeue for retry counter. */
public class UpdateDlqMetricsFn
    extends DoFn<FailsafeElement<String, String>, FailsafeElement<String, String>>
    implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(UpdateDlqMetricsFn.class);

  private final Counter failedEvents =
      Metrics.counter(UpdateDlqMetricsFn.class, "severe_error_count");

  private final Counter requeueEvents =
      Metrics.counter(UpdateDlqMetricsFn.class, "element_requeued_for_retry_count");

  /* The run mode, whether it is regular or retry. */
  private final Boolean isRegularRunMode;

  public UpdateDlqMetricsFn(Boolean isRegularRunMode) {
    this.isRegularRunMode = isRegularRunMode;
  }

  @ProcessElement
  public void processElement(ProcessContext context) {

    if (isRegularRunMode) {
      failedEvents.inc();
    } else {
      requeueEvents.inc();
    }
    context.output(context.element());
  }
}
