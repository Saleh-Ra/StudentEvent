package com.example.studentevent.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.studentevent.R;

/**
 * Purpose: DialogFragment for filtering events by category and registered participants.
 * Input: User selection from spinners.
 * Output: Calls applyFilter in BrowseEventsActivity.
 */
public class FilterEventsFragment extends DialogFragment {

    private Spinner spinnerCategoryFilter, spinnerMinParticipants;
    private Button btnConfirmFilter, btnCancelFilter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_filter_events, null);

        connectViews(view);
        setupSpinners();
        setButtonListeners();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        return builder.create();
    }

    private void connectViews(View view) {
        spinnerCategoryFilter = view.findViewById(R.id.spinnerCategoryFilter);
        spinnerMinParticipants = view.findViewById(R.id.spinnerMinParticipants);
        btnConfirmFilter = view.findViewById(R.id.btnConfirmFilter);
        btnCancelFilter = view.findViewById(R.id.btnCancelFilter);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.event_categories, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(catAdapter);

        ArrayAdapter<CharSequence> partAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.min_participants_options, android.R.layout.simple_spinner_item);
        partAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinParticipants.setAdapter(partAdapter);
    }

    private void setButtonListeners() {
        btnConfirmFilter.setOnClickListener(v -> {
            String category = spinnerCategoryFilter.getSelectedItem().toString();
            int minParticipants = getMinParticipantsValue(spinnerMinParticipants.getSelectedItemPosition());

            if (getActivity() instanceof BrowseEventsActivity) {
                ((BrowseEventsActivity) getActivity()).applyFilter(category, minParticipants);
            }
            dismiss();
        });

        btnCancelFilter.setOnClickListener(v -> dismiss());
    }

    private int getMinParticipantsValue(int position) {
        switch (position) {
            case 1: return 5;
            case 2: return 10;
            case 3: return 20;
            default: return 0;
        }
    }
}