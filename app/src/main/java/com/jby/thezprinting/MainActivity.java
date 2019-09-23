package com.jby.thezprinting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jby.thezprinting.dialog.CustomerDialog;
import com.jby.thezprinting.dialog.SortingDialog;
import com.jby.thezprinting.dialog.SupplierDialog;
import com.jby.thezprinting.document.DetailActivity;
import com.jby.thezprinting.document.InvoiceFragment;
import com.jby.thezprinting.document.QuotationFragment;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;
import com.jby.thezprinting.shareObject.NetworkConnection;
import com.jby.thezprinting.sharePreference.SharedPreferenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;
import static com.jby.thezprinting.shareObject.VariableUtils.REQUEST_UPDATE;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SortingDialog.SortingDialogCallBack {
    private Toolbar toolbar;
    BottomNavigationView navView;
    private FrameLayout frameLayout;
    Class fragmentClass;
    //progress bar
    public ProgressBar progressBar;
    /*
     * fragment control purpose
     * */
    public static Fragment fragment;
    private QuotationFragment quotationFragment;
    private InvoiceFragment invoiceFragment;
    private int lastFragment = -1;
    private String query = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.nav_view);
        frameLayout = findViewById(R.id.frameLayout);
        progressBar = findViewById(R.id.progress_bar);
        fragmentClass = QuotationFragment.class;
    }

    private void objectSetting() {
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.quotation);
        navView.setItemIconTintList(null);
        setupActionBar("Quotation");
        /*
         * if get data from notification
         * */
        if (getIntent().getExtras() != null) {
            String type = getIntent().getExtras().getString("channel_id");
            if (type != null) {
                navView.setSelectedItemId(type.equals("2") ? R.id.quotation : R.id.invoice);
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.quotation:
                    fragmentClass = QuotationFragment.class;
                    setupActionBar("Quotation");
                    break;
                case R.id.invoice:
                    fragmentClass = InvoiceFragment.class;
                    setupActionBar("Invoice");
                    break;
            }

            if (lastFragment != item.getItemId()) {
                checkInternetConnection(null);
            }
            lastFragment = item.getItemId();
            return true;
        }
    };

    public void checkInternetConnection(View view) {
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderFragment();
    }

    private void renderFragment() {
        boolean connection = new NetworkConnection(this).checkNetworkConnection();
        frameLayout.setVisibility(connection ? View.VISIBLE : View.GONE);
        if (connection) {
            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.frameLayout, fragment).commit();
                /*
                 * register fragment
                 * */
                if (fragmentClass == QuotationFragment.class)
                    quotationFragment = (QuotationFragment) fragment;
                else invoiceFragment = (InvoiceFragment) fragment;

            } else Toast.makeText(this, "No fragment found", Toast.LENGTH_SHORT).show();
        } else showSnackBar("No Internet Connection!");
    }

    //---------------------------------------------------------------------share method----------------------------------------------------------------------------
    public void showSnackBar(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }

    public void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupActionBar(String title) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void openDetailActivity(Bundle bundle) {
        startActivityForResult(new Intent(this, DetailActivity.class).putExtras(bundle), REQUEST_UPDATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_UPDATE) {
            if (fragmentClass == QuotationFragment.class) quotationFragment.onRefresh();
            else invoiceFragment.onRefresh();
        }
    }

    /*----------------------------------------------on back press------------------------------------------------------------------*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_action_menu, menu);
        /*
         * search purpose
         * */
        MenuItem searchId = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchId.getActionView();
        EditText editText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editText.setTextColor(Color.WHITE);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sorting:
                openSortingDialog();
                return true;
            case R.id.customer:
                openCustomerDialog();
                return true;
            case R.id.supplier:
                openSupplierDialog();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /*
     * Search purpose
     * */
    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String s) {
        showProgressBar(true);
        query = s;
        if (fragmentClass == QuotationFragment.class) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    quotationFragment.fetchParentItem(s);
                }
            }, 300);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    invoiceFragment.fetchParentItem(s);
                }
            }, 300);
        }
        return true;
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

    /*------------------------------------------------sorting purpose------------------------------------------------------------------*/
    private void openSortingDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("date_start", fragmentClass == QuotationFragment.class ? quotationFragment.startDate : invoiceFragment.startDate);
        bundle.putString("date_end", fragmentClass == QuotationFragment.class ? quotationFragment.endDate : invoiceFragment.endDate);

        DialogFragment dialogFragment = new SortingDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void applySorting(String dateStart, String dateEnd) {
        if (fragmentClass == QuotationFragment.class) {
            quotationFragment.startDate = dateStart;
            quotationFragment.endDate = dateEnd;
            quotationFragment.fetchParentItem(query);
        } else {
            invoiceFragment.startDate = dateStart;
            invoiceFragment.endDate = dateEnd;
        }
    }
}
