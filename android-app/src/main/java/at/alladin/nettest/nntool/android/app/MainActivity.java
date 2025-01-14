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

package at.alladin.nettest.nntool.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import at.alladin.nettest.nntool.android.app.async.RegisterMeasurementAgentTask;
import at.alladin.nettest.nntool.android.app.util.PermissionUtil;
import at.alladin.nettest.nntool.android.app.util.PreferencesUtil;
import at.alladin.nettest.nntool.android.app.util.info.InformationService;
import at.alladin.nettest.nntool.android.app.workflow.WorkflowParameter;
import at.alladin.nettest.nntool.android.app.workflow.WorkflowTarget;
import at.alladin.nettest.nntool.android.app.workflow.history.HistoryFragment;
import at.alladin.nettest.nntool.android.app.workflow.main.TitleFragment;
import at.alladin.nettest.nntool.android.app.workflow.map.MapFragment;
import at.alladin.nettest.nntool.android.app.workflow.measurement.MeasurementService;
import at.alladin.nettest.nntool.android.app.workflow.measurement.MeasurementType;
import at.alladin.nettest.nntool.android.app.workflow.measurement.QosFragment;
import at.alladin.nettest.nntool.android.app.workflow.measurement.SpeedFragment;
import at.alladin.nettest.nntool.android.app.workflow.measurement.TitleWithRecentResultFragment;
import at.alladin.nettest.nntool.android.app.workflow.measurement.WorkflowMeasurementParameter;
import at.alladin.nettest.nntool.android.app.workflow.result.ResultFragment;
import at.alladin.nettest.nntool.android.app.workflow.settings.SettingsFragment;
import at.alladin.nettest.nntool.android.app.workflow.statistics.StatisticsFragment;
import at.alladin.nettest.nntool.android.app.workflow.tc.TermsAndConditionsFragment;

import static at.alladin.nettest.nntool.android.app.workflow.WorkflowTarget.HISTORY;

/**
 * @author Lukasz Budryk (alladin-IT GmbH)
 */
public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    BottomNavigationView navigation;

    private String selectedMeasurementPeerIdentifier = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.d(TAG, "onNavigationItemSelected");
            if (navigation.getSelectedItemId() == item.getItemId()) {
                return true;
            }

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    navigateTo(WorkflowTarget.TITLE);
                    return true;
                case R.id.navigation_history:
                    navigateTo(HISTORY);
                    return true;
                case R.id.navigation_map:
                    navigateTo(WorkflowTarget.MAP);
                    return true;
                case R.id.navigation_settings:
                    navigateTo(WorkflowTarget.SETTINGS);
                    return true;
                case R.id.navigation_statistics:
                    navigateTo(WorkflowTarget.STATISTICS);
                    return true;
            }
            return false;
        }
    };

    public void navigateTo(final WorkflowTarget target, final WorkflowParameter workflowParameter) {
        Log.d(TAG, "navigateToTarget");
        Fragment targetFragment = null;
        boolean isBottomNavigationVisible = true;

        switch (target) {
            case TITLE:
                targetFragment = TitleFragment.newInstance(workflowParameter);
                navigation.getMenu().findItem(R.id.navigation_home).setChecked(true);
                break;
            case MEASUREMENT_SPEED:
                isBottomNavigationVisible = false;
                targetFragment = SpeedFragment.newInstance(workflowParameter);
                break;
            case MEASUREMENT_QOS:
                isBottomNavigationVisible = false;
                targetFragment = QosFragment.newInstance(workflowParameter);
                break;
            case MEASUREMENT_RECENT_RESULT:
                targetFragment = TitleWithRecentResultFragment.newInstance(workflowParameter);
                break;
            case SETTINGS:
                targetFragment = SettingsFragment.newInstance();
                navigation.getMenu().findItem(R.id.navigation_settings).setChecked(true);
                break;
            case MAP:
                targetFragment = MapFragment.newInstance();
                break;
            case HISTORY:
                targetFragment = HistoryFragment.newInstance();
                navigation.getMenu().findItem(R.id.navigation_history).setChecked(true);
                break;
            case STATISTICS:
                targetFragment = StatisticsFragment.newInstance();
                break;
            case RESULT:
                if (workflowParameter != null) {
                    targetFragment = ResultFragment.newInstance(workflowParameter);
                } else {
                    //TODO: is fallback to history the right choice?
                    targetFragment = HistoryFragment.newInstance();
                }
                navigation.getMenu().findItem(R.id.navigation_history).setChecked(true);
                break;
        }

        setBottomNavigationVisible(isBottomNavigationVisible);

        if (targetFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_layout, targetFragment)
                .commit();
        }
    }

    public void updateActionBar(final String title) {
        updateActionBar(title, false);
    }

    public void updateActionBar(final String title, final boolean displayHome) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && title != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(displayHome);
        }
    }

    public void navigateTo(final WorkflowTarget target) {
        navigateTo(target, null);
    }

    public void startMeasurement(final MeasurementType measurementType, final Bundle options) {
        final WorkflowMeasurementParameter parameter = new WorkflowMeasurementParameter();
        if (options != null) {
            parameter.setQoSEnabled(options.getBoolean(MeasurementService.EXTRAS_KEY_QOS_EXECUTE, true));
            parameter.setSpeedEnabled(options.getBoolean(MeasurementService.EXTRAS_KEY_SPEED_EXECUTE, true));
        }
        switch (measurementType) {
            case SPEED:
                navigateTo(WorkflowTarget.MEASUREMENT_SPEED, parameter);
                final Intent speedIntent = new Intent(MeasurementService.ACTION_START_SPEED_MEASUREMENT,
                        null, this, MeasurementService.class);
                speedIntent.putExtras(options);
                startService(speedIntent);
                break;
            case QOS:
                navigateTo(WorkflowTarget.MEASUREMENT_QOS, parameter);
                final Intent intent = new Intent(MeasurementService.ACTION_START_QOS_MEASUREMENT,
                        null, this, MeasurementService.class);
                intent.putExtras(options);
                startService(intent);
                break;
        }
    }

    public void startInformationService() {
        final Intent intent = new Intent(InformationService.ACTION_START_INFORMATION_SERVICE,
                null, this, InformationService.class);
        startService(intent);
    }

    public void stopInformationService() {
        final Intent intent = new Intent(this, InformationService.class);
        stopService(intent);
    }

    public void setBottomNavigationVisible(final boolean isVisible) {
        final boolean wasVisible = navigation.getVisibility() == View.VISIBLE;
        if (navigation != null && isVisible != wasVisible) {
            navigation.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            navigation.setEnabled(isVisible);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigateTo(WorkflowTarget.TITLE);

        if (!PreferencesUtil.isTermsAndConditionsAccepted(this, TermsAndConditionsFragment.TERMS_AND_CONDITIONS_VERSION)) {
            TermsAndConditionsFragment f = TermsAndConditionsFragment.newInstance();
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            final Fragment oldTermsFragment = getSupportFragmentManager().findFragmentByTag(TermsAndConditionsFragment.TERMS_FRAGMENT_TAG);
            if (oldTermsFragment != null) {
                ft.remove(oldTermsFragment);
            }
            f.show(ft, TermsAndConditionsFragment.TERMS_FRAGMENT_TAG);
        }
        else {
            registerMeasurementAgent();
            PermissionUtil.requestLocationPermission(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        stopInformationService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startInformationService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PermissionUtil.REQUEST_CODE_LOCATION:
                Log.d(TAG, "Granted FINE LOCATION permission!");
                break;
            default:
                break;
        }
    }

    public void registerMeasurementAgent() {
        RegisterMeasurementAgentTask task = new RegisterMeasurementAgentTask(this, r -> {
            if (r == null && getResources().getBoolean(R.bool.debug_functionality_reset_uuid_if_not_in_database_and_retry)) {
                Log.d(TAG, "Measurement agent registration request failed. Deleting agent uuid and retrying.");
                PreferencesUtil.setAgentUuid(MainActivity.this, null);
                final RegisterMeasurementAgentTask retryTask = new RegisterMeasurementAgentTask(MainActivity.this, null);
                retryTask.execute();
            }
        });
        task.execute();
    }

    public String getSelectedMeasurementPeerIdentifier() {
        return selectedMeasurementPeerIdentifier;
    }

    public void setSelectedMeasurementPeerIdentifier(String selectedMeasurementPeerIdentifier) {
        this.selectedMeasurementPeerIdentifier = selectedMeasurementPeerIdentifier;
    }
}
