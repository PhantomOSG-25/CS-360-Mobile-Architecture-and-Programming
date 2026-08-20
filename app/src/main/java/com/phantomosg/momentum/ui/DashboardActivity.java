package com.phantomosg.momentum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.phantomosg.momentum.R;
import com.phantomosg.momentum.data.AuthRepository;
import com.phantomosg.momentum.data.WeightRepository;
import com.phantomosg.momentum.databinding.ActivityDashboardBinding;
import com.phantomosg.momentum.model.UserProfile;
import com.phantomosg.momentum.model.WeightRecord;
import com.phantomosg.momentum.util.AppExecutors;
import com.phantomosg.momentum.util.ProgressCalculator;
import com.phantomosg.momentum.util.SessionStore;

import java.util.List;

public final class DashboardActivity extends AppCompatActivity implements WeightAdapter.Listener {
    private ActivityDashboardBinding binding;
    private AuthRepository authRepository;
    private WeightRepository weightRepository;
    private SessionStore sessionStore;
    private WeightAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository(this);
        weightRepository = new WeightRepository(this);
        sessionStore = new SessionStore(this);
        if (!sessionStore.isSignedIn()) {
            returnToLogin();
            return;
        }

        binding.toolbar.setTitle(R.string.dashboard_title);
        adapter = new WeightAdapter(this);
        binding.recordsList.setLayoutManager(new LinearLayoutManager(this));
        binding.recordsList.setAdapter(adapter);
        binding.addEntryButton.setOnClickListener(view -> openWeightForm(null));
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionStore != null && sessionStore.isSignedIn()) {
            loadDashboard();
        }
    }

    private void loadDashboard() {
        long userId = sessionStore.getUserId();
        AppExecutors.database().execute(() -> {
            UserProfile user = authRepository.getUser(userId);
            List<WeightRecord> records = weightRepository.getAll(userId);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (user == null) {
                    sessionStore.signOut();
                    returnToLogin();
                    return;
                }
                render(user, records);
            });
        });
    }

    private void render(UserProfile user, List<WeightRecord> records) {
        binding.welcomeText.setText(getString(R.string.welcome_user, user.getUsername()));
        binding.goalValue.setText(getString(R.string.goal_stat, user.getGoalWeight()));
        adapter.submitList(records);
        binding.emptyState.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recordsList.setVisibility(records.isEmpty() ? View.GONE : View.VISIBLE);

        ProgressCalculator.ProgressSnapshot progress =
                ProgressCalculator.calculate(records, user.getGoalWeight());
        binding.progressIndicator.setProgressCompat(progress.getPercent(), true);
        if (records.isEmpty()) {
            binding.progressMessage.setText(R.string.start_progress);
            binding.latestValue.setText(getString(R.string.latest_stat, 0.0));
            binding.remainingValue.setText(getString(R.string.remaining_stat, 0.0));
        } else {
            binding.progressMessage.setText(
                    progress.isGoalReached()
                            ? getString(R.string.goal_reached)
                            : getString(R.string.progress_message, progress.getPercent())
            );
            binding.latestValue.setText(getString(R.string.latest_stat, progress.getLatest()));
            binding.remainingValue.setText(
                    progress.isGoalReached()
                            ? getString(R.string.goal_reached_stat)
                            : getString(R.string.remaining_stat, progress.getRemaining())
            );
        }
    }

    @Override
    public void onEdit(WeightRecord record) {
        openWeightForm(record);
    }

    @Override
    public void onDelete(WeightRecord record) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_entry_title)
                .setMessage(R.string.delete_entry_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteRecord(record))
                .show();
    }

    private void deleteRecord(WeightRecord record) {
        long userId = sessionStore.getUserId();
        AppExecutors.database().execute(() -> {
            boolean deleted = weightRepository.delete(userId, record.getId());
            runOnUiThread(() -> {
                if (deleted) {
                    loadDashboard();
                } else {
                    Snackbar.make(binding.getRoot(), R.string.error_generic, Snackbar.LENGTH_LONG)
                            .show();
                }
            });
        });
    }

    private void openWeightForm(WeightRecord record) {
        Intent intent = new Intent(this, WeightFormActivity.class);
        if (record != null) {
            intent.putExtra(WeightFormActivity.EXTRA_RECORD_ID, record.getId());
            intent.putExtra(WeightFormActivity.EXTRA_DATE, record.getDate());
            intent.putExtra(WeightFormActivity.EXTRA_WEIGHT, record.getWeight());
        }
        startActivity(intent);
    }

    private void returnToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
