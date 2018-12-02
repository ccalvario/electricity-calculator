package prj.ccalvario.electricitycalculator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import android.view.MenuItem;import trikita.log.Log;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_rate_type)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_fixed_rate)));

            // feedback preference click listener
            /*Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //sendFeedback(getActivity());
                    //startActivity(getActivity(), AboutActivity.class);
                    return true;
                }
            });*/

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String ratePlan = settings.getString(getResources().getString(R.string.key_rate_type), "");
            if(ratePlan.equals("1")){
                findPreference(getResources().getString(R.string.key_fixed_rate)).setEnabled(false);
                findPreference(getResources().getString(R.string.key_set_rates)).setEnabled(true);
            } else {
                findPreference(getResources().getString(R.string.key_fixed_rate)).setEnabled(true);
                findPreference(getResources().getString(R.string.key_set_rates)).setEnabled(false);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {

            if (key.equals(getResources().getString(R.string.key_rate_type))) {
                String value = sharedPreferences.getString(key, null);
                SaveData.getInstance().eventLogSetRate();
                if(value.equals("1")){
                    getPreferenceScreen().findPreference(getResources().getString(R.string.key_fixed_rate)).setEnabled(false);
                    getPreferenceScreen().findPreference(getResources().getString(R.string.key_set_rates)).setEnabled(true);
                } else {
                    getPreferenceScreen().findPreference(getResources().getString(R.string.key_fixed_rate)).setEnabled(true);
                    getPreferenceScreen().findPreference(getResources().getString(R.string.key_set_rates)).setEnabled(false);
                }

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                if (preference.getKey().equals(preference.getContext().getResources().getString(R.string.key_fixed_rate))) {
                    preference.setSummary("$ " + stringValue);
                }
                else {
                    preference.setSummary(stringValue);
                }
                //Log.d("preference "+ preference.getKey() + " stringValue " + stringValue);
            }
            return true;
        }
    };
}
