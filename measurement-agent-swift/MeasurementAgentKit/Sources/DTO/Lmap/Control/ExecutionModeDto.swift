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

import Foundation

/// TODO
enum ExecutionModeDto: String, Codable {

    /// The Actions of the Schedule are executed sequentially.
    case sequential = "SEQUENTIAL"

    /// The Actions of the Schedule are executed concurrently.
    case parallel = "PARALLEL"

    /// The Actions of the Schedule are executed in a pipelined mode. Output created by an Action is passed as input to the subsequent Action.
    case pipelined = "PIPELINED"
}
