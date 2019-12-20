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

public class MeasurementAgentBuilder {

    let measurementAgent: MeasurementAgent

    public init(controllerServiceBaseUrl baseUrl: String) {
        measurementAgent = MeasurementAgent(configuration: MeasurementAgentConfiguration(controllerServiceBaseUrl: baseUrl))
    }

    /*public func controlServiceBaseUrl(baseUrl: String) -> Self {
        return self
    }*/

    ///
    public func program(task: MeasurementTypeDto, config: ProgramConfiguration) -> Self {
        measurementAgent.registerProgramForTask(task, withConfiguration: config)
        return self
    }

    public func build() -> MeasurementAgent {
        return measurementAgent
    }
}