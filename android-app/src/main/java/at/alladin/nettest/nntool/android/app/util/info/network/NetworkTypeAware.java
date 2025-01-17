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

package at.alladin.nettest.nntool.android.app.util.info.network;

/**
 * @author Lukasz Budryk (lb@alladin.at)
 */
public interface NetworkTypeAware {

    /** Returned by getNetwork() if Wifi */
    public static final int NETWORK_WLAN = 99;

    /** Returned by getNetwork() if Ethernet */
    public static final int NETWORK_ETHERNET = 106;

    /** Returned by getNetwork() if Bluetooth */
    public static final int NETWORK_BLUETOOTH = 107;

    int getNetwork();
}
