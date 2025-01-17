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

package at.alladin.nettest.nntool.android.app.util.info;

import android.util.Log;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import at.alladin.nettest.nntool.android.app.support.telephony.CellInfoWrapper;
import at.alladin.nettest.nntool.android.app.support.telephony.CellType;
import at.alladin.nettest.nntool.android.app.util.info.gps.GeoLocationChangeEvent;
import at.alladin.nettest.nntool.android.app.util.info.gps.GeoLocationChangeListener;
import at.alladin.nettest.nntool.android.app.util.info.gps.GeoLocationGatherer;
import at.alladin.nettest.nntool.android.app.util.info.interfaces.CurrentInterfaceTraffic;
import at.alladin.nettest.nntool.android.app.util.info.interfaces.TrafficGatherer;
import at.alladin.nettest.nntool.android.app.util.info.network.MobileOperator;
import at.alladin.nettest.nntool.android.app.util.info.network.NetworkChangeEvent;
import at.alladin.nettest.nntool.android.app.util.info.network.NetworkChangeListener;
import at.alladin.nettest.nntool.android.app.util.info.network.NetworkGatherer;
import at.alladin.nettest.nntool.android.app.util.info.network.OperatorInfo;
import at.alladin.nettest.nntool.android.app.util.info.signal.SignalGatherer;
import at.alladin.nettest.nntool.android.app.util.info.signal.SignalStrengthChangeEvent;
import at.alladin.nettest.nntool.android.app.util.info.signal.SignalStrengthChangeListener;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.shared.GeoLocationDto;

/**
 * @author Lukasz Budryk (lb@alladin.at)
 */
public class InformationCollector
        implements SignalStrengthChangeListener, NetworkChangeListener, GeoLocationChangeListener {

    private final static String TAG = InformationCollector.class.getSimpleName();

    public final static String IF_TRAFFIC_LIST_END_TAG = "_END";

    public final static String IF_TRAFFIC_LIST_TOTAL_TAG = "_TOTAL";

    private final InformationProvider informationProvider;

    private final AtomicBoolean isCollecting = new AtomicBoolean(false);

    private final List<CellInfoWrapper> cellInfoList = new ArrayList<>();

    private final List<GeoLocationDto> geoLocationList = new ArrayList<>();

    private final List<NetworkChangeEvent> networkChangeEventList = new ArrayList<>();

    private final AtomicReference<OperatorInfoHolder> operatorInfo = new AtomicReference<>(null);

    private final AtomicBoolean illegalNetworkStateDetected = new AtomicBoolean(false);

    private final Map<String, CurrentInterfaceTraffic> interfaceTrafficMap = new HashMap<>();

    //timestamp in ns used for all relative time values
    private final AtomicLong timestampNs = new AtomicLong(System.nanoTime());

    private String clientIpPublic;

    private String clientIpPrivate;

    public InformationCollector(final InformationProvider informationProvider) {
        this.informationProvider = informationProvider;
    }

    public InformationProvider getInformationProvider() {
        return informationProvider;
    }

    public void start() {
        if (!this.isCollecting.getAndSet(true)) {
            Log.d(TAG, "Starting InformationCollector...");
            final SignalGatherer signalGatherer = this.informationProvider.getGatherer(SignalGatherer.class);
            final GeoLocationGatherer geoLocationGatherer = this.informationProvider.getGatherer(GeoLocationGatherer.class);
            final NetworkGatherer networkGatherer = this.informationProvider.getGatherer(NetworkGatherer.class);
            final TrafficGatherer trafficGatherer = this.informationProvider.getGatherer(TrafficGatherer.class);

            if (signalGatherer != null) {
                Log.d(TAG, "Found SignalGatherer. Registering listener.");
                signalGatherer.addListener(this);
            }
            if (geoLocationGatherer != null) {
                Log.d(TAG, "Found GeoLocationGatherer. Registering listener.");
                geoLocationGatherer.addListener(this);
            }
            if (networkGatherer != null) {
                Log.d(TAG, "Found NetworkGatherer. Registering listener.");
                networkGatherer.addListener(this);
            }
            if (trafficGatherer != null) {
                Log.d(TAG, "Found TrafficGatherer. Starting.");
            }

            this.informationProvider.start();
        }
    }

    public void stop() {
        Log.d(TAG, "Stopping InformationCollector...");
        this.isCollecting.set(false);
        this.informationProvider.stop();

        final SignalGatherer signalGatherer = this.informationProvider.getGatherer(SignalGatherer.class);
        final GeoLocationGatherer geoLocationGatherer = this.informationProvider.getGatherer(GeoLocationGatherer.class);
        final NetworkGatherer networkGatherer = this.informationProvider.getGatherer(NetworkGatherer.class);
        final TrafficGatherer trafficGatherer = this.informationProvider.getGatherer(TrafficGatherer.class);

        if (signalGatherer != null) {
            signalGatherer.removeListener(this);
        }
        if (geoLocationGatherer != null) {
            geoLocationGatherer.removeListener(this);
        }
        if (networkGatherer != null) {
            networkGatherer.removeListener(this);
        }

        interfaceTrafficMap.put(IF_TRAFFIC_LIST_END_TAG, trafficGatherer.getCurrentValue());

        long rxTotal = 0;
        long txTotal = 0;
        long durationTotal = 0;
        for (final CurrentInterfaceTraffic ift : interfaceTrafficMap.values()) {
            rxTotal += ift.getRxBytes();
            txTotal += ift.getTxBytes();
            durationTotal += ift.getDurationNs();
        }

        interfaceTrafficMap.put(IF_TRAFFIC_LIST_TOTAL_TAG, new CurrentInterfaceTraffic(rxTotal, txTotal, durationTotal));

        interfaceTrafficMap.put(IF_TRAFFIC_LIST_END_TAG, getAggregatedIfValues());

        Log.d(TAG, "CellInfoList: " + cellInfoList);
        Log.d(TAG, "GeoLocationList: " + geoLocationList);
        Log.d(TAG, "InterfaceTrafficMap: " + interfaceTrafficMap);
    }

    public void takeTrafficSnapshot(final String tag) {
        final TrafficGatherer trafficGatherer = this.informationProvider.getGatherer(TrafficGatherer.class);
        if (trafficGatherer != null) {
            trafficGatherer.run();
            interfaceTrafficMap.put(tag, trafficGatherer.getCurrentValue());
        }
    }

    public Map<String, CurrentInterfaceTraffic> getInterfaceTrafficMap() {
        return interfaceTrafficMap;
    }

    public List<CellInfoWrapper> getCellInfoList() {
        return cellInfoList;
    }

    public List<GeoLocationDto> getGeoLocationList() {
        return geoLocationList;
    }

    @Override
    public void onLocationChanged(GeoLocationChangeEvent geoLocationChangeEvent) {
        if (this.isCollecting.get()
                && geoLocationChangeEvent != null
                && geoLocationChangeEvent.getGeoLocationDto() != null) {
            if (GeoLocationChangeEvent.GeoLocationChangeEventType.LOCATION_UPDATE.equals(geoLocationChangeEvent.getEventType())) {
                final GeoLocationDto locationDto = geoLocationChangeEvent.getGeoLocationDto();
                locationDto.setRelativeTimeNs(System.nanoTime() - timestampNs.get());
                geoLocationList.add(locationDto);

            }
        }
    }

    @Override
    public void onNetworkChange(NetworkChangeEvent event) {
        if (this.isCollecting.get() && event != null) {
            if (NetworkChangeEvent.NetworkChangeEventType.NO_CONNECTION.equals(event.getEventType())) {
                illegalNetworkStateDetected.set(true);
            }
            else if (event.getNetworkType() == null) {
                illegalNetworkStateDetected.set(true);
            }
            else {
                final CellType cellType = CellType.fromTelephonyNetworkTypeId(event.getNetworkType());
                boolean isMobileOperator = true;
                switch (cellType) {
                    case MOBILE_WCDMA:
                    case MOBILE_GSM:
                    case MOBILE_LTE:
                    case MOBILE_ANY:
                    case MOBILE_CDMA:
                        isMobileOperator = true;
                        break;
                    case WLAN:
                        isMobileOperator = false;
                        break;
                    case UNKNOWN:
                        illegalNetworkStateDetected.set(true);
                        break;
                }

                final OperatorInfo operator = isMobileOperator ? event.getMobileOperator() : event.getWifiOperator();
                if (operator == null) {
                    illegalNetworkStateDetected.set(true);
                }
                else if (operatorInfo.get() != null) {
                    final OperatorInfoHolder lastOperatorInfo = operatorInfo.get();
                    boolean wasMobileOperator = (lastOperatorInfo.getOperatorInfo() instanceof MobileOperator);
                    //check if previous and current operator are the same connection type
                    if (wasMobileOperator != isMobileOperator) {
                        illegalNetworkStateDetected.set(true);
                    }
                }

                //finally set operator info if no illegal network state occurred
                if (operator != null && !illegalNetworkStateDetected.get()) {
                    final OperatorInfoHolder operatorInfoHolder = new OperatorInfoHolder();
                    operatorInfoHolder.setCellType(cellType);
                    operatorInfoHolder.setOperatorInfo(operator);
                    operatorInfoHolder.setTime(event.getTime());
                    operatorInfoHolder.setTimestampNs(event.getTimestampNs());
                    operatorInfoHolder.setNetworkId(event.getNetworkType());
                    operatorInfo.set(operatorInfoHolder);
                }
            }

            networkChangeEventList.add(event);
        }
    }

    @Override
    public void onSignalStrengthChange(SignalStrengthChangeEvent event) {
        if (this.isCollecting.get()
                && event != null
                && event.getCurrentSignalStrength() != null
                && event.getCurrentSignalStrength().getCellInfoWrapper() != null) {
            cellInfoList.add(event.getCurrentSignalStrength().getCellInfoWrapper());
        }
    }

    public List<NetworkChangeEvent> getNetworkChangeEventList() {
        return networkChangeEventList;
    }

    public OperatorInfoHolder getOperatorInfo() {
        return operatorInfo.get();
    }

    public boolean getIllegalNetworkStateDetected() {
        return illegalNetworkStateDetected.get();
    }

    public void setStartTimeNs(final long startTimeNs) {
        timestampNs.set(startTimeNs);
    }

    public long getStartTimeNs() {
        return timestampNs.get();
    }

    public static class OperatorInfoHolder {
        OperatorInfo operatorInfo;
        CellType cellType;
        Integer networkId;
        LocalDateTime time;
        Long timestampNs;

        public OperatorInfo getOperatorInfo() {
            return operatorInfo;
        }

        public void setOperatorInfo(OperatorInfo operatorInfo) {
            this.operatorInfo = operatorInfo;
        }

        public CellType getCellType() {
            return cellType;
        }

        public void setCellType(CellType cellType) {
            this.cellType = cellType;
        }

        public Integer getNetworkId() {
            return networkId;
        }

        public void setNetworkId(Integer networkId) {
            this.networkId = networkId;
        }

        public Long getTimestampNs() {
            return timestampNs;
        }

        public void setTimestampNs(Long timestampNs) {
            this.timestampNs = timestampNs;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }
    }

    public String getClientIpPublic() {
        return clientIpPublic;
    }

    public void setClientIpPublic(String clientIpPublic) {
        this.clientIpPublic = clientIpPublic;
    }

    public String getClientIpPrivate() {
        return clientIpPrivate;
    }

    public void setClientIpPrivate(String clientIpPrivate) {
        this.clientIpPrivate = clientIpPrivate;
    }

    public CurrentInterfaceTraffic getAggregatedIfValues() {
        long rxTotal = 0;
        long txTotal = 0;
        long durationTotal = 0;
        for (final CurrentInterfaceTraffic ift : interfaceTrafficMap.values()) {
            rxTotal += ift.getRxBytes();
            txTotal += ift.getTxBytes();
            durationTotal += ift.getDurationNs();
        }

        return new CurrentInterfaceTraffic(rxTotal, txTotal, durationTotal);
    }
}
