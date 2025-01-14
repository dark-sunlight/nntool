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

package at.alladin.nettest.shared.server.storage.couchdb.mapper.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import at.alladin.nettest.shared.berec.collector.api.v1.dto.measurement.MeasurementTypeDto;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.measurement.brief.BriefMeasurementResponse;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.measurement.brief.BriefMeasurementResponse.BriefDeviceInfo;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.measurement.brief.BriefQoSMeasurement;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.measurement.brief.BriefSpeedMeasurement;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.measurement.brief.BriefSubMeasurement;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.shared.GeoLocationDto;
import at.alladin.nettest.shared.server.storage.couchdb.domain.model.DeviceInfo;
import at.alladin.nettest.shared.server.storage.couchdb.domain.model.EmbeddedNetworkType;
import at.alladin.nettest.shared.server.storage.couchdb.domain.model.GeoLocation;
import at.alladin.nettest.shared.server.storage.couchdb.domain.model.Measurement;
import at.alladin.nettest.shared.server.storage.couchdb.domain.model.QoSMeasurement;
import at.alladin.nettest.shared.server.storage.couchdb.domain.model.SpeedMeasurement;
import at.alladin.nettest.shared.server.storage.couchdb.domain.model.SubMeasurement;

@Mapper(componentModel = "spring", uses = { DateTimeMapper.class })
public interface BriefMeasurementResponseMapper {
	
	@Mappings({
		@Mapping(target="firstAccurateGeoLocation", expression="java(parseFirstGeoLocation(measurement))"),
		@Mapping(target="startTime", source="measurementTime.startTime"),
		@Mapping(target="durationNs", source="measurementTime.durationNs"),
		@Mapping(target="type", source="agentInfo.type"),
		@Mapping(target="measurements", expression="java(parseMeasurementMap(measurement))"),
		@Mapping(target="networkTypeName", expression="java(parseNetworkName(measurement))")
	})
	BriefMeasurementResponse map(Measurement measurement);
	
	@Mappings({
		@Mapping(target="averageCpuUsage", source="osInfo.cpuUsage.average"),
		@Mapping(target="averageMemUsage", source="osInfo.memUsage.average"),
		@Mapping(target="deviceCodeName", source="codeName"),
		@Mapping(target="deviceFullName", source="fullName"),
		@Mapping(target="osName", source="osInfo.name"),
		@Mapping(target="osVersion", source="osInfo.version"),
	})
	BriefDeviceInfo map(DeviceInfo deviceInfo);
	
	@Mappings({
		@Mapping(target="objectiveCount", expression="java(qosMeasurement.getResults().size())"),
		@Mapping(target="durationNs", source="measurementTime.durationNs"),
		@Mapping(target="startTime", source="measurementTime.startTime")
	})
	BriefQoSMeasurement map(QoSMeasurement qosMeasurement);
	
	@Mappings({
		
		@Mapping(target="durationNs", source="measurementTime.durationNs"),
		@Mapping(target="startTime", source="measurementTime.startTime"),
		@Mapping(target="rttAverageNs", source="rttInfo.averageNs"),
		@Mapping(target="rttMedianNs", source="rttInfo.medianNs")
	})
	BriefSpeedMeasurement map(SpeedMeasurement speedMeasurement);
	
	GeoLocationDto map(GeoLocation geoLocation);
	
	public default GeoLocationDto parseFirstGeoLocation(final Measurement measurement) {
		if (measurement != null && measurement.getGeoLocationInfo() != null) {
			final List<GeoLocation> geoLocationList = measurement.getGeoLocationInfo().getGeoLocations();
			if (geoLocationList != null && geoLocationList.size() > 0) {
				return map(geoLocationList.get(0));
			}
		}
		return null;
	}
	
	public default Map<MeasurementTypeDto, BriefSubMeasurement> parseMeasurementMap (final Measurement measurement) {
		if (measurement.getMeasurements() != null) {
			final Map<MeasurementTypeDto, BriefSubMeasurement> ret = new HashMap<>();
			for (Entry<MeasurementTypeDto, SubMeasurement> entry : measurement.getMeasurements().entrySet()) {
				switch (entry.getKey()) {
				case QOS:
					ret.put(MeasurementTypeDto.QOS, map((QoSMeasurement) entry.getValue()));
					break;
				case SPEED:
					ret.put(MeasurementTypeDto.SPEED, map((SpeedMeasurement) entry.getValue()));
					break;
				}
			}
			return ret;
		}
		return null;
	}
	
	public default String parseNetworkName(Measurement measurement) {
		if (measurement.getNetworkInfo() != null && measurement.getNetworkInfo().getNetworkPointsInTime() != null && measurement.getNetworkInfo().getNetworkPointsInTime().size() > 0) {
			final EmbeddedNetworkType networkType = measurement.getNetworkInfo().getNetworkPointsInTime().get(0).getNetworkType();
			if (networkType != null) {
				return networkType.getGroupName();
			}
		}
		return null;
	}
}
