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

/// This DTO contains all relevant network information of a single point in time.
class MeasurementResultNetworkPointInTimeDto: Codable {

    /// Time and date the signal information was captured (UTC).
    var time: Date?

    /// Relative time in nanoseconds (to test begin).
    var relativeTimeNs: UInt64?

    /// Network type id as it gets returned by the Android API.
    var networkTypeId: Int?

    /// SSID of the network.
    var ssid: String?

    /// BSSID of the network.
    var bssid: String?

    /// The network operator country code (e.g. "AT"), if available.
    var networkCountry: String?

    /// The MCC/MNC of the network operator, if available.
    var networkOperatorMccMnc: String?

    /// The network operator name, if available.
    var networkOperatorName: String?

    /// The SIM operator country code (e.g. "AT"), if available.
    var simCountry: String?

    /// The MCC/MNC of the SIM operator, if available.
    var simOperatorMccMnc: String?

    /// SIM operator name, if available.
    var simOperatorName: String?

    ///
    var agentPublicIp: String?

    ///
    var agentPrivateIp: String?

    ///
    enum CodingKeys: String, CodingKey {
        case time
        case relativeTimeNs        = "relative_time_ns"
        case networkTypeId         = "network_type_id"
        case ssid
        case bssid
        case networkCountry        = "network_country"
        case networkOperatorMccMnc = "network_operator_mcc_mnc"
        case networkOperatorName   = "network_operator_name"
        case simCountry            = "sim_country"
        case simOperatorMccMnc     = "sim_operator_mcc_mnc"
        case simOperatorName       = "sim_operator_name"
        case agentPublicIp         = "client_public_ip"
        case agentPrivateIp        = "client_private_ip"
    }
}
