package com.jby.thezprinting.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.jby.thezprinting.R;
import com.jby.thezprinting.others.SwipeDismissTouchListener;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class SortingDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private TextView tvDateStart, tvDateEnd;
    private Button applyButton;
    private ImageView cancelButton;

    public SortingDialogCallBack sortingDialogCallBack;

    public SortingDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.sorting_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        sortingDialogCallBack = (SortingDialogCallBack) getActivity();

        tvDateStart = rootView.findViewById(R.id.sorting_dialog_start_date);
        tvDateEnd = rootView.findViewById(R.id.sorting_dialog_end_date);

        applyButton = rootView.findViewById(R.id.sorting_dialog_apply_button);
        cancelButton = rootView.findViewById(R.id.sorting_dialog_cancel_button);
    }

    private void objectSetting() {
        tvDateStart.setOnClickListener(this);
        tvDateEnd.setOnClickListener(this);

        applyButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            tvDateStart.setText(bundle.getString("date_start"));
            tvDateEnd.setText(bundle.getString("date_end"));
        }
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
            /*
             * date
             * */
            case R.id.sorting_dialog_start_date:
                selectDate(true);
                break;
            case R.id.sorting_dialog_end_date:
                selectDate(false);
                break;
            /*
             * button
             * */
            case R.id.sorting_dialog_apply_button:
                applySorting();
                break;
            case R.id.sorting_dialog_cancel_button:
                dismiss();
        }
    }

    private void selectDate(final boolean dateStart) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = String.format("%s", String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth));
                        if (dateStart) tvDateStart.setText(selectedDate);
                        else tvDateEnd.setText(selectedDate);
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void applySorting() {
        sortingDialogCallBack.applySorting(tvDateStart.getText().toString(), tvDateEnd.getText().toString());
        dismiss();
    }

    public interface SortingDialogCallBack {
        void applySorting(String dateStart, String dateEnd);
    }
}
