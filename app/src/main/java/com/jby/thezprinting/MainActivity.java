package com.jby.thezprinting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;
import com.jby.thezprinting.document.DetailActivity;
import com.jby.thezprinting.document.InvoiceFragment;
import com.jby.thezprinting.document.QuotationFragment;
import com.jby.thezprinting.registration.LoginActivity;
import com.jby.thezprinting.shareObject.NetworkConnection;
import com.jby.thezprinting.sharePreference.SharedPreferenceManager;

import java.io.FileNotFoundException;
import java.util.Objects;

import static com.jby.thezprinting.shareObject.VariableUtils.REQUEST_UPDATE;

public class MainActivity extends AppCompatActivity {
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
    /*
     * exit prupose
     * */
    private boolean exit = false;

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
                if (fragmentClass == QuotationFragment.class) quotationFragment = (QuotationFragment) fragment;
                else invoiceFragment = (InvoiceFragment) fragment;

            }
            else Toast.makeText(this, "No fragment found", Toast.LENGTH_SHORT).show();
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

    }

    public void openDetailActivity(Bundle bundle){
        startActivityForResult(new Intent(this, DetailActivity.class).putExtras(bundle), REQUEST_UPDATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_UPDATE)
        {
            if (fragmentClass == QuotationFragment.class) quotationFragment.onRefresh();
            else invoiceFragment.onRefresh();
        }
    }

    /*----------------------------------------------on back press------------------------------------------------------------------*/

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                logOutRequest();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

}
