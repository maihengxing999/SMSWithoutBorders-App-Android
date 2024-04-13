package com.example.sw0b_001;

import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MessageLockBiometricsActivity extends AppCompactActivityCustomized {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_lock_biometrics);
    }

    private void navigateAway() throws GeneralSecurityException, IOException {
        startActivity(new Intent(this, HomepageActivity.class));
        finish();
    }

    public void enabledDecryptionClicked(View view) throws GeneralSecurityException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("lock_screen_for_encryption", true).apply();

        navigateAway();
    }

    public void notNOwDecryptionClicked(View view) throws GeneralSecurityException, IOException {
        navigateAway();
    }
}