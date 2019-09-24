package com.jby.thezprinting.document;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.jby.thezprinting.R;
import com.jby.thezprinting.adapter.DocumentDetailAdapter;
import com.jby.thezprinting.dialog.CustomerDialog;
import com.jby.thezprinting.dialog.DepositDialog;
import com.jby.thezprinting.dialog.PrintOptionDialog;
import com.jby.thezprinting.object.CustomerObject;
import com.jby.thezprinting.object.DocumentObject;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.jby.thezprinting.others.ExpandableHeightListView;
import com.jby.thezprinting.sharePreference.SharedPreferenceManager;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.view.View.GONE;
import static com.itextpdf.text.html.WebColors.getRGBColor;
import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;
import static com.jby.thezprinting.shareObject.VariableUtils.REQUEST_UPDATE;
import static com.jby.thezprinting.shareObject.VariableUtils.REQUEST_WRITE_EXTERNAL_PERMISSION;


public class DetailActivity extends AppCompatActivity implements View.OnClickListener, AbsListView.MultiChoiceModeListener,
        AddItemDialog.AddItemDialogCallBack, AdapterView.OnItemClickListener, CustomerDialog.CustomerDialogCallBack,
        PrintOptionDialog.PrintOptionDialogCallBack, DepositDialog.DepositDialogCallBack {
    private Toolbar toolbar;
    /*
     * intent parameter
     * */
    private DocumentObject documentHeaderInformation;
    private String createdDate;
    private String intentAction = "create";
    private String intentType = "quotation";
    /*
     * Async Task
     * */
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    /*
     * progress bar
     * */
    private ProgressBar progressBar;
    /*
     * customer purpose
     * */
    private TextView selectedCustomer;
    private LinearLayout selectCustomerLayout;
    private CustomerObject customerObject;

    /*
     * item purpose
     * */
    private TextView no_item_layout, status;
    private Button addItemButton, actionButton;
    /*
     * deposit purpose
     * */
    private Button depositButton;
    private LinearLayout depositLayout, balanceLayout;
    private TextView tvDeposit, balance;
    /*
     * date
     * */
    private TextView tvDate;
    private String selectedDate;
    /*
     * delete purpose
     * */
    SparseBooleanArray checkDeleteItem;
    List<String> list = new ArrayList<String>();
    ActionMode actionMode;
    /*
     * total layout
     * */
    private LinearLayout totalLayout;
    private TextView total;
    float totalPrice = 0;
    private float deposit = 0;
    /*
     * pdf
     * */
    private boolean isDeliveryOrder = false;
    //spacing
    private boolean autoSpacing;
    private boolean artWorkProvided = true;
    private int customSpacing;
    //currency
    private String currency;
    private double rate = 1;

    private File pdfFile;
    Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    Font smallBoldFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD);
    Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    Font largeFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD);
    Font smallFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
    Font mandarin;
    /*
     * on back press control
     * */
    int initializeSize = 0;

    private ExpandableHeightListView listView;
    private ArrayList<DocumentObject> documentObjectArrayList;
    private DocumentDetailAdapter documentDetailAdapter;
    private int selectedPosition = 0;
    /*
     * dialog
     * */
    private SweetAlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        toolbar = findViewById(R.id.toolbar);
        //not found layout
        tvDate = findViewById(R.id.date);
        no_item_layout = findViewById(R.id.no_item_found);
        addItemButton = findViewById(R.id.add_button);
        actionButton = findViewById(R.id.action_button);
        status = findViewById(R.id.status);

        depositButton = findViewById(R.id.deposit_button);
        tvDeposit = findViewById(R.id.deposit);
        balance = findViewById(R.id.balance);
        depositLayout = findViewById(R.id.deposit_layout);
        balanceLayout = findViewById(R.id.balance_layout);


        listView = findViewById(R.id.list_view);
        documentObjectArrayList = new ArrayList<>();
        documentDetailAdapter = new DocumentDetailAdapter(this, documentObjectArrayList);

        totalLayout = findViewById(R.id.total_layout);
        total = findViewById(R.id.total);

        selectCustomerLayout = findViewById(R.id.select_customer_layout);
        selectedCustomer = findViewById(R.id.select_customer);

        progressBar = findViewById(R.id.progress_bar);
        handler = new Handler();
    }

    private void objectSetting() {
        tvDate.setOnClickListener(this);
        addItemButton.setOnClickListener(this);
        actionButton.setOnClickListener(this);
        depositButton.setOnClickListener(this);
        selectCustomerLayout.setOnClickListener(this);

        listView.setMultiChoiceModeListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemClickListener(this);
        listView.setAdapter(documentDetailAdapter);
        listView.setExpanded(true);

        if (getIntent().getExtras() != null) {
            createdDate = getIntent().getExtras().getString("created_at");
            intentAction = getIntent().getExtras().getString("action");
            intentType = getIntent().getExtras().getString("type");
            actionButton.setText(intentType.equals("quotation") ? "Upload Quotation" : "Upload Invoice");
            depositButton.setVisibility(intentType.equals("quotation") ? View.GONE : View.VISIBLE);
            /*
             * edit
             * */
            if (intentAction.equals("edit")) {
                showProgressBar(true);
                documentHeaderInformation = (DocumentObject) getIntent().getExtras().getSerializable("object");
                setUpdateLayout();
            }
            /*
             * create new
             * */
            else {
                setCreateLayout();
            }
        }
        setupActionBar();
        setUpMandarin();
    }


    private void setupActionBar() {
        try {
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(intentType.equals("quotation") ? "Quotation" : "Invoice" + (intentAction.equals("edit") ? ": " + setQuotationPlaceHolder(documentHeaderInformation.getDocumentNo()) : ""));

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private String setQuotationPlaceHolder(String do_id) {
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for (int i = do_idBuilder.length(); i < 5; i++) {
            do_idBuilder.insert(0, "0");
        }
        return (intentType.equals("quotation") ? "#Q" : "#I") + do_idBuilder.toString();
    }

    /*--------------------------------------------------------------------menu purpose---------------------------------------------------------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        setMenuItemVisibility(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteConfirmationDialog(true);
                return true;
            case R.id.print_do:
                isDeliveryOrder = true;
                printAction();
                return true;
            case R.id.print:
                isDeliveryOrder = false;
                printAction();
                return true;
            case R.id.convert:
                convertQuotationToInvoice();
                return true;
            case R.id.complete:
                updateInvoiceStatusConfirmation();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void openPrintOptionDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isDeliveryOrder", isDeliveryOrder);

        DialogFragment dialogFragment = new PrintOptionDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private void printAction() {
        if (documentObjectArrayList.size() > 0) {
            if (initializeSize != documentObjectArrayList.size()) {
                requestUploadBeforePrintConfirmation();
            } else {
                try {
                    createPdfWrapper();
                } catch (FileNotFoundException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "Nothing to print!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setMenuItemVisibility(Menu menu) {
        menu.findItem(R.id.convert).setVisible(intentType.equals("quotation"));
        menu.findItem(R.id.complete).setVisible(!intentType.equals("quotation"));
        menu.findItem(R.id.print_do).setVisible(true);
    }

    /*-------------------------------------------------------------------------create purpose---------------------------------------------------------------------*/
    private void setCreateLayout() {
        status.setVisibility(GONE);
        setCurrentTime();
    }

    /*------------------------------------------------------------------------edit purpose------------------------------------------------------------------------*/
    private void setUpdateLayout() {
        tvDate.setText(createdDate);
        /*
         * set status
         * */
        switch (documentHeaderInformation.getStatus()) {
            case "0":
                status.setText("Pending");
                status.setBackground(getResources().getDrawable(R.drawable.custom_pending_status));
                break;
            case "1":
                status.setText(intentType.equals("quotation") ? "Confirmed" : "Completed");
                status.setBackground(getResources().getDrawable(R.drawable.custom_confirm_status));
                break;
            default:
                status.setText("Rejected");
                status.setBackground(getResources().getDrawable(R.drawable.custom_rejected_status));
        }
        /*
         * fetch detail
         * */
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchDocumentDetail();
            }
        }, 200);
    }

    private void fetchDocumentDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read_detail", "1"));
                apiDataObjectArrayList.add(new ApiDataObject(intentType.equals("quotation") ? "quotation_id" : "invoice_id", documentHeaderInformation.getDocumentID()));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        intentType.equals("quotation") ? new ApiManager().quotation : new ApiManager().invoice,
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
                        Log.d("haha", "Json: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray(intentType.equals("quotation") ? "quotation_detail" : "invoice_detail");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    documentObjectArrayList.add(new DocumentObject(
                                            jsonArray.getJSONObject(i).getString("detail_id"),
                                            jsonArray.getJSONObject(i).getString("item"),
                                            jsonArray.getJSONObject(i).getString("price"),
                                            jsonArray.getJSONObject(i).getString("quantity"),
                                            jsonArray.getJSONObject(i).getString("sub_total")
                                    ));
                                }
                                /*
                                 * set up customer detail
                                 * */
                                setUpCustomerDetail(jsonObjectLoginResponse.getJSONArray("customer_detail"));
                                /*
                                 * count total price
                                 * */
                                countTotal();
                                /*
                                 * for control print pdf purpose
                                 * */
                                initializeSize = jsonArray.length();
                            }
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                        showProgressBar(false);
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
                    }
                }
                setUpView();
            }
        }).start();
    }

    private void setUpCustomerDetail(JSONArray jsonArray) throws JSONException {
        customerObject = new CustomerObject(
                jsonArray.getJSONObject(0).getString("customer_id"),
                jsonArray.getJSONObject(0).getString("name"),
                jsonArray.getJSONObject(0).getString("address"),
                jsonArray.getJSONObject(0).getString("contact"));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                selectedCustomer.setText(customerObject.getName());
            }
        });

    }

    private void setUpView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (documentObjectArrayList.size() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    no_item_layout.setVisibility(GONE);
                } else {
                    listView.setVisibility(GONE);
                    no_item_layout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : GONE);

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_button:
                Bundle bundle = new Bundle();
                bundle.putString("action", "create");
                openAddOrEditDialog(bundle);
                break;
            case R.id.action_button:
                uploadConfirmation();
                break;
            case R.id.date:
                openDatePicker();
                break;
            case R.id.select_customer_layout:
                openCustomerDialog();
                break;
            case R.id.deposit_button:
                openDepositDialog();
                break;
        }
    }

    /*-------------------------------------------------------------------customer-----------------------------------------------------------------------------*/
    private void openCustomerDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isMainActivity", false);

        DialogFragment dialogFragment = new CustomerDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }


    @Override
    public void selectedItem(CustomerObject object) {
        this.customerObject = object;
        selectedCustomer.setText(object.getName());
    }

    /*-------------------------------------------------------------------date picker-----------------------------------------------------------------------------*/

    private void openDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedDate = String.format("%s", String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth));
                        tvDate.setText(selectedDate);
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void setCurrentTime() {
        selectedDate = (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date());
        tvDate.setText(selectedDate);
    }

    /*---------------------------------------------------------------multiple delete------------------------------------------------------------------*/
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkDeleteItemCount = listView.getCheckedItemCount();
        // Set the  CAB title according to total checkDeleteItem items
        actionMode.setTitle(checkDeleteItemCount + "  Selected");

        // Calls  toggleSelection method from ListViewAdapter Class
        documentDetailAdapter.toggleSelection(position);
        checkDeleteItem = listView.getCheckedItemPositions();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        toolbar.setVisibility(GONE);
        actionMode.getMenuInflater().inflate(R.menu.delete_action_bar, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        this.actionMode = actionMode;
        switch (menuItem.getItemId()) {
            case R.id.select_all:
                final int checkDeleteItemCount = documentObjectArrayList.size();
                documentDetailAdapter.removeSelection();
                for (int i = 0; i < checkDeleteItemCount; i++) {
                    listView.setItemChecked(i, true);
                }
                actionMode.setTitle(checkDeleteItemCount + "  Selected");
                return true;
            case R.id.delete:
                deleteConfirmationDialog(false);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        documentDetailAdapter.removeSelection();
        list.clear();
        toolbar.setVisibility(View.VISIBLE);
    }

    public ArrayList<String> getSelectedItem() {
        for (int i = 0; i < listView.getCount(); i++) {
            if (checkDeleteItem.get(i)) {
                list.add(documentObjectArrayList.get(i).getDocumentDetailID());
            }
        }
        return (ArrayList<String>) list;
    }

    public ActionMode getActionMode() {
        return actionMode;
    }

    /*
     * update list after delete successfully
     */
    private void updateListAfterDelete() {
        for (int i = listView.getCount() - 1; i >= 0; i--) {
            if (documentDetailAdapter.getSelectedIds().get(i)) {
                documentObjectArrayList.remove(i);
            }
        }
        getActionMode().finish();
    }

    /*--------------------------------------------------------add edit document detail to local purpose-------------------------------------------------------------*/

    @Override
    public void returnData(String action, DocumentObject documentObject) {
        if (action.equals("edit"))
            documentObjectArrayList.set(selectedPosition, documentObject);
        else
            documentObjectArrayList.add(documentObject);
        /*
         * update ui purpose
         * */
        documentDetailAdapter.notifyDataSetChanged();
        countTotal();
        setUpView();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedPosition = i;
        Bundle bundle = new Bundle();
        bundle.putSerializable("detail", documentObjectArrayList.get(selectedPosition));
        bundle.putString("action", "edit");
        openAddOrEditDialog(bundle);
    }

    private void openAddOrEditDialog(Bundle bundle) {
        DialogFragment dialogFragment = new AddItemDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    /*--------------------------------------------------------cloud crud--------------------------------------------------------*/
    private void checking() {
        if (documentObjectArrayList.size() > 0 && customerObject != null && !customerObject.getCustomerID().equals("")) {
            showProgressBar(true);
            if (intentAction.equals("edit")) updateDocument();
            else createDocument();
        } else {
            showSnackBar("Please Check you input!");
        }
    }

    /*
     * create
     * */
    private void createDocument() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerObject.getCustomerID()));
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("date", tvDate.getText().toString()));


                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        intentType.equals("quotation") ? new ApiManager().quotation : new ApiManager().invoice,
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
                        Log.d("haha", "Json: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                boolean stop = false;
                                /*
                                 * set id into object
                                 * */
                                documentHeaderInformation = new DocumentObject();
                                documentHeaderInformation.setDocumentID(jsonObjectLoginResponse.getString(intentType.equals("quotation") ? "quotation_id" : "invoice_id"));

                                for (int i = 0; i < documentObjectArrayList.size(); i++) {
                                    if (i == documentObjectArrayList.size() - 1) stop = true;
                                    storeDocumentDetail(i, documentHeaderInformation.getDocumentID(), stop);
                                }
                            }

                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                        showProgressBar(false);
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
                    }
                }
            }
        }).start();
    }

    /*
     * update
     * */
    private void updateDocument() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerObject.getCustomerID()));
                apiDataObjectArrayList.add(new ApiDataObject(intentType.equals("quotation") ? "quotation_id" : "invoice_id", documentHeaderInformation.getDocumentID()));
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        intentType.equals("quotation") ? new ApiManager().quotation : new ApiManager().invoice,
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
                        Log.d("haha", "Json: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                boolean stop = false;
                                for (int i = 0; i < documentObjectArrayList.size(); i++) {
                                    if (i == documentObjectArrayList.size() - 1) stop = true;
                                    storeDocumentDetail(i, documentHeaderInformation.getDocumentID(), stop);
                                }
                            }

                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                        showProgressBar(false);
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
                    }
                }
            }
        }).start();
    }

    /*
     * delete document detail
     * */
    private void deleteDocumentDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("detail_id", TextUtils.join(", ", getSelectedItem())));
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        intentType.equals("quotation") ? new ApiManager().quotation : new ApiManager().invoice,
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
                        Log.d("haha", "Json: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showSnackBar("Delete Successfully!");
                                        updateListAfterDelete();
                                        initializeSize = documentObjectArrayList.size();
                                    }
                                });
                            }

                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                        showProgressBar(false);
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
                    }
                }
            }
        }).start();
    }

    /*
     * delete document
     * */
    private void deleteDocument() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject(intentType.equals("quotation") ? "quotation_id" : "invoice_id", documentHeaderInformation.getDocumentID()));
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        intentType.equals("quotation") ? new ApiManager().quotation : new ApiManager().invoice,
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
                        Log.d("haha", "Json: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initializeSize = documentObjectArrayList.size();
                                        Toast.makeText(DetailActivity.this, "Delete Successfully!", Toast.LENGTH_SHORT).show();
                                        setResult(REQUEST_UPDATE);
                                        onBackPressed();
                                    }
                                });
                            }

                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                        showProgressBar(false);
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
                    }
                }
            }
        }).start();
    }

    /*
     * convert quotaion to invoice
     * */
    private void convertQuotationToInvoice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerObject.getCustomerID()));
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("quotation_id", documentHeaderInformation.getDocumentID()));


                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
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
                        Log.d("haha", "Convert: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initializeSize = documentObjectArrayList.size();
                                        try {
                                            statusDialog("Convert Status", "Convert Successfully!", true);
                                            pushCreateNotification(true, documentHeaderInformation.getDocumentID(), "complete");
                                            /*
                                            * update UI from quotation to invoice
                                            * */
                                            documentHeaderInformation.setDocumentID(jsonObjectLoginResponse.getString("invoice_id"));
                                            intentType = "invoice";
                                            setupActionBar();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } else if (jsonObjectLoginResponse.getString("status").equals("3")) {
                                statusDialog("Convert Status", "This quotation is converted before!", false);
                            }

                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                        showProgressBar(false);
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
                    }
                }
            }
        }).start();
    }

    /*
     * update invoice status
     * */
    private void updateInvoiceStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("update_status", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("invoice_id", documentHeaderInformation.getDocumentID()));


                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
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
                        Log.d("haha", "Convert: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                statusDialog("Update Status", "Update Successfully!", true);
                                pushCreateNotification(false, documentHeaderInformation.getDocumentID(), "complete");
                            } else {
                                statusDialog("Update Status", "Update Failed!", false);
                            }

                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }
                        showProgressBar(false);
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
                    }
                }
            }
        }).start();
    }

    private void storeDocumentDetail(final int position, final String documentID, boolean finished) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject(intentType.equals("quotation") ? "quotation_id" : "invoice_id", documentID));
        apiDataObjectArrayList.add(new ApiDataObject("item", documentObjectArrayList.get(position).getItem()));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", documentObjectArrayList.get(position).getQuantity()));
        apiDataObjectArrayList.add(new ApiDataObject("price", documentObjectArrayList.get(position).getPrice()));
        apiDataObjectArrayList.add(new ApiDataObject("sub_total", documentObjectArrayList.get(position).getSubTotal()));
        apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("date", tvDate.getText().toString()));

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                intentType.equals("quotation") ? new ApiManager().quotation : new ApiManager().invoice,
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
                Log.d("haha", "Json: " + jsonObjectLoginResponse);
                if (jsonObjectLoginResponse != null) {
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        if (finished) {
                            initializeSize = documentObjectArrayList.size();
                            showProgressBar(false);
                            pushCreateNotification(intentType.equals("quotation"), documentID, "create");
                            statusDialog("Upload Status", "Upload Successfully!", true);
                        }
                    }
                } else {
                    CustomToast(getApplicationContext(), "Network Error!");
                    showProgressBar(false);
                    statusDialog("Upload Status", "Upload Failed!", false);
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
            }
        }
    }

    /*----------------------------------------------------------------confirmation dialog-------------------------------------------------------------------*/
    public void requestUploadBeforePrintConfirmation() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("Warning");
        dialog.setContentText("Please Upload before Print!");
        dialog.setConfirmText("Okay");
        dialog.setCancelText("Cancel");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                uploadConfirmation();
                dialog.dismissWithAnimation();
            }
        });
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismissWithAnimation();
            }
        });
        dialog.show();

    }

    public void deleteConfirmationDialog(final boolean deleteWholeDocument) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Request");
        builder.setMessage("Are you sure that you want to do so?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!deleteWholeDocument) deleteDocumentDetail();
                        else deleteDocument();
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void uploadConfirmation() {
        dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("Upload Request");
        dialog.setContentText("Are you sure that you want to do so?");
        dialog.setConfirmText("Upload");
        dialog.setCancelText("Cancel");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                checking();
                dialog.dismissWithAnimation();
            }
        });
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismissWithAnimation();
            }
        });
        dialog.show();
    }

    public void updateInvoiceStatusConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Request");
        builder.setMessage("Are you sure that you want to do so?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Update",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateInvoiceStatus();
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void statusDialog(final String title, final String content, final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final SweetAlertDialog dialog = new SweetAlertDialog(DetailActivity.this, success ? SweetAlertDialog.SUCCESS_TYPE : SweetAlertDialog.ERROR_TYPE);
                dialog.setTitleText(title);
                dialog.setContentText(content);
                if (success) {
                    dialog.setConfirmText("Okay");
                    dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            dialog.dismissWithAnimation();
                        }
                    });
                } else {
                    dialog.setConfirmText(null);
                    dialog.setCancelText("Cancel");
                    dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            dialog.dismissWithAnimation();
                        }
                    });
                }
                dialog.show();
            }
        });
    }

    /*---------------------------------------------------------count total and deposit-----------------------------------------------------------------------------------*/
    private void countTotal() {
        runOnUiThread(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                totalPrice = 0;
                for (int i = 0; i < documentObjectArrayList.size(); i++) {
                    totalPrice = totalPrice + Float.valueOf(documentObjectArrayList.get(i).getSubTotal());
                }
                totalLayout.setVisibility(totalPrice > 0 ? View.VISIBLE : GONE);
                total.setText(String.format("RM%s", totalPrice > 0 ? String.format("%.2f", totalPrice) : "0"));
                /*
                 * count deposit
                 * */
                try {
                    Log.d("haha", "deposit:: " + deposit);
                    deposit = Float.valueOf(documentHeaderInformation.getDeposit());
                } catch (NumberFormatException | NullPointerException e) {
                    deposit = 0;
                }
                setDepositVisibility();
            }
        });
    }

    private void openDepositDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("invoice_id", documentHeaderInformation.getDocumentID());
        bundle.putString("deposit", String.valueOf(deposit));

        DialogFragment dialogFragment = new DepositDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private void setDepositVisibility() {
        if (deposit > 0) {
            depositLayout.setVisibility(View.VISIBLE);
            balanceLayout.setVisibility(View.VISIBLE);
            tvDeposit.setText("RM " + String.format("%.2f", deposit));
            balance.setText("RM " + String.format("%.2f", totalPrice - deposit));
        } else {
            depositLayout.setVisibility(GONE);
            balanceLayout.setVisibility(GONE);
        }
        depositButton.setVisibility(intentType.equals("quotation") ? GONE : View.VISIBLE);
    }

    @Override
    public void updateDeposit(String deposit) {
        this.deposit = Float.valueOf(deposit);
        setDepositVisibility();
    }

    /*-----------------------------------------------------------------------notification-------------------------------------------------------------------------------*/
    private void pushCreateNotification(final boolean isQuotation, final String documentId, final String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("type", type));
                apiDataObjectArrayList.add(new ApiDataObject(isQuotation ? "quotation_id" : "invoice_id", documentId));
                Log.d("haha","hahaha2:"  + isQuotation);

                asyncTaskManager = new AsyncTaskManager(
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
                        jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);
                        showProgressBar(false);
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

    /*------------------------------------------------------------------generate pdf---------------------------------------------------------------------------*/

    @Override
    public void printOption(boolean autoSpacing, boolean artWorkProvided, int customSpacing, String currency, double rate) {
        Log.d("haha", "artwork: " + artWorkProvided);
        this.autoSpacing = autoSpacing;
        this.artWorkProvided = artWorkProvided;
        this.customSpacing = customSpacing;
        this.currency = currency;
        this.rate = rate;

        if (isDeliveryOrder) createDeliveryOrder();
        else createPdf();
    }

    private void createPdfWrapper() throws FileNotFoundException, DocumentException {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel(
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_WRITE_EXTERNAL_PERMISSION);
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_PERMISSION);
            }
        } else {
            openPrintOptionDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                printAction();
            } else {
                // Permission Denied
                Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT)
                        .show();
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage("You need to allow access to Storage")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /*
     * print DO
     * */
    private void createDeliveryOrder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressBar(true);

                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/ThezPrinting");
                if (!docsFolder.exists()) {
                    docsFolder.mkdir();
                    Log.i("Detail Activity", "Created a new directory for PDF");
                }
                pdfFile = new File(docsFolder.getAbsolutePath(), setDeliveryOrderPlaceHolder(documentHeaderInformation.getDocumentNo()) + ".pdf");
                OutputStream output = null;
                try {
                    output = new FileOutputStream(pdfFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Document document = new Document();
                try {
                    String[] DoHeader = {"S.No", "Description", "Unit", "Quantity"};
                    //use to set background color
                    BaseColor white = getRGBColor("#ffffff");

                    PdfWriter.getInstance(document, output);
                    document.open();
                    document.add(new Paragraph(""));


                    try {
                        PdfPCell cell;
                        Image bgImage;
                        //set drawable in cell
                        Drawable myImage = getApplicationContext().getResources().getDrawable(R.drawable.thezlogo);
                        Bitmap bitmap = ((BitmapDrawable) myImage).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        byte[] bitmapdata = stream.toByteArray();
                        try {
                            /*-----------------------------------------------------header------------------------------------------------------------*/
                            //create table
                            PdfPTable pt = new PdfPTable(3);
                            pt.setWidthPercentage(100);
                            float[] fl = new float[]{40, 20, 40};
                            pt.setWidths(fl);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            bgImage = Image.getInstance(bitmapdata);
                            bgImage.scaleToFit(100f, 80f);
                            cell.addElement(bgImage);
                            cell.setBackgroundColor(white);
                            cell.setColspan(1);
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(new Phrase(""));
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(1);
                            cell.setBackgroundColor(white);
                            cell.setBorder(Rectangle.NO_BORDER);
                            Paragraph documentHeader = new Paragraph();
                            documentHeader.setAlignment(Element.ALIGN_RIGHT);
                            documentHeader.setFont(largeFont);
                            documentHeader.add("Delivery Order");
                            cell.addElement(documentHeader);

                            Phrase date = new Phrase("Date: ", boldFont);
                            date.add(new Chunk(createdDate != null ? createdDate : tvDate.getText().toString(), normalFont));
                            cell.addElement(date);

                            Phrase documentId = new Phrase("ID: ", boldFont);
                            documentId.add(new Chunk(setDeliveryOrderPlaceHolder(documentHeaderInformation.getDocumentNo()), normalFont));
                            cell.addElement(documentId);

                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(1);
                            cell.setBorder(Rectangle.NO_BORDER);
                            Paragraph customer = new Paragraph();
                            customer.setAlignment(Element.ALIGN_LEFT);
                            customer.add("Bill To:");
                            customer.setFont(boldFont);
                            cell.addElement(customer);

                            customer = new Paragraph();
                            customer.setAlignment(Element.ALIGN_LEFT);

                            customer.setFont(isMandarin(customerObject.getName()) ? mandarin : boldFont);
                            customer.setLeading(12f);
                            customer.add(customerObject.getName());

                            customer.setFont(normalFont);
                            customer.add(!customerObject.getAddress().equals("") ? "\n" + customerObject.getAddress() : "");

                            customer.setFont(boldFont);
                            customer.add(!customerObject.getContact().equals("") ? "\nContact Information" : "");

                            customer.setFont(normalFont);
                            customer.add(!customerObject.getContact().equals("") ? "\n" + customerObject.getContact() : "");

                            cell.addElement(customer);

                            cell.setBackgroundColor(white);
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(new Phrase(""));
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(1);
                            cell.setBorder(Rectangle.NO_BORDER);
                            Paragraph companyName = new Paragraph();
                            companyName.setFont(boldFont);
                            companyName.add("THEz Printing ＆ Design ");
                            companyName.setFont(smallBoldFont);
                            companyName.add("(002923140-P)");
                            cell.addElement(companyName);

                            Paragraph companyDetail = new Paragraph();
                            companyDetail.setFont(normalFont);
                            companyDetail.setLeading(12f);
                            companyDetail.add("39, Jalan Tasek 51");
                            companyDetail.add("\nBandar Seri Alam");
                            companyDetail.add("\nJohor Bahru, Johor 81750");
                            companyDetail.add("\nMalaysia");
                            cell.addElement(companyDetail);

                            cell.addElement(new Phrase("Contact Information ", boldFont));
                            cell.addElement(new Phrase("Mobile: 01110640416", normalFont));
                            cell.addElement(new Phrase("Email: thezprinting0105@gmail.com", normalFont));
                            cell.setBackgroundColor(white);
                            pt.addCell(cell);


                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(new Paragraph("\n"));
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(pt);

                            /*-----------------------------------------------------content------------------------------------------------------------*/

                            PdfPTable table = new PdfPTable(4);
                            table.setWidthPercentage(100);
                            float[] columnWidth = new float[]{10, 50, 20, 20};
                            table.setWidths(columnWidth);
                            table.addCell(cell);

                            for (int i = 0; i < DoHeader.length; i++) {
                                cell = new PdfPCell(new Phrase(DoHeader[i], headerFont));
                                if (i == 1 || i == 0)
                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                else cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorder(Rectangle.NO_BORDER);
                                cell.setBackgroundColor(white);
                                table.addCell(cell);
                            }

                            //line
                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.BOX);
                            table.addCell(cell);

                            for (int i = 0; i < documentObjectArrayList.size(); i++) {
                                for (int j = 0; j < 4; j++) {
                                    cell = new PdfPCell();
                                    cell.setColspan(1);
                                    cell.setBorder(Rectangle.NO_BORDER);
                                    cell.addElement(returnDoColumnData(i, j));
                                    table.addCell(cell);
                                }
                            }

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(countSpacing(cell, 8));

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.BOX);
                            table.addCell(cell);
                            /*-----------------------------------------------------total and tax-----------------------------------------------------*/

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setColspan(4);
                            cell.addElement(new Paragraph("1. I confirm that all goods received are in good order and condition.", normalFont));
                            table.addCell(cell);

                            /*--------------------------------------------------footer------------------------------------------------------------------*/
                            PdfPTable footerTable = new PdfPTable(1);
                            footerTable.setWidthPercentage(100);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(table);

                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            /*
                             * sign line
                             * */
                            PdfPTable signTable = new PdfPTable(3);
                            signTable.setWidthPercentage(100);
                            float[] signTableWidth = new float[]{35, 32, 32};
                            signTable.setWidths(signTableWidth);

                            PdfPCell signTableCell = new PdfPCell();
                            signTableCell.setColspan(1);
                            signTable.addCell(signTableCell);

                            PdfPCell blankCell = new PdfPCell();
                            blankCell.setBorder(Rectangle.NO_BORDER);
                            blankCell.setColspan(2);
                            signTable.addCell(blankCell);

                            cell.addElement(signTable);

                            /*
                             * sign by who
                             * */
                            Paragraph sign = new Paragraph();
                            sign.setFont(boldFont);
                            sign.add("Receiver's Signature / Company Stamp");
                            cell.addElement(sign);


                            cell.addElement(new Paragraph("\n"));

                            Paragraph footer2 = new Paragraph();
                            footer2.setFont(boldFont);
                            footer2.setAlignment(Element.ALIGN_CENTER);
                            footer2.add("Thank You For Your Business!");
                            cell.addElement(footer2);

                            footerTable.addCell(cell);
                            document.add(footerTable);

                        } catch (IOException e) {
                            Log.e("PDFCreator", "ioException:" + e);
                        } catch (NullPointerException e) {
                            Log.e("Null", "null exception:" + e);
                            CustomToast(getApplicationContext(), "Something is null!");
                        } finally {
                            document.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                showProgressBar(false);
                previewPdf();
            }
        }).start();
    }

    private String setDeliveryOrderPlaceHolder(String do_id) {
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for (int i = do_idBuilder.length(); i < 5; i++) {
            do_idBuilder.insert(0, "0");
        }
        return ("#DO") + do_idBuilder.toString();
    }

    private Paragraph returnDoColumnData(int i, int j) {
        Paragraph itemDetail = new Paragraph();
        itemDetail.setLeading(12f);
        itemDetail.setAlignment(Element.ALIGN_RIGHT);
        itemDetail.setFont(normalFont);
        switch (j) {
            case 0:
                itemDetail.setAlignment(Element.ALIGN_LEFT);
                itemDetail.add(String.valueOf(i + 1));
                break;
            case 1:
                itemDetail.add(documentObjectArrayList.get(i).getItem());
                itemDetail.setAlignment(Element.ALIGN_LEFT);
                break;
            case 2:
                itemDetail.setFont(smallFont);
                itemDetail.add("UNIT");
                break;
            default:
                itemDetail.add(documentObjectArrayList.get(i).getQuantity());
        }
        return itemDetail;
    }

    /*
     * print invoice or quotation
     * */
    private void createPdf() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressBar(true);

                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/ThezPrinting");
                if (!docsFolder.exists()) {
                    docsFolder.mkdir();
                    Log.i("Detail Activity", "Created a new directory for PDF");
                }
                pdfFile = new File(docsFolder.getAbsolutePath(), setQuotationPlaceHolder(documentHeaderInformation.getDocumentNo()) + ".pdf");
                OutputStream output = null;
                try {
                    output = new FileOutputStream(pdfFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Document document = new Document();
                try {
                    String[] productHeader = {"Item", "Quantity", "Price(" + currency + ")", "Amount(" + currency + ")"};
                    //use to set background color
                    BaseColor white = getRGBColor("#ffffff");

                    PdfWriter.getInstance(document, output);
                    document.open();
                    document.add(new Paragraph(""));


                    try {
                        PdfPCell cell;
                        Image bgImage;
                        //set drawable in cell
                        Drawable myImage = getApplicationContext().getResources().getDrawable(R.drawable.thezlogo);
                        Bitmap bitmap = ((BitmapDrawable) myImage).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        byte[] bitmapdata = stream.toByteArray();
                        try {
                            /*-----------------------------------------------------header------------------------------------------------------------*/
                            //create table
                            PdfPTable pt = new PdfPTable(3);
                            pt.setWidthPercentage(100);
                            float[] fl = new float[]{40, 20, 40};
                            pt.setWidths(fl);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            bgImage = Image.getInstance(bitmapdata);
                            bgImage.scaleToFit(100f, 80f);
                            cell.addElement(bgImage);
                            cell.setBackgroundColor(white);
                            cell.setColspan(1);
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(new Phrase(""));
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(1);
                            cell.setBackgroundColor(white);
                            cell.setBorder(Rectangle.NO_BORDER);
                            Paragraph documentHeader = new Paragraph();
                            documentHeader.setAlignment(Element.ALIGN_RIGHT);
                            documentHeader.setFont(largeFont);
                            documentHeader.add(intentType.equals("quotation") ? "Quotation" : "Invoice");
                            cell.addElement(documentHeader);

                            Phrase date = new Phrase("Date: ", boldFont);
                            date.add(new Chunk(createdDate != null ? createdDate : tvDate.getText().toString(), normalFont));
                            cell.addElement(date);

                            Phrase documentId = new Phrase("ID: ", boldFont);
                            documentId.add(new Chunk(setQuotationPlaceHolder(documentHeaderInformation.getDocumentNo()), normalFont));
                            cell.addElement(documentId);

                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(1);
                            cell.setBorder(Rectangle.NO_BORDER);
                            Paragraph customer = new Paragraph();
                            customer.setAlignment(Element.ALIGN_LEFT);
                            customer.add("Bill To:");
                            customer.setFont(boldFont);
                            cell.addElement(customer);

                            customer = new Paragraph();
                            customer.setAlignment(Element.ALIGN_LEFT);

                            customer.setFont(isMandarin(customerObject.getName()) ? mandarin : boldFont);
                            customer.setLeading(12f);
                            customer.add(customerObject.getName());

                            customer.setFont(normalFont);
                            customer.add(!customerObject.getAddress().equals("") ? "\n" + customerObject.getAddress() : "");

                            customer.setFont(boldFont);
                            customer.add(!customerObject.getContact().equals("") ? "\nContact Information" : "");

                            customer.setFont(normalFont);
                            customer.add(!customerObject.getContact().equals("") ? "\n" + customerObject.getContact() : "");

                            cell.addElement(customer);

                            cell.setBackgroundColor(white);
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(new Phrase(""));
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(1);
                            cell.setBorder(Rectangle.NO_BORDER);
                            Paragraph companyName = new Paragraph();
                            companyName.setFont(boldFont);
                            companyName.add("THEz Printing ＆ Design ");
                            companyName.setFont(smallBoldFont);
                            companyName.add("(002923140-P)");
                            cell.addElement(companyName);

                            Paragraph companyDetail = new Paragraph();
                            companyDetail.setFont(normalFont);
                            companyDetail.setLeading(12f);
                            companyDetail.add("39, Jalan Tasek 51");
                            companyDetail.add("\nBandar Seri Alam");
                            companyDetail.add("\nJohor Bahru, Johor 81750");
                            companyDetail.add("\nMalaysia");
                            cell.addElement(companyDetail);

                            cell.addElement(new Phrase("Contact Information ", boldFont));
                            cell.addElement(new Phrase("Mobile: 01110640416", normalFont));
                            cell.addElement(new Phrase("Email: thezprinting0105@gmail.com", normalFont));
                            cell.setBackgroundColor(white);
                            pt.addCell(cell);


                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(new Paragraph("\n"));
                            pt.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(pt);

                            /*-----------------------------------------------------content------------------------------------------------------------*/

                            PdfPTable table = new PdfPTable(4);
                            table.setWidthPercentage(100);
                            float[] columnWidth = new float[]{40, 20, 20, 20};
                            table.setWidths(columnWidth);
                            table.addCell(cell);

                            for (int i = 0; i < productHeader.length; i++) {
                                cell = new PdfPCell(new Phrase(productHeader[i], headerFont));
                                if (i == 0) cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                else cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorder(Rectangle.NO_BORDER);
                                cell.setBackgroundColor(white);
                                table.addCell(cell);
                            }

                            //line
                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.BOX);
                            table.addCell(cell);

                            for (int i = 0; i < documentObjectArrayList.size(); i++) {
                                for (int j = 0; j < 4; j++) {
                                    cell = new PdfPCell();
                                    cell.setColspan(1);
                                    cell.setBorder(Rectangle.NO_BORDER);
                                    cell.addElement(returnColumnData(i, j));
                                    table.addCell(cell);
                                }
                            }

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(countSpacing(cell, 8));

                            cell = new PdfPCell();
                            cell.setColspan(4);
                            cell.setBorder(Rectangle.BOX);
                            table.addCell(cell);
                            /*-----------------------------------------------------total and tax-----------------------------------------------------*/

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setColspan(2);
                            cell.addElement(new Paragraph("1.Validity of this proposal: ONE MONTH from date of quotation", smallFont));
                            cell.addElement(new Paragraph("2. Prices is subject to 10% SST (if applicable)", smallFont));
                            cell.addElement(new Paragraph("3. 50% Deposit payment - Upon confirmation", smallFont));
                            cell.addElement(new Paragraph("4. Balance of payment – to be paid within 14 days after the trade have done.", smallFont));
                            cell.addElement(new Paragraph("5. Overall process may takes up to 10 - 15 working days", boldFont));
                            table.addCell(cell);

                            cell = new PdfPCell();
                            cell.setColspan(2);
                            cell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(totalAndTaxTable());

                            /*--------------------------------------------------footer------------------------------------------------------------------*/
                            PdfPTable footerTable = new PdfPTable(1);
                            footerTable.setWidthPercentage(100);

                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.addElement(table);

                            cell.addElement(new Paragraph("\n"));
                            /*
                             * bank detail
                             * */
                            Paragraph bankDetail = new Paragraph();
                            bankDetail.setLeading(12f);
                            bankDetail.setFont(boldFont);
                            bankDetail.setAlignment(Element.ALIGN_LEFT);
                            bankDetail.add("\nBank Details:");
                            bankDetail.add("\nTHEZPRINTING&DESIGN");
                            bankDetail.add("\nOCBC BANK MALAYSIA");
                            bankDetail.add("\n791 102 4465");
                            cell.addElement(bankDetail);

                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            cell.addElement(new Paragraph("\n"));
                            /*
                             * sign line
                             * */
                            PdfPTable signTable = new PdfPTable(3);
                            signTable.setWidthPercentage(100);
                            float[] signTableWidth = new float[]{35, 32, 32};
                            signTable.setWidths(signTableWidth);

                            PdfPCell signTableCell = new PdfPCell();
                            signTableCell.setColspan(1);
                            signTable.addCell(signTableCell);

                            PdfPCell blankCell = new PdfPCell();
                            blankCell.setBorder(Rectangle.NO_BORDER);
                            blankCell.setColspan(2);
                            signTable.addCell(blankCell);

                            cell.addElement(signTable);

                            /*
                             * sign by who
                             * */
                            Paragraph sign = new Paragraph();
                            sign.setFont(boldFont);
                            sign.add("Sales Executive THEZ PRINTING & DESIGN");
                            cell.addElement(sign);


                            cell.addElement(new Paragraph("\n"));
                            ;
                            Paragraph footer1 = new Paragraph();
                            footer1.setFont(boldFont);
                            footer1.setAlignment(Element.ALIGN_CENTER);
                            footer1.add("If you have any question concerning this " + (intentType.equals("quotation") ? "quotation" : "invoice") + " please kindly contact us");
                            cell.addElement(footer1);


                            Paragraph footer2 = new Paragraph();
                            footer2.setFont(boldFont);
                            footer2.setAlignment(Element.ALIGN_CENTER);
                            footer2.add("Thank You For Your Business!");
                            cell.addElement(footer2);

                            footerTable.addCell(cell);
                            document.add(footerTable);

                        } catch (IOException e) {
                            Log.e("PDFCreator", "ioException:" + e);
                        } catch (NullPointerException e) {
                            Log.e("Null", "null exception:" + e);
                            CustomToast(getApplicationContext(), "Something is null!");
                        } finally {
                            document.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                showProgressBar(false);
                previewPdf();
            }
        }).start();
    }

    /*
     * invoice and quotation column data
     * */
    private Paragraph returnColumnData(int i, int j) {
        Paragraph itemDetail = new Paragraph();
        itemDetail.setLeading(12f);
        itemDetail.setAlignment(Element.ALIGN_RIGHT);
        itemDetail.setFont(normalFont);
        switch (j) {
            case 0:
                itemDetail.add(documentObjectArrayList.get(i).getItem());
                itemDetail.setAlignment(Element.ALIGN_LEFT);
                break;
            case 1:
                itemDetail.add(documentObjectArrayList.get(i).getQuantity());
                break;
            case 2:
                double price = Double.valueOf(documentObjectArrayList.get(i).getPrice()) / rate;
                itemDetail.add(decimalControl(price));
                break;
            default:
                double subTotal = Double.valueOf(documentObjectArrayList.get(i).getSubTotal()) / rate;
                itemDetail.add(String.format("%.2f", subTotal));
        }
        return itemDetail;
    }

    @SuppressLint("DefaultLocale")
    private String decimalControl(double value) {
        return BigDecimal.valueOf(value).scale() > 2 ? String.format("%.3f", value) : String.format("%.2f", value);
    }

    /*
     * count spacing
     * */
    private PdfPCell countSpacing(PdfPCell cell, int size) {
        if (autoSpacing) {
            if (size > documentObjectArrayList.size()) {
                for (int i = (size - documentObjectArrayList.size()); i > 0; i--) {
                    if (artWorkProvided && i == 1) {
                        cell.addElement(new Paragraph("Artwork provided by client.", boldFont));
                        return cell;
                    }
                    cell.addElement(new Paragraph("\n"));
                }
            } else {
                for (int i = 2; i > 0; i--) {
                    if (artWorkProvided && i == 1) {
                        cell.addElement(new Paragraph("Artwork provided by client.", boldFont));
                        return cell;
                    }
                    cell.addElement(new Paragraph("\n"));
                }
            }
        } else {
            for (int i = 0; i < customSpacing; i++) {
                if (artWorkProvided && i == customSpacing - 1) {
                    cell.addElement(new Paragraph("Artwork provided by client.", boldFont));
                    return cell;
                }
                cell.addElement(new Paragraph("\n"));
            }
        }
        return cell;
    }

    /*
     * invoice and quotation total
     * */
    @SuppressLint("DefaultLocale")
    private PdfPCell totalAndTaxTable() throws DocumentException {
        PdfPCell parentCell = new PdfPCell();
        parentCell.setColspan(2);
        parentCell.setBorder(Rectangle.NO_BORDER);

        PdfPTable totalAndTaxTable = new PdfPTable(2);
        totalAndTaxTable.setWidthPercentage(100);
        float[] fl = new float[]{50, 50};
        totalAndTaxTable.setWidths(fl);

        for (int i = 0; deposit == 0 ? i < 3 : i < 5; i++) {
            PdfPCell cell = new PdfPCell();

            cell.setColspan(1);
            cell.setBorder(Rectangle.NO_BORDER);
            Paragraph labelSubTotal = new Paragraph();
            labelSubTotal.setFont(headerFont);
            labelSubTotal.setAlignment(Element.ALIGN_LEFT);
            switch (i) {
                case 0:
                    labelSubTotal.add("Subtotal");
                    break;
                case 1:
                    labelSubTotal.add("Tax");
                    break;
                case 2:
                    labelSubTotal.add("Total");
                    break;
                case 3:
                    labelSubTotal.add("Deposit ");
                    break;
                case 4:
                    labelSubTotal.add("Balance Due");
                    break;
            }
            cell.addElement(labelSubTotal);
            totalAndTaxTable.addCell(cell);

            cell = new PdfPCell();
            cell.setColspan(1);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            Paragraph subTotal = new Paragraph();
            subTotal.setFont(normalFont);
            subTotal.setAlignment(Element.ALIGN_RIGHT);
            if (deposit == 0) {
                subTotal.add(i != 1 ? String.format("%.2f", totalPrice / rate) : "0.00");
            } else {
                if (i == 3) {
                    subTotal.add(String.format("%.2f", deposit / rate));
                } else if (i == 4) {
                    subTotal.add(String.format("%.2f", totalPrice / rate - deposit / rate));
                } else {
                    subTotal.add(i != 1 ? String.format("%.2f", totalPrice / rate) : "0.00");
                }
            }

            cell.addElement(subTotal);
            totalAndTaxTable.addCell(cell);
        }
        parentCell.addElement(totalAndTaxTable);
        return parentCell;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void previewPdf() {
        try {
            PackageManager packageManager = getPackageManager();
            Intent testIntent = new Intent(Intent.ACTION_VIEW);
            testIntent.setType("application/pdf");
            List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0) {
                Uri uri = FileProvider.getUriForFile(this, getResources().getString(R.string.file_provider_authority), pdfFile);
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");

                startActivity(intent);
            } else {
                CustomToast(getApplicationContext(), "Download a PDF Viewer to see the generated PDF");
            }
        } catch (FileUriExposedException e) {
            CustomToast(getApplicationContext(), "Unable to preview your PDF");
        }
    }

    private void setUpMandarin() {
        BaseFont mandarinFile = null;
        try {
            mandarinFile = BaseFont.createFont("assets/fonts/simsun.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Not Found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Not Found", Toast.LENGTH_SHORT).show();
        }
        mandarin = new Font(mandarinFile, 10, Font.BOLD);
    }

    public static boolean isMandarin(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block) ||
                    Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block) ||
                    Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)) {
                return true;
            }
        }
        return false;
    }

    /*------------------------------------------------------------------others---------------------------------------------------------------------------*/
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

    @Override
    public void onBackPressed() {
        if (documentObjectArrayList.size() != initializeSize) {
            leaveConfirmation();
            return;
        }
        super.onBackPressed();
    }

    public void leaveConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Don't forget to upload your item before leave!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Upload Now!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        uploadConfirmation();
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(
                "Leave Anyway",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        initializeSize = documentObjectArrayList.size();
                        onBackPressed();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
