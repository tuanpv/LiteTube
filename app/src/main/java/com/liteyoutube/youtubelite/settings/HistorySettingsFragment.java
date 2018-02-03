package com.liteyoutube.youtubelite.settings;

import android.os.Bundle;

import com.liteyoutube.youtubelite.R;

public class HistorySettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.history_settings);
    }
}
