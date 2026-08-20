package com.phantomosg.momentum.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.phantomosg.momentum.R;
import com.phantomosg.momentum.data.AuthRepository;
import com.phantomosg.momentum.databinding.ActivityLoginBinding;
import com.phantomosg.momentum.util.AppExecutors;
import com.phantomosg.momentum.util.NotificationHelper;
import com.phantomosg.momentum.util.SessionStore;

public final class MainActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthRepository authRepository;
    private SessionStore sessionStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authRepository = new AuthRepository(this);
        sessionStore = new SessionStore(this);
        NotificationHelper.createChannel(this);

        if (sessionStore.isSignedIn()) {
            openDashboard();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.signInButton.setOnClickListener(view -> signIn());
        binding.createAccountButton.setOnClickListener(
                view -> startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void signIn() {
        binding.usernameLayout.setError(null);
        binding.passwordLayout.setError(null);

        String username = text(binding.usernameInput.getText());
        String password = text(binding.passwordInput.getText());
        if (username.isEmpty()) {
            binding.usernameLayout.setError(getString(R.string.error_required));
            return;
        }
        if (password.isEmpty()) {
            binding.passwordLayout.setError(getString(R.string.error_required));
            return;
        }

        binding.signInButton.setEnabled(false);
        AppExecutors.database().execute(() -> {
            long userId = authRepository.authenticate(username, password);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                binding.signInButton.setEnabled(true);
                if (userId < 0) {
                    Snackbar.make(
                            binding.getRoot(),
                            R.string.error_invalid_credentials,
                            Snackbar.LENGTH_LONG
                    ).show();
                    return;
                }
                sessionStore.signIn(userId);
                openDashboard();
            });
        });
    }

    private void openDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
