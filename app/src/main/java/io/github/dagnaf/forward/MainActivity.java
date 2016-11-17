package io.github.dagnaf.forward;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);
            onSharedPreferenceChanged(getPreferenceManager().getSharedPreferences(), "pref_email");
            Preference login = findPreference("pref_login");
            login.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    preference.setEnabled(false);
                    // TODO
                    // Sensitive information email and pw should be stored in Account Manager
                    //   or encrypted?
                    String email = ((EditTextPreference) findPreference("pref_email")).getText();
                    String pw = ((EditTextPreference) findPreference("pref_pw")).getText();
                    new AsyncTask<String, Void, Boolean>() {

                        @Override
                        protected Boolean doInBackground(String... params) {
                            return MailUtils.verify(params[0], params[1]);
                        }

                        @Override
                        protected void onPostExecute(Boolean b) {
                            Toast.makeText(getActivity(),
                                    b ? R.string.successful : R.string.failed,
                                    Toast.LENGTH_SHORT).show();
                            preference.setEnabled(true);
                        }
                    }.execute(email, pw);
                    return false;
                }
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (key.equals("pref_email")) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                pref.setSummary(editTextPref.getText());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }

    private static final int MY_PERMISSION_REQUEST = 1;

    // TODO
    // More flexible permission management
    public static void requestPermission(Activity thisActivity) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                        Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(thisActivity,
                        Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length <= 1
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED
                        || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermission(this);
    }
}
