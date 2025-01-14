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

package at.alladin.nettest.shared.server.opendata.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import at.alladin.nettest.shared.berec.collector.api.v1.dto.measurement.full.FullQoSMeasurement;

/**
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
public class QoSMeasurementPreparedStatementSetter extends SubMeasurementPreparedStatementSetter {

	private final FullQoSMeasurement qosMeasurement;
	
	public QoSMeasurementPreparedStatementSetter(FullQoSMeasurement qosMeasurement, String openDataUuid) {
		super(qosMeasurement, openDataUuid);
		
		this.qosMeasurement = qosMeasurement;
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		super.setValues(ps);
		
		// TODO: qos_measurement_results and qos_objectives
	}
}
