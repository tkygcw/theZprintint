package com.jby.thezprinting.document;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.DocumentObject;

import java.util.Objects;

import com.jby.thezprinting.others.KeyboardHelper;
import com.jby.thezprinting.others.SwipeDismissTouchListener;


public class AddItemDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private EditText tvItem, tvQuantity, tvPrice;
    private Button addButton, closeButton;
    private AddItemDialogCallBack addItemDialogCallBack;
    /*
     * to determine whether is update or create new
     * */
    private String action;
    /*
    * Document Object
    * */
    private DocumentObject documentObject;

    public AddItemDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_item_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        addItemDialogCallBack = (AddItemDialogCallBack) getActivity();
        tvItem = rootView.findViewById(R.id.item);
        tvQuantity = rootView.findViewById(R.id.quantity);
        tvPrice = rootView.findViewById(R.id.price);
        addButton = rootView.findViewById(R.id.add_button);
        closeButton = rootView.findViewById(R.id.close);
    }

    private void objectSetting() {
        addButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        clickEnterToPerformAction();

        Bundle bundle = getArguments();
        if (bundle != null) {
            action = bundle.getString("action");
            if(!action.equals("create")){
                documentObject = (DocumentObject) bundle.getSerializable("detail");
                setupUpdateValue();
            }
            else{
                addButton.setText("Add Item");
            }
        }
    }

    private void setupUpdateValue(){
        tvItem.append(documentObject.getItem());
        tvQuantity.append(documentObject.getQuantity());
        tvPrice.append(documentObject.getPrice());
        addButton.setText("Update Item");
    }

    private void clickEnterToPerformAction(){
        tvPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    add();
                    handled = true;
                }
                return handled;
            }
        });
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
            case R.id.add_button:
                add();
                break;
            case R.id.close:
                dismiss();
                break;
        }
    }

    private void add() {
        if (!tvItem.getText().toString().equals("") && !tvQuantity.getText().toString().equals("") && !tvPrice.getText().toString().equals("")) {
            try {
                String item = tvItem.getText().toString();
                String quantity = tvQuantity.getText().toString();
                float price = Float.valueOf(tvPrice.getText().toString());

                float subTotal = price * Float.valueOf(quantity);

                addItemDialogCallBack.returnData(action, new DocumentObject("", item, String.format ("%.2f", price), quantity, String.format ("%.2f", subTotal), ""));
                //reset after insert
                reset();
            } catch (NumberFormatException e) {
                showSnackBar("Invalid Input");
                e.printStackTrace();
            }
        }
        else{
            showSnackBar("All field are required!");
        }
    }

    private void reset() {
        tvItem.setText("");
        tvPrice.setText("");
        tvQuantity.setText("");
        showSnackBar("Insert Successfully!");
    }

    public void showSnackBar(final String message) {
        final Snackbar snackbar = Snackbar.make(getDialog().findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void dismiss() {
        KeyboardHelper.hideSoftKeyboard(getActivity(), tvPrice);
        super.dismiss();
    }

    public interface AddItemDialogCallBack {
        void returnData(String action, DocumentObject documentObject);
    }
}
