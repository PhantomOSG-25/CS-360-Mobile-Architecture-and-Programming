package com.phantomosg.momentum.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.phantomosg.momentum.R;
import com.phantomosg.momentum.data.AuthRepository;
import com.phantomosg.momentum.databinding.ActivityRegisterBinding;
import com.phantomosg.momentum.util.AppExecutors;
import com.phantomosg.momentum.util.InputValidator;
import com.phantomosg.momentum.util.SessionStore;

public final class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthRepository authRepository;
    private SessionStore sessionStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authRepository = new AuthRepository(this);
        sessionStore = new SessionStore(this);

        binding.registerButton.setOnClickListener(view -> register());
        binding.signInInsteadButton.setOnClickListener(view -> finish());
    }

    private void register() {
        clearErrors();
        String username = text(binding.usernameInput.getText());
        String password = text(binding.passwordInput.getText());
        String confirmation = text(binding.confirmPasswordInput.getText());
        String goalText = text(binding.goalInput.getText());

        if (!InputValidator.isValidUsername(username)) {
            binding.usernameLayout.setError(getString(R.string.error_username_format));
            return;
        }
        if (!InputValidator.isValidPassword(password)) {
            binding.passwordLayout.setError(getString(R.string.error_password_format));
            return;
        }
        if (!password.equals(confirmation)) {
            binding.confirmPasswordLayout.setError(getString(R.string.error_password_match));
            return;
        }

        double goal;
        try {
            goal = Double.parseDouble(goalText);
        } catch (NumberFormatException invalidNumber) {
            binding.goalLayout.setError(getString(R.string.error_weight_range));
            return;
        }
        if (!InputValidator.isValidWeight(goal)) {
            binding.goalLayout.setError(getString(R.string.error_weight_range));
            return;
        }

        binding.registerButton.setEnabled(false);
        AppExecutors.database().execute(() -> {
            long userId = authRepository.register(username, password, goal);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                binding.registerButton.setEnabled(true);
                if (userId < 0) {
                    binding.usernameLayout.setError(getString(R.string.error_username_taken));
                    return;
                }
                sessionStore.signIn(userId);
                Intent dashboard = new Intent(this, DashboardActivity.class);
                dashboard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(dashboard);
                finish();
            });
        });
    }

    private void clearErrors() {
        binding.usernameLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.confirmPasswordLayout.setError(null);
        binding.goalLayout.setError(null);
    }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}

