/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import { LmapCapabilityTask } from './lmap-capability/lmap-capability-task.model';

export class LmapCapability {
  /**
   * A short description of the software implementing the Measurement Agent.
   * This should include the version number of the Measurement Agent software.
   */
  public version: string;

  /**
   * An optional unordered set of tags that provide additional information about the capabilities of the Measurement Agent.
   */
  public tag: string[];

  /**
   * A list of Tasks that the Measurement Agent supports.
   */
  public tasks: LmapCapabilityTask[];
}
