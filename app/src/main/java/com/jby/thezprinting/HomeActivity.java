package com.jby.thezprinting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jby.thezprinting.dialog.CustomerDialog;
import com.jby.thezprinting.dialog.ProductDialog;
import com.jby.thezprinting.dialog.SupplierDialog;
import com.jby.thezprinting.registration.LoginActivity;
import com.jby.thezprinting.shareObject.AnimationUtility;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;
import com.jby.thezprinting.sharePreference.SharedPreferenceManager;
import com.robinhood.ticker.TickerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private CardView documentLayout, productLayout, customerLayout, supplierLayout, signOutLayout;
    private AppBarLayout toolBarLayout;
    private TextView homeActivityVersion;
    private TickerView pendingNum, completeNum;
    private NestedScrollView nestedScrollView;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    /*
     * exit purpose
     * */
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        documentLayout = findViewById(R.id.document_layout);
        productLayout = findViewById(R.id.product_layout);
        customerLayout = findViewById(R.id.customer_layout);
        supplierLayout = findViewById(R.id.supplier_layout);
        signOutLayout = findViewById(R.id.log_out_layout);

        pendingNum = findViewById(R.id.activity_home_pending_balance);
        completeNum = findViewById(R.id.activity_home_complete_balance);

        toolBarLayout = findViewById(R.id.activity_home_tool_bar_layout);
        nestedScrollView = findViewById(R.id.activity_home_scroll_view);
        homeActivityVersion = findViewById(R.id.activity_home_version_name);
    }

    private void objectSetting() {
        /*
        * full screen
        * */
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        documentLayout.setOnClickListener(this);
        productLayout.setOnClickListener(this);
        customerLayout.setOnClickListener(this);
        signOutLayout.setOnClickListener(this);
        supplierLayout.setOnClickListener(this);

        displayVersion();
        setUpView();
        gettingInvoiceDetail();
        registerToken();
    }

    private void setUpView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new AnimationUtility().layoutSwipeDownIn(HomeActivity.this, toolBarLayout);
                new AnimationUtility().fadeInVisible(HomeActivity.this, nestedScrollView);
            }
        }, 100);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.document_layout:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.product_layout:
                openProductDialog();
                break;
            case R.id.customer_layout:
                openCustomerDialog();
                break;
            case R.id.supplier_layout:
                openSupplierDialog();
                break;
            case R.id.log_out_layout:
                logOutRequest();
                break;
        }
    }

    private void openProductDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isMainActivity", true);
        bundle.putBoolean("isUpdateList", false);

        DialogFragment dialogFragment = new ProductDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private void openCustomerDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isMainActivity", true);

        DialogFragment dialogFragment = new CustomerDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private void openSupplierDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isMainActivity", true);

        DialogFragment dialogFragment = new SupplierDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    public void logOutRequest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to sign out?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Sign Out",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferenceManager.setUserId(getApplicationContext(), "default");
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onBackPressed() {
        exit();
    }

    public void exit() {
        if (exit) {
            moveTaskToBack(true);
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    /*
    * count invoice
    * */
    private void gettingInvoiceDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("company_id", SharedPreferenceManager.getCompanyId(getApplicationContext())));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().home,
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("invoice_detail");
                                            for(int i = 0 ; i < jsonArray.length(); i++){
                                                if(jsonArray.getJSONObject(i).getString("status").equals("1")){
                                                    completeNum.setText(jsonArray.getJSONObject(i).getString("num_invoice"));
                                                }
                                                else{
                                                    pendingNum.setText(jsonArray.getJSONObject(i).getString("num_invoice"));
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    } catch (NullPointerException | IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void displayVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Power By Channel Soft \n" + "Version " + pInfo.versionName;
            homeActivityVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------register token-------------------------------------------------------------------*/
    private void registerToken() {
        FirebaseApp.initializeApp(this);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FireBase", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = Objects.requireNonNull(task.getResult()).getToken();
                        Log.d("FireBase", "token: " + token);
                        updateToken(token);
                    }
                });
    }

    private void updateToken(final String token) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("company_id", SharedPreferenceManager.getCompanyId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("token", token));
                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().notification,
                        new ApiManager().getResultParameter(
                                "",
                                new ApiManager().setData(apiDataObjectArrayList),
                                ""
                        )
                );
                asyncTaskManager.execute();

                if (!asyncTaskManager.isCancelled()) {

                    try {
                        JSONObject jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);
                        if (jsonObjectLoginResponse != null) {
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                        } else {
                            CustomToast(getApplicationContext(), "No Network Connection");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
