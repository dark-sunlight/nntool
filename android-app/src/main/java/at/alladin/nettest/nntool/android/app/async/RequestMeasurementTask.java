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

package at.alladin.nettest.nntool.android.app.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.alladin.nettest.nntool.android.app.R;
import at.alladin.nettest.nntool.android.app.dialog.BlockingProgressDialog;
import at.alladin.nettest.nntool.android.app.util.ConnectionUtil;
import at.alladin.nettest.nntool.android.app.util.LmapUtil;
import at.alladin.nettest.nntool.android.app.util.ObjectMapperUtil;
import at.alladin.nettest.nntool.android.app.util.RequestUtil;
import at.alladin.nettest.nntool.android.app.util.connection.ControllerConnection;
import at.alladin.nettest.shared.berec.collector.api.v1.dto.lmap.control.LmapControlDto;

/**
 * @author Felix Kendlbacher (fk@alladin.at)
 */
public class RequestMeasurementTask extends AsyncTask<Void, Void, LmapControlDto> {

    private final static String TAG = RequestMeasurementTask.class.getSimpleName();

    private final Context context;

    private final OnTaskFinishedCallback<LmapUtil.LmapTaskWrapper> callback;

    private BlockingProgressDialog progressDialog;

    private String selectedMeasurementPeerIdentifier;

    public RequestMeasurementTask (final String selectedMeasurementPeerIdentifier, final Context context, final OnTaskFinishedCallback<LmapUtil.LmapTaskWrapper> callback) {
        this.context = context;
        this.callback = callback;
        this.selectedMeasurementPeerIdentifier = selectedMeasurementPeerIdentifier;
    }

    public RequestMeasurementTask (final Context context, final OnTaskFinishedCallback<LmapUtil.LmapTaskWrapper> callback) {
        this(null, context, callback);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new BlockingProgressDialog.Builder(context)
                .setCancelable(false)
                .setMessage(R.string.task_measurement_initiation_dialog_info)
                .build();
        progressDialog.show();
    }

    @Override
    protected LmapControlDto doInBackground(Void... voids) {
        final ControllerConnection controllerConnection = ConnectionUtil.createControllerConnection(context, false);
        return controllerConnection.requestMeasurement(
                RequestUtil.prepareMeasurementInitiationRequest(selectedMeasurementPeerIdentifier, context));
    }

    @Override
    protected void onPostExecute(LmapControlDto result) {
        final LmapUtil.LmapTaskWrapper taskWrapper = LmapUtil.extractQosTaskDescList(result);
        try {
            Log.d(TAG, ObjectMapperUtil.createBasicObjectMapper().writeValueAsString(taskWrapper));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (callback != null) {
            callback.onTaskFinished(taskWrapper);
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
