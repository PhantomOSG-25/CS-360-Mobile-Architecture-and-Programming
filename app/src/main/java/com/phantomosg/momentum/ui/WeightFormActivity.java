package com.phantomosg.momentum.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.phantomosg.momentum.R;
import com.phantomosg.momentum.data.AuthRepository;
import com.phantomosg.momentum.data.WeightRepository;
import com.phantomosg.momentum.databinding.ActivityWeightFormBinding;
import com.phantomosg.momentum.model.UserProfile;
import com.phantomosg.momentum.model.WeightRecord;
import com.phantomosg.momentum.util.AppExecutors;
import com.phantomosg.momentum.util.InputValidator;
import com.phantomosg.momentum.util.NotificationHelper;
import com.phantomosg.momentum.util.ProgressCalculator;
import com.phantomosg.momentum.util.SessionStore;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class WeightFormActivity extends AppCompatActivity {
    public static final String EXTRA_RECORD_ID = "record_id";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_WEIGHT = "weight";

    private ActivityWeightFormBinding binding;
    private WeightRepository weightRepository;
    private AuthRepository authRepository;
    private SessionStore sessionStore;
    private long recordId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeightFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        weightRepository = new WeightRepository(this);
        authRepository = new AuthRepository(this);
        sessionStore = new SessionStore(this);
        if (!sessionStore.isSignedIn()) {
            finish();
            return;
        }

        recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1L);
        boolean editing = recordId >= 0;
        binding.toolbar.setTitle(editing ? R.string.edit_entry : R.string.add_entry);
        binding.toolbar.setNavigationOnClickListener(view -> finish());
        binding.dateInput.setText(
                editing ? getIntent().getStringExtra(EXTRA_DATE) : LocalDate.now().toString()
        );
        if (editing) {
            binding.weightInput.setText(
                    String.format(Locale.US, "%.1f", getIntent().getDoubleExtra(EXTRA_WEIGHT, 0))
            );
        }
        binding.dateInput.setOnClickListener(view -> showDatePicker());
        binding.saveButton.setOnClickListener(view -> save());
    }

    private void showDatePicker() {
        LocalDate selected;
        try {
            selected = LocalDate.parse(text(binding.dateInput.getText()));
        } catch (RuntimeException invalidDate) {
            selected = LocalDate.now();
        }
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> binding.dateInput.setText(
                        LocalDate.of(year, month + 1, day).toString()
                ),
                selected.getYear(),
                selected.getMonthValue() - 1,
                selected.getDayOfMonth()
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void save() {
        binding.dateLayout.setError(null);
        binding.weightLayout.setError(null);
        String date = text(binding.dateInput.getText());
        String weightText = text(binding.weightInput.getText());
        if (!InputValidator.isIsoDate(date)) {
            binding.dateLayout.setError(getString(R.string.error_required));
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException invalidNumber) {
            binding.weightLayout.setError(getString(R.string.error_weight_range));
            return;
        }
        if (!InputValidator.isValidWeight(weight)) {
            binding.weightLayout.setError(getString(R.string.error_weight_range));
            return;
        }

        binding.saveButton.setEnabled(false);
        long userId = sessionStore.getUserId();
        AppExecutors.database().execute(() -> {
            boolean success = recordId >= 0
                    ? weightRepository.update(userId, recordId, date, weight)
                    : weightRepository.add(userId, date, weight) >= 0;
            UserProfile user = success ? authRepository.getUser(userId) : null;
            List<WeightRecord> records =
                    success ? weightRepository.getAll(userId) : Collections.emptyList();
            runOnUiThread(() -> {
                if (isFinishing()) return;
                binding.saveButton.setEnabled(true);
                if (!success) {
                    Snackbar.make(
                            binding.getRoot(), R.string.error_duplicate_date, Snackbar.LENGTH_LONG
                    ).show();
                    return;
                }
                if (user != null && user.isNotificationsEnabled()
                        && ProgressCalculator.calculate(records, user.getGoalWeight()).isGoalReached()) {
                    NotificationHelper.showGoalReached(this);
                }
                finish();
            });
        });
    }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
