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

public class FilterEventsFragment extends DialogFragment {

    private Spinner spinnerCategoryFilter;
    private Button btnConfirmFilter, btnCancelFilter;

    /**
     * Purpose: Creates a filter dialog with Spinner and buttons.
     * Input: savedInstanceState contains previous fragment state if it exists.
     * Output: DialogFragment view is displayed.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_filter_events, null);

        connectViews(view);
        setupSpinner();
        setButtonListeners();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        return builder.create();
    }

    /**
     * Purpose: Connects XML components to Java variables.
     * Input: view is the fragment layout.
     * Output: Spinner and buttons are ready to use.
     */
    private void connectViews(View view) {
        spinnerCategoryFilter = view.findViewById(R.id.spinnerCategoryFilter);
        btnConfirmFilter = view.findViewById(R.id.btnConfirmFilter);
        btnCancelFilter = view.findViewById(R.id.btnCancelFilter);
    }

    /**
     * Purpose: Loads event categories from strings.xml into Spinner.
     * Input: None.
     * Output: Spinner shows event categories.
     */
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.event_categories,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(adapter);
    }

    /**
     * Purpose: Adds actions to confirm and cancel buttons.
     * Input: None.
     * Output: Confirm filters events, cancel closes dialog.
     */
    private void setButtonListeners() {
        btnConfirmFilter.setOnClickListener(v -> {
            String selectedCategory = spinnerCategoryFilter.getSelectedItem().toString();

            if (getActivity() instanceof BrowseEventsActivity) {
                ((BrowseEventsActivity) getActivity()).filterEventsByCategory(selectedCategory);
            }

            dismiss();
        });

        btnCancelFilter.setOnClickListener(v -> dismiss());
    }
}