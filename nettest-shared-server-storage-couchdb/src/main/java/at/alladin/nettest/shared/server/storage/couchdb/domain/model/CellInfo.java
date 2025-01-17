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

package at.alladin.nettest.shared.server.storage.couchdb.domain.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Cell location information from a point in time.
 *
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@JsonClassDescription("Cell identity information from a point in time.")
public class CellInfo {

	/**
	 * Contains the cell-ID, if available.
	 */
	@JsonPropertyDescription("Contains the cell-ID, if available.")
	@Expose
	@SerializedName("cell_id")
	@JsonProperty("cell_id")
	private Integer cellId;

	/**
	 * Contains the area code (e.g. location area code (GSM), tracking area code (LTE)), if available.
	 */
	@JsonPropertyDescription("Contains the area code (e.g. location area code (GSM), tracking area code (LTE)), if available.")
	@Expose
	@SerializedName("area_code")
	@JsonProperty("area_code")
	private Integer areaCode;

	/**
	 * Time and date the cell location information was captured (UTC).
	 */
	@JsonPropertyDescription("Time and date the cell location information was captured (UTC).")
	@Expose
	@SerializedName("time")
	@JsonProperty("time")
	private LocalDateTime time;

	/**
	 * Contains the primary scrambling code, if available.
	 */
	@JsonPropertyDescription("Contains the primary scrambling code, if available.")
	@Expose
	@SerializedName("primary_scrambling_code")
	@JsonProperty("primary_scrambling_code")
	private Integer primaryScramblingCode;

	/**
	 * Contains the ARFCN (Absolute Radio Frequency Channel Number) (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.
	 */
	@JsonPropertyDescription("Contains the ARFCN (Absolute Radio Frequency Channel Number) (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.")
	@Expose
	@SerializedName("arfcn")
	@JsonProperty("arfcn")
	private Integer arfcn;

	/**
	 * Relative time in nanoseconds (to test begin).
	 */
	@JsonPropertyDescription("Relative time in nanoseconds (to test begin).")
	@Expose
	@SerializedName("relative_time_ns")
	@JsonProperty("relative_time_ns")
	private Long relativeTimeNs;

	/**
	 * Contains the ARFCN (Absolute Radio Frequency Channel Number) (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.
	 */
	@io.swagger.annotations.ApiModelProperty("Contains the frequency (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.")
	@JsonPropertyDescription("Contains the frequency (e.g. 16-bit GSM ARFCN or 18-bit LTE EARFCN), if available.")
	@Expose
	@SerializedName("frequency")
	@JsonProperty("frequency")
	private Integer frequency;

	/**
     * Geographic location latitude of this cell.
     */
	@JsonPropertyDescription("Geographic location latitude of this cell.")
	@Expose
    @SerializedName("latitude")
    @JsonProperty("latitude")
    private Double latitude;

    /**
     * Geographic location longitude of this cell.
     */
	@JsonPropertyDescription("Geographic location longitude of this cell.")
    @Expose
    @SerializedName("longitude")
    @JsonProperty("longitude")
    private Double longitude;

	public Integer getCellId() {
		return cellId;
	}

	public void setCellId(Integer cellId) {
		this.cellId = cellId;
	}

	public Integer getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(Integer areaCode) {
		this.areaCode = areaCode;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public Integer getPrimaryScramblingCode() {
		return primaryScramblingCode;
	}

	public void setPrimaryScramblingCode(Integer primaryScramblingCode) {
		this.primaryScramblingCode = primaryScramblingCode;
	}

	public Integer getArfcn() {
		return arfcn;
	}

	public void setArfcn(Integer arfcn) {
		this.arfcn = arfcn;
	}

	public Long getRelativeTimeNs() {
		return relativeTimeNs;
	}

	public void setRelativeTimeNs(Long relativeTimeNs) {
		this.relativeTimeNs = relativeTimeNs;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
}
