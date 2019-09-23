package com.jby.thezprinting.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jby.thezprinting.R;
import com.jby.thezprinting.others.KeyboardHelper;
import com.jby.thezprinting.others.SwipeDismissTouchListener;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;

public class DepositDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    /*
     * progress bar
     * */
    private ProgressBar progressBar;
    DepositDialogCallBack depositDialogCallBack;
    private String invoiceID, previousDeposit;
    private EditText deposit;
    private Button save;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public DepositDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.deposit_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        depositDialogCallBack = (DepositDialogCallBack) getActivity();
        progressBar = rootView.findViewById(R.id.progress_bar);
        deposit = rootView.findViewById(R.id.deposit);
        save = rootView.findViewById(R.id.save_button);
    }

    private void objectSetting() {
        save.setOnClickListener(this);
        if (getArguments() != null) {
            invoiceID = getArguments().getString("invoice_id");
            previousDeposit = getArguments().getString("deposit");
            if (Float.valueOf(previousDeposit) > 0)
                deposit.append(previousDeposit);
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
    public void dismiss() {
        KeyboardHelper.hideSoftKeyboard(getActivity(), deposit);
        super.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                showProgressBar(true);
                try {
                    if (!deposit.getText().toString().equals("")) {
                        saveDeposit();
                    } else {
                        Toast.makeText(getActivity(), "Invalid Input!", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Invalid Input!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void saveDeposit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("invoice_id", invoiceID));
                apiDataObjectArrayList.add(new ApiDataObject("deposit", deposit.getText().toString()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().invoice,
                        new ApiManager().getResultParameter(
                                "",
                                new ApiManager().setData(apiDataObjectArrayList),
                                ""
                        )
                );
                asyncTaskManager.execute();

                if (!asyncTaskManager.isCancelled()) {

                    try {
                        jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                        if (jsonObjectLoginResponse != null) {
                            Log.d("jsonObject", "jsonObject: haha " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        depositDialogCallBack.updateDeposit(deposit.getText().toString());
                                        dismiss();
                                    }
                                });
                            }
                        } else {
                            CustomToast(getActivity(), "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getActivity(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getActivity(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getActivity(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void showProgressBar(final boolean show) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);

                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public interface DepositDialogCallBack {
        void updateDeposit(String deposit);
    }
}
