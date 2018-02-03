package com.liteyoutube.youtubelite.settings;

import android.os.Bundle;

import com.liteyoutube.youtubelite.R;

public class VideoAudioSettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.video_audio_settings);
    }
}
