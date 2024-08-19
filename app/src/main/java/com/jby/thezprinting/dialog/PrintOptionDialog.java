package com.jby.thezprinting.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


import com.jby.thezprinting.R;
import com.jby.thezprinting.others.SwipeDismissTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrintOptionDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    View rootView;
    PrintOptionDialogCallBack printOptionDialogCallBack;
    private AppCompatCheckBox autoSpacing, artworkProvided;
    private EditText customSpacing, rate;
    private LinearLayout rateLayout, artworkLayout, currencLayout;
    private Spinner currency;
    private Button print;

    public PrintOptionDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.space_counting_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        printOptionDialogCallBack = (PrintOptionDialogCallBack) getActivity();
        autoSpacing = rootView.findViewById(R.id.auto_spacing);
        customSpacing = rootView.findViewById(R.id.custom_spacing);

        rate = rootView.findViewById(R.id.rate);
        rateLayout = rootView.findViewById(R.id.rate_layout);

        artworkLayout = rootView.findViewById(R.id.artwork_layout);
        artworkProvided = rootView.findViewById(R.id.artwork_provided);

        currencLayout = rootView.findViewById(R.id.currency_layout);
        currency = rootView.findViewById(R.id.currency);

        print = rootView.findViewById(R.id.print_button);
    }

    private void objectSetting() {
        print.setOnClickListener(this);
        autoSpacing.setOnCheckedChangeListener(this);
        artworkProvided.setOnCheckedChangeListener(this);
        currency.setOnItemSelectedListener(this);
        setupSpinner();
        if (getArguments() != null) {
            artworkLayout.setVisibility(getArguments().getBoolean("isDeliveryOrder") ? View.GONE : View.VISIBLE);
            artworkProvided.setChecked(!getArguments().getBoolean("isDeliveryOrder"));

            currencLayout.setVisibility(getArguments().getBoolean("isDeliveryOrder") ? View.GONE : View.VISIBLE);
        }
    }

    private void setupSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("RM");
        categories.add("SGD");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.custom_spinner_layout, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        currency.setAdapter(dataAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        Objects.requireNonNull(d.getWindow()).getDecorView().setOnTouchListener(new SwipeDismissTouchListener(d.getWindow().getDecorView(), null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        dismiss();
                    }
                }));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.print_button:
                try {
                    printOptionDialogCallBack.getCompanyDetailToPrint(
                            autoSpacing.isChecked(),
                            artworkProvided.isChecked(),
                            autoSpacing.isChecked() ? 0 : Integer.valueOf(customSpacing.getText().toString().trim()),
                            currency.getSelectedItem().toString(),
                            currency.getSelectedItemPosition() == 0 ? 1 : Double.valueOf(rate.getText().toString().trim()));

                    dismiss();
                } catch (NullPointerException | NumberFormatException e) {
                    Toast.makeText(getActivity(), "Invalid Input", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.auto_spacing)
            customSpacing.setVisibility(b ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        rateLayout.setVisibility(i == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface PrintOptionDialogCallBack {
        void getCompanyDetailToPrint(boolean autoSpacing, boolean artworkProvided, int customSpacing, String currency, double rate);
    }
}
