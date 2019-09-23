package com.jby.thezprinting.product;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.thezprinting.R;
import com.jby.thezprinting.dialog.CustomerDialog;
import com.jby.thezprinting.dialog.SupplierDialog;
import com.jby.thezprinting.object.DocumentObject;
import com.jby.thezprinting.object.SupplierObject;
import com.jby.thezprinting.others.KeyboardHelper;
import com.jby.thezprinting.others.SwipeDismissTouchListener;

import java.util.Objects;


public class AddSupplierPriceDialog extends DialogFragment implements View.OnClickListener, SupplierDialog.SupplierDialogCallBack {
    View rootView;
    private AddSupplierPriceDialogCallBack addSupplierPriceDialogCallBack;

    private EditText tvPrice, tvUnit;
    private TextView tvSupplier;
    private LinearLayout selectSupplierLayout;
    private Button confirmButton;
    private SupplierObject supplierObject;
    private boolean isUpdate = false;

    public AddSupplierPriceDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_supplier_price_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        tvPrice = rootView.findViewById(R.id.price);
        tvUnit = rootView.findViewById(R.id.unit);
        tvSupplier = rootView.findViewById(R.id.select_supplier);
        confirmButton = rootView.findViewById(R.id.confirm_button);
        selectSupplierLayout = rootView.findViewById(R.id.select_supplier_layout);

        addSupplierPriceDialogCallBack = (AddSupplierPriceDialogCallBack) getActivity();

    }

    private void objectSetting() {
        if (getArguments() != null) {
            supplierObject = (SupplierObject) getArguments().getSerializable("object");
            setInitialValue();
        }
        selectSupplierLayout.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
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

    private void setInitialValue() {
        isUpdate = true;
        tvPrice.append(supplierObject.getPrice());
        tvUnit.append(supplierObject.getUnit());
        tvSupplier.setText(supplierObject.getName());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_supplier_layout:
                openSupplierDialog();
                break;
            case R.id.confirm_button:
                addSupplierPrice();
                break;
        }
    }

    private void openSupplierDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isMainActivity", false);

        DialogFragment dialogFragment = new SupplierDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "");
    }

    @Override
    public void selectedItem(SupplierObject supplierObject) {
        this.supplierObject = new SupplierObject();
        this.supplierObject = supplierObject;
        tvSupplier.setText(supplierObject.getName());
    }

    private void addSupplierPrice() {
        if (!tvPrice.getText().toString().equals("") && !tvUnit.getText().toString().equals("") && supplierObject != null) {
            supplierObject.setPrice(tvPrice.getText().toString());
            supplierObject.setUnit(tvUnit.getText().toString());

            if (!isUpdate)
                supplierObject.setNewAdded(true);
            else
                supplierObject.setUpdated(true);

            addSupplierPriceDialogCallBack.addSupplierPrice(supplierObject);
            dismiss();
        } else
            Toast.makeText(getActivity(), "Every Field Above is Required!", Toast.LENGTH_SHORT).show();
    }

    public interface AddSupplierPriceDialogCallBack {
        void addSupplierPrice(SupplierObject supplierObject);
    }
}
