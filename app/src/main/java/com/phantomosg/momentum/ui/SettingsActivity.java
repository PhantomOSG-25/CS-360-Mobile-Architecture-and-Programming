package com.phantomosg.momentum.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.phantomosg.momentum.R;
import com.phantomosg.momentum.data.AuthRepository;
import com.phantomosg.momentum.databinding.ActivitySettingsBinding;
import com.phantomosg.momentum.model.UserProfile;
import com.phantomosg.momentum.util.AppExecutors;
import com.phantomosg.momentum.util.InputValidator;
import com.phantomosg.momentum.util.SessionStore;

import java.util.Locale;

public final class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private AuthRepository authRepository;
    private SessionStore sessionStore;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (!granted) {
                            binding.notificationsSwitch.setChecked(false);
                            Snackbar.make(
                                    binding.getRoot(),
                                    R.string.error_notification_permission,
                                    Snackbar.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authRepository = new AuthRepository(this);
        sessionStore = new SessionStore(this);
        if (!sessionStore.isSignedIn()) {
            returnToLogin();
            return;
        }

        binding.toolbar.setTitle(R.string.settings);
        binding.toolbar.setNavigationOnClickListener(view -> finish());
        binding.notificationsSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        });
        binding.saveSettingsButton.setOnClickListener(view -> saveSettings());
        binding.logoutButton.setOnClickListener(view -> {
            sessionStore.signOut();
            returnToLogin();
        });
        binding.deleteAccountButton.setOnClickListener(view -> confirmDeleteAccount());
        loadSettings();
    }

    private void loadSettings() {
        long userId = sessionStore.getUserId();
        AppExecutors.database().execute(() -> {
            UserProfile user = authRepository.getUser(userId);
            runOnUiThread(() -> {
                if (user == null || isFinishing()) return;
                binding.goalInput.setText(String.format(Locale.US, "%.1f", user.getGoalWeight()));
                binding.notificationsSwitch.setChecked(user.isNotificationsEnabled());
            });
        });
    }

    private void saveSettings() {
        binding.goalLayout.setError(null);
        double goal;
        try {
            goal = Double.parseDouble(text(binding.goalInput.getText()));
        } catch (NumberFormatException invalidNumber) {
            binding.goalLayout.setError(getString(R.string.error_weight_range));
            return;
        }
        if (!InputValidator.isValidWeight(goal)) {
            binding.goalLayout.setError(getString(R.string.error_weight_range));
            return;
        }

        boolean notifications = binding.notificationsSwitch.isChecked();
        if (notifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }

        binding.saveSettingsButton.setEnabled(false);
        long userId = sessionStore.getUserId();
        AppExecutors.database().execute(() -> {
            boolean updated = authRepository.updateSettings(userId, goal, notifications);
            runOnUiThread(() -> {
                binding.saveSettingsButton.setEnabled(true);
                Snackbar.make(
                        binding.getRoot(),
                        updated ? R.string.saved : R.string.error_generic,
                        Snackbar.LENGTH_LONG
                ).show();
            });
        });
    }

    private void confirmDeleteAccount() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_account_title)
                .setMessage(R.string.delete_account_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteAccount())
                .show();
    }

    private void deleteAccount() {
        long userId = sessionStore.getUserId();
        AppExecutors.database().execute(() -> {
            boolean deleted = authRepository.deleteAccount(userId);
            runOnUiThread(() -> {
                if (!deleted) {
                    Snackbar.make(binding.getRoot(), R.string.error_generic, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                sessionStore.signOut();
                returnToLogin();
            });
        });
    }

    private void returnToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
