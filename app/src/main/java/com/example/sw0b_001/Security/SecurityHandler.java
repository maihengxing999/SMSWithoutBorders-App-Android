package com.example.sw0b_001.Security;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.sw0b_001.BuildConfig;
import com.example.sw0b_001.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.spec.MGF1ParameterSpec;
import java.util.concurrent.Executor;

import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class SecurityHandler {
    KeyStore keyStore;
    Context context;
    SharedPreferences sharedPreferences;


    public static MGF1ParameterSpec defaultEncryptionDigest = MGF1ParameterSpec.SHA256;
    public static MGF1ParameterSpec defaultDecryptionDigest = MGF1ParameterSpec.SHA1;

    public static OAEPParameterSpec encryptionDigestParam = new OAEPParameterSpec("SHA-256", "MGF1", defaultEncryptionDigest, PSource.PSpecified.DEFAULT);
    public static OAEPParameterSpec decryptionDigestParam = new OAEPParameterSpec("SHA-256", "MGF1", defaultDecryptionDigest, PSource.PSpecified.DEFAULT);
    MasterKey masterKeyAlias;

    public static final String DEFAULT_KEYPAIR_ALGORITHM_PADDING = "RSA/ECB/" + KeyProperties.ENCRYPTION_PADDING_RSA_OAEP;

    public static final String DEFAULT_AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String DEFAULT_KEYSTORE_PROVIDER = "AndroidKeyStore";
    final String SHARED_SECRET_KEY = "SHARED_SECRET_KEY";
    final String MSISDN_HASH = "MSISDN_HASH";
    final String BIOMETRIC_CHECK_ALWAYS_ON = "BIOMETRIC_CHECK_ALWAYS_ON";
    final String BIOMETRIC_CHECK_DECRYPTION = "BIOMETRIC_CHECK_DECRYPTION";

    public SecurityHandler() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        this.keyStore = KeyStore.getInstance(DEFAULT_KEYSTORE_PROVIDER);
        this.keyStore.load(null);
    }

    public KeyStore getKeyStore() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        this.keyStore = KeyStore.getInstance(DEFAULT_KEYSTORE_PROVIDER);
        this.keyStore.load(null);

        return this.keyStore;
    }

    public SecurityHandler(Context context) throws GeneralSecurityException, IOException {
        this.keyStore = KeyStore.getInstance(DEFAULT_KEYSTORE_PROVIDER);
        this.keyStore.load(null);
        this.context = context;

        this.masterKeyAlias = new MasterKey.Builder(this.context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        this.sharedPreferences = EncryptedSharedPreferences.create(
                context,
                this.SHARED_SECRET_KEY,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );
    }

    public boolean hasSharedKey() throws KeyStoreException {
        try {
            Boolean keystoreHasSharedKey = sharedPreferences.contains(this.SHARED_SECRET_KEY);
            if (keystoreHasSharedKey) {
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String generateRandom(int length) {
//        char[] charsArray = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', '#', '$', '%', '^', '*'};
        char[] charsArray = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()-+}|[]:\"';,./><?\\|".toCharArray();
        SecureRandom rand = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(charsArray[rand.nextInt(charsArray.length)]);
        }
        return password.toString();
    }

    public String getEncryptedBase64SharedKey() {
        return this.sharedPreferences.getString(SHARED_SECRET_KEY, "");
    }

    public byte[] getSharedKey() {
        String encryptedSharedKey = this.sharedPreferences.getString(SHARED_SECRET_KEY, "");

        byte[] encryptedSharedKeyDecoded = Base64.decode(encryptedSharedKey, Base64.DEFAULT);

        return encryptedSharedKeyDecoded;
    }

    public String getMSISDN() throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                MSISDN_HASH,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        return encryptedSharedPreferences.getString(MSISDN_HASH, "");
    }

    public void removeSharedKey() throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                this.SHARED_SECRET_KEY,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        SharedPreferences.Editor sharedPreferencesEditor = encryptedSharedPreferences.edit();
        sharedPreferencesEditor.remove(SHARED_SECRET_KEY);
        sharedPreferencesEditor.apply();
    }

    public void storeMSISDN(String msisdnHash) throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                MSISDN_HASH,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        SharedPreferences.Editor sharedPreferencesEditor = encryptedSharedPreferences.edit();

        sharedPreferencesEditor.putString(MSISDN_HASH, msisdnHash);
        if(!sharedPreferencesEditor.commit()) {
            throw new RuntimeException("Failed to store MSISDN");
        }
    }

    public void storeSharedKey(String sharedKey) throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                this.SHARED_SECRET_KEY,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        SharedPreferences.Editor sharedPreferencesEditor = encryptedSharedPreferences.edit();

        sharedPreferencesEditor.putString(SHARED_SECRET_KEY, sharedKey);
        if(!sharedPreferencesEditor.commit()) {
            throw new RuntimeException("Failed to store shared key!");
        }
    }

    public static boolean phoneCredentialsPossible(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager biometricManager = (BiometricManager) context.getSystemService(Context.BIOMETRIC_SERVICE);
            int canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
            switch (canAuthenticate) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    return true;

                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    break;
//                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
//                    // Prompts the user to create credentials that your app accepts.
//                    final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
//                    enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                            BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
////                    startActivityForResult(enrollIntent, REQUEST_CODE);
//                    break;
//                case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
//                    // TODO:
//                    break;
                default:
                    break;
            }
        }
        else {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            return keyguardManager.isDeviceSecure();
        }
        return false;
    }

    public void authenticateWithLockScreen(Intent callbackIntent, AppCompatActivity parent) throws InterruptedException {
        Executor executor = ContextCompat.getMainExecutor(context);
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {

            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.hardware.biometrics.BiometricPrompt biometricPrompt = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ?
                new android.hardware.biometrics.BiometricPrompt.Builder(context)
                        .setTitle(context.getString(R.string.settings_biometric_login))
                        .setSubtitle(context.getString(R.string.settings_biometric_login_subtitle))
                        .setDescription(context.getString(R.string.settings_biometric_login_description))
                        .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                        .build() :
                new android.hardware.biometrics.BiometricPrompt.Builder(context)
                        .setTitle(context.getString(R.string.settings_biometric_login))
                        .setSubtitle(context.getString(R.string.settings_biometric_login_subtitle))
                        .setDescription(context.getString(R.string.settings_biometric_login_description))
                        .setDeviceCredentialAllowed(true)
                        .build();

            biometricPrompt.authenticate(cancellationSignal,
                    executor, new android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(context,
                                                "Authentication error: " + errorCode + ":" + errString, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }

                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull android.hardware.biometrics.BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            if (BuildConfig.DEBUG)
                                Toast.makeText(context,
                                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                            ActivityOptions options = ActivityOptions.makeCustomAnimation(context,
                                    android.R.anim.fade_in, android.R.anim.fade_out);
                            context.startActivity(callbackIntent, options.toBundle());

                            if(parent != null)
                                parent.finish();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            if (BuildConfig.DEBUG)
                                Toast.makeText(context, "Authentication failed",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static String PRIMARY_KEYSTORE_ALIAS = "PRIMARY_KEYSTORE_ALIAS";
    public static PublicKey getNewPublicKey(Context context) throws GeneralSecurityException, IOException {
        SecurityRSA securityRSA = new SecurityRSA(context);
        return securityRSA.generateKeyPair(PRIMARY_KEYSTORE_ALIAS)
                .generateKeyPair().getPublic();
    }

    public void authenticateWithLockScreen(Runnable successRunnable, Runnable failedRunnable) throws InterruptedException {
        Executor executor = ContextCompat.getMainExecutor(context);
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.hardware.biometrics.BiometricPrompt biometricPrompt = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ?
                    new android.hardware.biometrics.BiometricPrompt.Builder(context)
                            .setTitle(context.getString(R.string.settings_biometric_login))
                            .setSubtitle(context.getString(R.string.settings_biometric_login_subtitle))
                            .setDescription(context.getString(R.string.settings_biometric_login_description))
                            .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                            .build() :
                    new android.hardware.biometrics.BiometricPrompt.Builder(context)
                            .setTitle(context.getString(R.string.settings_biometric_login))
                            .setSubtitle(context.getString(R.string.settings_biometric_login_subtitle))
                            .setDescription(context.getString(R.string.settings_biometric_login_description))
                            .setDeviceCredentialAllowed(true)
                            .build();

            biometricPrompt.authenticate(cancellationSignal,
                    executor, new android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(context,
                                                "Authentication error: " + errorCode + ":" + errString, Toast.LENGTH_SHORT)
                                        .show();
                                failedRunnable.run();
                            }
                        }

                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull android.hardware.biometrics.BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            if (BuildConfig.DEBUG)
                                Toast.makeText(context,
                                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();

//                            ActivityOptions options = ActivityOptions.makeCustomAnimation(context,
//                                    android.R.anim.fade_in, android.R.anim.fade_out);
//                            context.startActivity(callbackIntent, options.toBundle());
                            successRunnable.run();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            if (BuildConfig.DEBUG)
                                Toast.makeText(context, "Authentication failed",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public boolean seenBiometricCheckAlwaysOn() throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                BIOMETRIC_CHECK_ALWAYS_ON,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        return encryptedSharedPreferences.contains(BIOMETRIC_CHECK_ALWAYS_ON);
    }

    public boolean seenBiometricCheckDecryption() throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                BIOMETRIC_CHECK_DECRYPTION,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        return encryptedSharedPreferences.contains(BIOMETRIC_CHECK_DECRYPTION);
    }

    public void setSeenBiometricScreenAlwaysOn(boolean seen) throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                BIOMETRIC_CHECK_ALWAYS_ON,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        SharedPreferences.Editor sharedPreferencesEditor = encryptedSharedPreferences.edit();

        sharedPreferencesEditor.putBoolean(BIOMETRIC_CHECK_ALWAYS_ON, seen);
        if(!sharedPreferencesEditor.commit()) {
            throw new RuntimeException("Failed to store MSISDN");
        }
    }

    public void setSeenBiometricScreenDecryption(boolean seen) throws GeneralSecurityException, IOException {
        SharedPreferences encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                BIOMETRIC_CHECK_DECRYPTION,
                this.masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM );

        SharedPreferences.Editor sharedPreferencesEditor = encryptedSharedPreferences.edit();

        sharedPreferencesEditor.putBoolean(BIOMETRIC_CHECK_DECRYPTION, seen);
        if(!sharedPreferencesEditor.commit()) {
            throw new RuntimeException("Failed to store MSISDN");
        }
    }

    public boolean requiresSyncing() throws GeneralSecurityException, IOException {
        return getMSISDN().isEmpty();
    }

    public static boolean checkHasLockScreenAlways(Context context) {
        // Get the SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the state of the SwitchPreferenceCompact
        return prefs.getBoolean("lock_screen_always_on", false);
    }

    public static boolean checkHasLockDecryption(Context context) {
        // Get the SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the state of the SwitchPreferenceCompact
        return prefs.getBoolean("lock_screen_for_encryption", false);
    }
}
