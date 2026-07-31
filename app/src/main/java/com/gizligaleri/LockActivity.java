package com.chavogaleri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class LockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) 
            == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometric();
        } else {
            Toast.makeText(this, "Parmak izi sensörü bulunamadı!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    startActivity(new Intent(LockActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onAuthenticationFailed() {
                    Toast.makeText(LockActivity.this, "Parmak izi tanınamadı!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    Toast.makeText(LockActivity.this, "Hata: " + errString, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Chavo.exe")
            .setSubtitle("Parmak izinle giriş yap")
            .setNegativeButtonText("İptal")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
