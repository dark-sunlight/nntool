/*!
    \file Ias.js
    \author zafaco GmbH <info@zafaco.de>
    \date Last update: 2019-12-04

    Copyright (C) 2016 - 2019 zafaco GmbH

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3 
    as published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* global jsinterface, require, global */

var wsControl;
var jsTool = new JSTool();




/**
 * @Class Ias
 * @description IAS Measurement class
 */
function Ias()
{
    this.wsMeasurement;

    var iasVersion                      = '1.0.1';
    var platform;
    var cookieId                        = true;

    //Test Cases
    var performRttMeasurement           = false;
    var performDownloadMeasurement      = false;
    var performUploadMeasurement        = false;

    var performedRttMeasurement         = false;
    var performedDownloadMeasurement    = false;
    var performedUploadMeasurement      = false;

    var performRouteToClientLookup      = false;
    var performedRouteToClientLookup    = false;
    var routeToClientTargetPort         = '8080';
    var routeToClientTargetPortTls      = '8443';
    var routeToClientUseHttps           = true;

    var wsMeasurementParameters         = {};

    var classChangesLimit               = 2;
    var classIndexCurrent               = -1;
    var classIndexUsed                  = [];
    var classIndexLowestBound           = -1;
    var classIndexHighestBound          = -1;
    var classMatched                    = false;
    var classChangePerforming           = false;

    //KPIs
    var globalKPIs                      = {};
    var rttKPIs                         = {};
    var downloadKPIs                    = [];
    var downloadStreamKPIs              = [];
    var uploadKPIs                      = [];
    var uploadStreamKPIs                = [];
    var timestampKPIs                   = {};
    var clientKPIs                      = {};
    var deviceKPIs                      = {};
    var routeKPIs                       = {};
    var peerKPIs                        = {};

    var wsRttTimer                      = 0;
    var wsDownloadTimer                 = 0;
    var wsUploadTimer                   = 0;

    var measurementStopped              = false;

    var gcTimer;
    var gcTimerInterval                 = 10;

    var waitTime                        = 3000;
    var waitTimeShort                   = 1000;
    var waitTimeClassChangeDownload     = 8000;
    var waitTimeClassChangeUpload       = 15000;

    var useWebWorkers                   = true;

    var rttIdTimestampMapping           = {};




    /*-------------------------public functions------------------------*/

    /**
     * @function measurementStart
     * @description API Function to start a measurement
     * @public
     * @param {string} measurementParameters JSON coded measurement Parameters
     */
    this.measurementStart = function(measurementParameters)
    {
        wsMeasurementParameters = JSON.parse(measurementParameters);

        console.log('Measurement Parameters: \n' + JSON.stringify(wsMeasurementParameters));

        console.warn("The browser developer console should only be used for debugging purposes, as an active developer console can cause performance issues");

        wsControlReset();

        if (typeof wsMeasurementParameters.platform !== 'undefined')
        {
            platform = String(wsMeasurementParameters.platform);
        }

        if (typeof wsMeasurementParameters.useWebWorkers === 'undefined')
        {
            wsMeasurementParameters.useWebWorkers = useWebWorkers;
        }

        globalKPIs.start_time           = jsTool.getFormattedDate();

        clientKPIs.timezone             = jsTool.getTimezone();
        clientKPIs.type                 = platform.toUpperCase();
        clientKPIs.ias_version          = iasVersion;

        deviceKPIs                      = JSON.parse(jsTool.getDeviceKPIs(platform));

        if (wsMeasurementParameters.platform === 'mobile')
        {
            //WebSocket streams are natively threaded, so we dont need WebWorkers
            wsMeasurementParameters.useWebWorkers = false;
        }
        else
        {
            //catch Firefox < 38, as it has no WebSocket in WebWorker Support
            if ((deviceKPIs.browser_info.name.search('Firefox') !== -1) && Number(deviceKPIs.browser_info.version) < 38)
            {
                wsMeasurementParameters.useWebWorkers = false;
            }

            //catch ie11 and Edge > 13
            //ie11: no WebSocket in WebWorker Support
            //edge14: no WebSocket in WebWorker Support
            //edge>14: WebSocket in WebWorker Support, but poor performance
            if ((deviceKPIs.browser_info.name.search('Internet Explorer 11') !== -1) || ((deviceKPIs.browser_info.name.search('Edge') !== -1) && (Number(deviceKPIs.browser_info.version) > 13)))
            {
                wsMeasurementParameters.useWebWorkers = false;
            }

            //catch Safari 5, 6 and 7, as they only have partial WebSocket support
            if ((deviceKPIs.browser_info.name.search('Safari 5') !== -1) || (deviceKPIs.browser_info.name.search('Safari 6') !== -1) || (deviceKPIs.browser_info.name.search('Safari 7') !== -1))
            {
                var data                  = {};
                data.cmd                  = 'error';
                data.error_code           = 5;
                data.error_description    = 'WebSockets are only partially supported by your browser';
                data                            = JSON.stringify(data);
                this.controlCallback(data);
                return;
            }
        }

        if (!timestampKPIs.measurement_start)
        {
            timestampKPIs.measurement_start = jsTool.getTimestamp() * 1000 * 1000;
        }

        if (typeof window !== 'undefined' && !window.WebSocket)
        {
            var data                  = {};
            data.cmd                  = 'error';
            data.error_code           = 3;
            data.error_description    = 'WebSockets are not supported by your browser';
            data                      = JSON.stringify(data);
            this.controlCallback(data);
            return;
        }

        startGcTimer();

        if (wsMeasurementParameters.useWebWorkers === false)
        {
            console.log('WebWorkers:        inactive');
            deviceKPIs.web_workers_active = false;
        }
        else
        {
            console.log('WebWorkers:        active');
            deviceKPIs.web_workers_active = true;
        }

        var cookieName = wsMeasurementParameters.wsTLD.split('.', 1);

        if (typeof wsMeasurementParameters.cookieId !== 'undefined')
        {
            cookieId = Boolean(wsMeasurementParameters.cookieId);
        }

        if (typeof require === 'undefined' && cookieId)
        {
            var cookie = jsTool.getCookie(cookieName + '_id');
            if (!cookie)
            {
                cookie = jsTool.generateRandomData(64, true, false);
            }

            jsTool.setCookie(cookieName + '_id', cookie, 365);
            clientKPIs.cookie = cookie;
        }

        if (typeof wsMeasurementParameters.rtt.performMeasurement !== 'undefined')
        {
            performRttMeasurement       = Boolean(wsMeasurementParameters.rtt.performMeasurement);
        }
        if (typeof wsMeasurementParameters.download.performMeasurement !== 'undefined')
        {
            performDownloadMeasurement  = Boolean(wsMeasurementParameters.download.performMeasurement);
        }
        if (typeof wsMeasurementParameters.upload.performMeasurement !== 'undefined')
        {
            performUploadMeasurement    = Boolean(wsMeasurementParameters.upload.performMeasurement);
        }
        if (typeof wsMeasurementParameters.performRouteToClientLookup !== 'undefined')
        {
            performRouteToClientLookup  = Boolean(wsMeasurementParameters.performRouteToClientLookup);
        }
        if (typeof wsMeasurementParameters.routeToClientTargetPort !== 'undefined')
        {
            routeToClientTargetPort     = Number(wsMeasurementParameters.routeToClientTargetPort);
        }
        if (typeof wsMeasurementParameters.routeToClientTargetPortTls !== 'undefined')
        {
            routeToClientTargetPortTls  = Number(wsMeasurementParameters.routeToClientTargetPortTls);
        }
        if (typeof wsMeasurementParameters.routeToClientUseHttps !== 'undefined')
        {
            routeToClientUseHttps       = Boolean(wsMeasurementParameters.routeToClientUseHttps);
        }

        if (wsMeasurementParameters.wsTargets.length > 0)
        {
            wsMeasurementParameters.wsTarget = wsMeasurementParameters.wsTargets[Math.floor(Math.random() * wsMeasurementParameters.wsTargets.length)];
            if (wsMeasurementParameters.wsTLD)
            {
                wsMeasurementParameters.wsTarget += '.' + wsMeasurementParameters.wsTLD;
            }
        }

        peerKPIs.url  = wsMeasurementParameters.wsTarget;
        peerKPIs.port = String(wsMeasurementParameters.wsTargetPort);
        peerKPIs.tls  = String(wsMeasurementParameters.wsWss);

        if (!platform || (!performRttMeasurement && !performDownloadMeasurement && !performUploadMeasurement))
        {
            var data                = {};
            data.cmd                = 'error';
            data.error_code         = 1;
            data.error_description  = 'Measurement Parameters Missing';
            data                    = JSON.stringify(data);
            this.controlCallback(data);
            return;
        }

        delete wsMeasurement;
        wsMeasurement = null;
        wsMeasurement = this;

        measurementCampaign();
    }

    /**
     * @function measurementStop
     * @public
     * @description API Function to stop a measurement
     */
    this.measurementStop = function()
    {
        measurementStopped = true;

        clearTimeout(gcTimer);
        clearTimeout(wsRttTimer);
        clearTimeout(wsDownloadTimer);
        clearTimeout(wsUploadTimer);

        if (wsControl) wsControl.measurementStop(JSON.stringify(wsMeasurementParameters));
    }

    /**
     * @function controlCallback
     * @description API Function to Callback the JSON coded measurement Results to the implementing client
     * @param {string} data JSON coded measurement Results
     */
    this.controlCallback = function(data)
    {
        if (classChangePerforming)
        {
            return;
        }    

        data = JSON.parse(data);

        globalKPIs.cmd                  = data.cmd;
        globalKPIs.msg                  = data.msg;
        globalKPIs.test_case            = data.test_case;
        globalKPIs.error_code           = data.error_code;
        globalKPIs.error_description    = data.error_description;

        if (data.test_case === 'routeToClient')
        {
            routeKPIs.server_client       = data.server_client_route;
            routeKPIs.server_client_hops  = data.server_client_route_hops;
        }

        if (data.cmd === 'classCheck' && (typeof wsMeasurementParameters[data.test_case].classes === 'undefined' || wsMeasurementParameters[data.test_case].classes.length === 0))
        {
            return;
        }

        if (classIndexCurrent !== -1 && ((data.test_case === 'download' && typeof data.downloadKPIs !== 'undefined' && typeof data.downloadKPIs.throughput_avg_bps !== 'undefined') || (data.test_case === 'upload' && typeof data.uploadKPIs !== 'undefined' && typeof data.uploadKPIs.throughput_avg_bps !== 'undefined')))
        {
            data.throughput_avg_bps = (data.test_case === 'download') ? data.downloadKPIs.throughput_avg_bps : data.uploadKPIs.throughput_avg_bps;
        }
        
        if ((data.test_case === 'download' || data.test_case === 'upload') && typeof data.throughput_avg_bps !== 'undefined' && classIndexCurrent !== -1)
        {
            //check, if current class matches the class bounds
            if (wsMeasurementParameters[data.test_case].classes[classIndexCurrent].bounds.lower * 1000 * 1000 > data.throughput_avg_bps || wsMeasurementParameters[data.test_case].classes[classIndexCurrent].bounds.upper * 1000 * 1000 < data.throughput_avg_bps)
            {
                data.out_of_bounds = true;
                if (data.cmd === 'classCheck')
                {
                    console.log('ClassCheck: Current class is out of bounds');
                    classMatched = false;
                }
            }
            else
            {
                data.out_of_bounds = false;
                if (data.cmd === 'classCheck')
                {
                    console.log('ClassCheck: Current class is in bounds');
                    classMatched = true;
                }
            }

            if (typeof data.downloadKPIs !== 'undefined')
            {
                data.downloadKPIs.out_of_bounds = data.out_of_bounds;
            }
            else
            {
                data.uploadKPIs.out_of_bounds = data.out_of_bounds;
            }
            

            classChangePerforming = false;
            if (data.out_of_bounds  && !classMatched && data.cmd === 'classCheck')
            {
                if (classIndexUsed.length <= classChangesLimit)
                {
                    var newClassSelected = false;

                    wsMeasurementParameters[data.test_case].classes.every(function(element, index)
                    {
                        var i = index;
                        //check for new class within bounds OR if lowest lower or highest upper bounds where exceeded
                        if ((element.bounds.lower * 1000 * 1000 <= data.throughput_avg_bps && element.bounds.upper * 1000 * 1000 > data.throughput_avg_bps)
                            || wsMeasurementParameters[data.test_case].classes[classIndexLowestBound].bounds.lower * 1000 * 1000 > data.throughput_avg_bps
                            || wsMeasurementParameters[data.test_case].classes[classIndexHighestBound].bounds.upper * 1000 * 1000 < data.throughput_avg_bps)
                        {
                            if (wsMeasurementParameters[data.test_case].classes[classIndexLowestBound].bounds.lower * 1000 * 1000 > data.throughput_avg_bps
                                || wsMeasurementParameters[data.test_case].classes[classIndexHighestBound].bounds.upper * 1000 * 1000 < data.throughput_avg_bps)
                            {
                                if (wsMeasurementParameters[data.test_case].classes[classIndexLowestBound].bounds.lower * 1000 * 1000 > data.throughput_avg_bps)
                                {
                                    i = classIndexLowestBound;
                                    console.log("ClassCheck: Lowest bound of configured classes was exceeded");
                                }
                                if (wsMeasurementParameters[data.test_case].classes[classIndexHighestBound].bounds.upper * 1000 * 1000 < data.throughput_avg_bps)
                                {
                                    i = classIndexHighestBound;
                                    console.log("ClassCheck: ighest bound of configured classes was exceeded");
                                }

                                if (i === classIndexCurrent || classIndexUsed.includes(i))
                                {
                                    console.log('ClassCheck: Class #' + i + ' was already used');
                                }
                                else
                                {
                                    newClassSelected = true;
                                }
                            }
                            else
                            {
                                if (index === classIndexCurrent || classIndexUsed.includes(index))
                                {
                                    console.log('ClassCheck: Class #' + index + ' was already used');
                                }
                                else
                                {
                                    i = index;
                                    newClassSelected = true;
                                }
                            }
                        }

                        if (newClassSelected)
                        {
                            classIndexCurrent = i;
                            classChangePerforming = true;
                            return false;
                        }

                        return true;
                    });

                    if (!newClassSelected)
                    {
                        console.log('ClassCheck: Class can not be changed, resuming measurement');
                        classMatched = true;
                    }
                }
                else
                {
                    console.log('ClassCheck: Class can not be changed: class change limit reached');
                    classMatched = true;
                }
            }

            if (classChangePerforming && data.cmd === 'classCheck')
            {
                console.log('ClassCheck: Changing Class');
                wsControl.measurementStop(JSON.stringify(wsMeasurementParameters));
                setTimeout(wsControlReset, 200);
                setTimeout(startGcTimer, 500);
                clearTimeout(wsDownloadTimer);
                clearTimeout(wsUploadTimer);

                if (data.test_case === 'download')
                {
                    wsDownloadTimer = setTimeout(startDownload, waitTimeClassChangeDownload);
                }

                if (data.test_case === 'upload')
                {
                    wsDownloadTimer = setTimeout(startUpload, waitTimeClassChangeUpload);
                }
                
                globalKPIs.cmd = 'report';
                globalKPIs.msg = 'changing class';
            }
        }

        var cleanedData = jsTool.extend(data);
        delete cleanedData.cmd;
        delete cleanedData.msg;
        delete cleanedData.test_case;
        delete cleanedData.throughput_avg_bps;

        var relative_time_ns_measurement_start = (jsTool.getTimestamp() * 1000 * 1000 ) - timestampKPIs.measurement_start;

        if (data.test_case === 'rtt')
        {
            if (typeof cleanedData !== 'undefined' && typeof cleanedData.rtts !== 'undefined' && typeof cleanedData.rtts[cleanedData.rtts.length - 1].id !== 'undefined')
            {
                var id = cleanedData.rtts[cleanedData.rtts.length - 1].id;
                if (typeof rttIdTimestampMapping[id] === 'undefined')
                {
                    rttIdTimestampMapping[id] = relative_time_ns_measurement_start;
                }
            }

            if (typeof cleanedData.rtts !== 'undefined')
            {
                cleanedData.rtts.forEach(function(element, index)
                {
                    element.relative_time_ns_measurement_start = rttIdTimestampMapping[element.id];

                    if (typeof element.relative_time_ns_measurement_start === 'undefined')
                    {
                        rttIdTimestampMapping[element.id] = relative_time_ns_measurement_start - 5e8;
                        element.relative_time_ns_measurement_start = rttIdTimestampMapping[element.id];
                    }

                    delete(element.id);
                });
            }

            rttKPIs = cleanedData;
        }

        if (data.test_case === 'download' && typeof data.downloadKPIs !== 'undefined' && typeof data.downloadKPIs.throughput_avg_bps !== 'undefined')
        {
            cleanedData.downloadKPIs.relative_time_ns_measurement_start = relative_time_ns_measurement_start;

            downloadKPIs.push(cleanedData.downloadKPIs);

            if (typeof cleanedData.downloadStreamKPIs !== 'undefined')
            {
                for (index in cleanedData.downloadStreamKPIs)
                {
                    cleanedData.downloadStreamKPIs[index].relative_time_ns_measurement_start = relative_time_ns_measurement_start;
                    downloadStreamKPIs.push(cleanedData.downloadStreamKPIs[index]);
                }
            }
        }

        if (data.test_case === 'upload' && typeof data.uploadKPIs !== 'undefined' && typeof data.uploadKPIs.throughput_avg_bps !== 'undefined')
        {
            cleanedData.uploadKPIs.relative_time_ns_measurement_start = relative_time_ns_measurement_start;

            uploadKPIs.push(cleanedData.uploadKPIs);

            if (typeof cleanedData.uploadStreamKPIs !== 'undefined')
            {
                for (index in cleanedData.uploadStreamKPIs)
                {
                    cleanedData.uploadStreamKPIs[index].relative_time_ns_measurement_start = relative_time_ns_measurement_start;
                    uploadStreamKPIs.push(cleanedData.uploadStreamKPIs[index]);
                }
            }
        }

        if (data.cmd === 'error')
        {
            setEndTimestamps(data.test_case);
            setTimeout(wsControlReset, 200);
        }

        if (data.cmd === 'finish')
        {
            if (data.test_case === 'rtt')
            {
                performedRttMeasurement         = true;
            }

            if (data.test_case === 'download')
            {
                performedDownloadMeasurement    = true;
            }

            if (data.test_case === 'upload')
            {
                performedUploadMeasurement      = true;
            }

            measurementCampaign();
        }

        var kpis = getKPIs();

        if (data.cmd !== 'error' && performRttMeasurement === performedRttMeasurement && performDownloadMeasurement === performedDownloadMeasurement && performUploadMeasurement === performedUploadMeasurement)
        {
            var kpisCompleted = jsTool.extend(kpis);
            kpisCompleted.cmd = 'completed';
            kpisCompleted = JSON.stringify(kpisCompleted);
            setTimeout(controlCallbackToPlatform, 50, kpisCompleted);
        }

        controlCallbackToPlatform(JSON.stringify(kpis));
    };

    /**
     * @function setDeviceKPIs
     * @description API Function to set additional Device KPIs
     * @public
     * @param {string} data JSON coded Device KPIs
     */
    this.setDeviceKPIs = function(data)
    {
        data = JSON.parse(data);
        deviceKPIs = jsTool.extend(deviceKPIs, data);
    };




    /*-------------------------private functions------------------------*/

    /**
     * @function measurementCampaign
     * @description Perform a measurement Campaign containing one or more Test Cases
     * @private
     */
    function measurementCampaign()
    {
        if (measurementStopped)
        {
            return;
        }

        if (performRttMeasurement && !performedRttMeasurement)
        {
            setEndTimestamps();

            if (!timestampKPIs.rtt_start) timestampKPIs.rtt_start = (jsTool.getTimestamp() + waitTimeShort) * 1000 * 1000;
            wsMeasurementParameters.testCase = 'rtt';
            wsRttTimer = setTimeout(startRtt, waitTimeShort);

            return;
        }

        if (performRouteToClientLookup && !performedRouteToClientLookup && (performDownloadMeasurement || performUploadMeasurement))
        {
            var port = routeToClientUseHttps ? routeToClientTargetPortTls : routeToClientTargetPort;
            jsTool.performRouteToClientLookup(wsMeasurementParameters.wsTargets[Math.floor(Math.random() * wsMeasurementParameters.wsTargets.length)] + '.' + wsMeasurementParameters.wsTLD, port, routeToClientUseHttps);
            performedRouteToClientLookup = true;
        }

        if (performDownloadMeasurement && !performedDownloadMeasurement)
        {
            setEndTimestamps();

            classIndexCurrent       = -1;
            classIndexUsed          = []
            classMatched            = false;
            classChangePerforming   = false;

            wsDownloadTimer = setTimeout(startDownload, waitTimeShort);

            return;
        }
        else
        if (performUploadMeasurement && !performedUploadMeasurement)
        {
            setEndTimestamps();

            classIndexCurrent       = -1;
            classIndexUsed          = []
            classMatched            = false;
            classChangePerforming   = false;

            wsUploadTimer = setTimeout(startUpload, waitTime);

            return;
        }
        else setTimeout(wsControlReset, 200);

        setEndTimestamps();
    }

    /**
     * @function setEndTimestamps
     * @description Set the Timestamp of the completion of the current Test Case and the complete measurement
     * @private
     * @param {string} test_case Test Case
     */
    function setEndTimestamps(test_case)
    {
        if ((performRttMeasurement && performedRttMeasurement && !timestampKPIs.rtt_end) || test_case === 'rtt')
        {
            timestampKPIs.rtt_end = jsTool.getTimestamp() * 1000 * 1000;
        }
        if ((performDownloadMeasurement && performedDownloadMeasurement && !timestampKPIs.download_end)  || test_case === 'download')
        {
            timestampKPIs.download_end = jsTool.getTimestamp() * 1000 * 1000;
        }
        if ((performUploadMeasurement && performedUploadMeasurement && !timestampKPIs.upload_end)  || test_case === 'upload')
        {
            timestampKPIs.upload_end = jsTool.getTimestamp() * 1000 * 1000;
        }

        if (performRttMeasurement === performedRttMeasurement && performDownloadMeasurement === performedDownloadMeasurement && performUploadMeasurement === performedUploadMeasurement)
        {
            timestampKPIs.measurement_end = jsTool.getTimestamp() * 1000 * 1000;
        }
    }

    function startRtt()
    {
        wsControlInstantiate();

        wsControl.measurementStart(JSON.stringify(wsMeasurementParameters));
    }

    function startDownload()
    {
        handleClasses('download');

        if (!timestampKPIs.download_start)
        {
            timestampKPIs.download_start = jsTool.getTimestamp() * 1000 * 1000;
        }
        wsMeasurementParameters.testCase = 'download';
        if (typeof require !== 'undefined')
        {
            if (wsMeasurementParameters.wsTargets[0].indexOf('ipv6') !== -1)
            {
                wsMeasurementParameters.ndServerFamily = 6;
            }
            else if (wsMeasurementParameters.wsTargets[0].indexOf('ipv4') !== -1)
            {
                wsMeasurementParameters.ndServerFamily = 4;
            }
        }
        wsControl.measurementStart(JSON.stringify(wsMeasurementParameters));
    }

    function startUpload()
    {
        handleClasses('upload');

        if (!timestampKPIs.upload_start)
        {
            timestampKPIs.upload_start = jsTool.getTimestamp() * 1000 * 1000;
        }
        wsMeasurementParameters.testCase = 'upload';
        wsControl.measurementStart(JSON.stringify(wsMeasurementParameters));
    }

    function handleClasses(measurementType)
    {
        wsControlInstantiate();

        classChangePerforming = false;
        if (typeof wsMeasurementParameters[measurementType].classes !== 'undefined' && wsMeasurementParameters[measurementType].classes.length > 0)
        {
            if (classIndexCurrent !== -1)
            {
                console.log("Class selected: " + JSON.stringify(wsMeasurementParameters[measurementType].classes[classIndexCurrent]));
            }
            else
            {
                if (classIndexCurrent === -1)
                {
                    //get default class
                    wsMeasurementParameters[measurementType].classes.forEach(function(element, index)
                    {
                        if (element.default === true)
                        {
                            classIndexCurrent = index;
                            console.log("Default Class selected: " + JSON.stringify(element));
                        }

                        //get classes with highest upper and lowest lower bound
                        if (element.bounds.lower < classIndexLowestBound || classIndexLowestBound === -1)
                        {
                            classIndexLowestBound = index;
                            console.log("New lowest bound discovered on index #" + classIndexLowestBound + ": " + element.bounds.lower);
                        }
                        if (element.bounds.upper > classIndexHighestBound || classIndexHighestBound === -1)
                        {
                            classIndexHighestBound = index;
                            console.log("New highest bound discovered on index #" + classIndexHighestBound + ": " + element.bounds.upper);
                        }
                    });

                }

                if (classIndexCurrent === -1)
                {
                    //select first class if no default was set
                    classIndexCurrent = 0;
                    console.log("No Default Class set, using first class: " + JSON.stringify(wsMeasurementParameters[measurementType].classes[classIndexCurrent]));
                }
            }

            classIndexUsed.push(classIndexCurrent);

            wsMeasurementParameters[measurementType].streams          = wsMeasurementParameters[measurementType].classes[classIndexCurrent].streams;
            wsMeasurementParameters[measurementType].frameSize        = wsMeasurementParameters[measurementType].classes[classIndexCurrent].frameSize;
            wsMeasurementParameters[measurementType].framesPerCall    = wsMeasurementParameters[measurementType].classes[classIndexCurrent].framesPerCall;
        }
        else
        {
            console.log('No Classes set, using parameters');
        }
    }

    /**
     * @function getKPIs
     * @description Return all measurement Results
     */
    function getKPIs()
    {
        var kpis = {};
        kpis = jsTool.extend(globalKPIs);
        if (!jsTool.isEmpty(rttKPIs))            kpis.rtt_info          = rttKPIs;
        if (!jsTool.isEmpty(downloadKPIs))       kpis.download_info     = downloadKPIs;
        if (!jsTool.isEmpty(downloadStreamKPIs)) kpis.download_raw_data = downloadStreamKPIs;
        if (!jsTool.isEmpty(uploadKPIs))         kpis.upload_info       = uploadKPIs;
        if (!jsTool.isEmpty(uploadStreamKPIs))   kpis.upload_raw_data   = uploadStreamKPIs;
        if (!jsTool.isEmpty(timestampKPIs))      kpis.time_info         = timestampKPIs;
        if (!jsTool.isEmpty(clientKPIs))         kpis.client_info       = clientKPIs;
        if (!jsTool.isEmpty(deviceKPIs))         kpis.device_info       = deviceKPIs;
        if (!jsTool.isEmpty(routeKPIs))          kpis.route_info        = routeKPIs;
        if (!jsTool.isEmpty(peerKPIs))           kpis.peer_info         = peerKPIs;

        return kpis;
    }

    function startGcTimer()
    {
        if (typeof require !== 'undefined' && typeof platformModule !== 'undefined' && platformModule.isIOS)
        {
            gcTimer = setInterval(function ()
            {
                utils.GC();
            }, gcTimerInterval);
        }
    }

    function wsControlInstantiate()
    {
        delete wsControl;
        wsControl = null;
        wsControl = new WSControl();
        wsControl.wsMeasurement = this;
        wsControl.callback      = 'wsMeasurement';
    }

    /**
     * @function wsControlReset
     * @description Reset the wsControl object
     * @private
     */
    function wsControlReset()
    {
        clearInterval(gcTimer);
        wsControl = null;
        delete wsControl;
    }

    /**
     * @function controlCallbackToPlatform
     * @description API Function to Callback the JSON coded measurement Results to the implementing client
     * @param {string} kpis JSON coded measurement Results
     */
    function controlCallbackToPlatform(kpis)
    {
        switch (wsMeasurementParameters.platform)
        {
            case 'web':
            {
                reportToWeb(kpis);
                break;
            }

            case 'desktop':
            {
                reportToWeb(kpis);
                break;
            }

            case 'mobile':
            {
                reportToMobile(kpis);
                break;
            }
        }
    }

    /**
     * @function reportToWeb
     * @description Callback to Web
     * @param {string} kpis JSON coded Measurement Results
     */
    function reportToWeb(kpis)
    {
        iasCallback(kpis);
    }

    /**
     * @function reportToMobile
     * @description Callback to Mobile
     * @param {string} kpis JSON coded Measurement Results
     */
    function reportToMobile(kpis)
    {
        global.messageToNative('tnsReportCallback', kpis);
    }
}
