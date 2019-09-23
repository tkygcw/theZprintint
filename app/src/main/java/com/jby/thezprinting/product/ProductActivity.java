package com.jby.thezprinting.product;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.thezprinting.R;
import com.jby.thezprinting.adapter.ProductAdapter;
import com.jby.thezprinting.adapter.SupplierPriceListAdapter;
import com.jby.thezprinting.dialog.CustomerDialog;
import com.jby.thezprinting.dialog.SupplierDialog;
import com.jby.thezprinting.object.ProductObject;
import com.jby.thezprinting.object.SupplierObject;
import com.jby.thezprinting.others.ExpandableHeightListView;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;

public class ProductActivity extends AppCompatActivity implements SupplierPriceListAdapter.SupplierAdapterCallBack, View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, AddSupplierPriceDialog.AddSupplierPriceDialogCallBack,
        AdapterView.OnItemClickListener, AbsListView.OnScrollListener, ProductAdapter.ProductAdapterCallBack, ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnGroupCollapseListener {
    private Toolbar toolbar;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //progress bar
    public ProgressBar progressBar;

    private LinearLayout displayProductLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton addButton;

    private ExpandableListView productList;
    private ArrayList<ProductObject> productObjectArrayList, productChildObjectArrayList;
    private ProductAdapter productAdapter;

    private String query = "", sorting = "";
    /*-----------------------------------------------------add product purpose-----------------------------------------------------------------------------*/
    private EditText etProduct, etPrice, etUnit, etCategory;
    private Button addSupplierButton, saveButton, cancelButton;
    private ScrollView addSupplierLayout;

    private ExpandableHeightListView supplierList;
    private ArrayList<SupplierObject> supplierPriceArrayList;
    private SupplierPriceListAdapter supplierPriceListAdapter;
    private LinearLayout supplierPriceListLabel;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    /*
     * supplier price list
     * */
    private String productId = "";
    /*
     * delete purpose
     * */
    private List<String> deleteId = new ArrayList<>();
    /*
     * update purpose
     * */
    private boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        toolbar = findViewById(R.id.toolbar);
        //not found layout
        notFoundLayout = findViewById(R.id.not_found_layout);
        notFoundIcon = findViewById(R.id.not_found_layout_icon);
        notFoundLabel = findViewById(R.id.not_found_layout_label);
        //progress bar
        progressBar = findViewById(R.id.progress_bar);
        /*
         * display layout
         * */
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        displayProductLayout = findViewById(R.id.display_product_layout);
        addButton = findViewById(R.id.add_button);

        productList = findViewById(R.id.product_list);
        productObjectArrayList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productObjectArrayList, this);

        /*
         * create product layout
         */
        etProduct = findViewById(R.id.product);
        etPrice = findViewById(R.id.price);
        etUnit = findViewById(R.id.unit);
        etCategory = findViewById(R.id.category);

        addSupplierButton = findViewById(R.id.add_supplier_button);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.back_button);

        addSupplierLayout = findViewById(R.id.add_supplier_layout);

        supplierPriceListLabel = findViewById(R.id.label_supplier_price_list);
        supplierList = findViewById(R.id.supplier_list);
        supplierPriceArrayList = new ArrayList<>();
        supplierPriceListAdapter = new SupplierPriceListAdapter(this, supplierPriceArrayList, this);
    }

    private void objectSetting() {
        setupActionBar("Product");
        setupNotFoundLayout();
        /*
         * display layout
         * */
        swipeRefreshLayout.setOnRefreshListener(this);
        addButton.setOnClickListener(this);
        productList.setAdapter(productAdapter);
        productList.setOnScrollListener(this);
        productList.setOnGroupClickListener(this);
        productList.setOnGroupCollapseListener(this);

        /*
         * create product layout
         */
        addSupplierButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        supplierList.setAdapter(supplierPriceListAdapter);
        supplierList.setExpanded(true);
        supplierList.setOnItemClickListener(this);

        showProgressBar(true);
        showDisplayLayout(true);
        readProduct();
    }

    private void showDisplayLayout(boolean show) {
        displayProductLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        addSupplierLayout.setVisibility(!show ? View.VISIBLE : View.GONE);
        addButton.setVisibility(show ? View.VISIBLE : View.GONE);
        notFoundLayout.setVisibility(show && productList.getChildCount() <= 0 ? View.VISIBLE : View.GONE);

        swipeRefreshLayout.setEnabled(show);
    }

    private void setupActionBar(String title) {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.product));
        notFoundLabel.setText("No Item is Found!");
    }

    public void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    //---------------------------------------------------------------------product list read purpose--------------------------------------------------------------------------
    private void readProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("query", query));
                apiDataObjectArrayList.add(new ApiDataObject("sorting", sorting));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("product_list");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                //parent item
                                                productObjectArrayList.add(new ProductObject(jsonArray.getJSONObject(i).getString("category")));
                                                //child item
                                                productObjectArrayList.get(i).setChildProductObjectArrayList(separateProduct(jsonArray.getJSONObject(i).getString("products")));
                                                expandGroup(i);
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
                notifyDataSetChanged();
                setUpView();
            }
        }).start();
    }

    private ArrayList<ProductObject> separateProduct(String products) {
        try {
            productChildObjectArrayList = new ArrayList<>();
            String[] product = products.split(";");

            for (String s : product) {
                String[] productDetail = s.split(" ,");
                productChildObjectArrayList.add(new ProductObject(
                        productDetail[0],
                        productDetail[1],
                        productDetail[2],
                        productDetail[3]));
            }
        } catch (NullPointerException e) {
            CustomToast(getApplicationContext(), "No Item Found!");
        } catch (IndexOutOfBoundsException e) {
            CustomToast(getApplicationContext(), "Something went wrong!");
        }
        return productChildObjectArrayList;
    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (displayProductLayout.getVisibility() == View.VISIBLE)
                    productAdapter.notifyDataSetChanged();
                else {
                    supplierPriceListLabel.setVisibility(supplierPriceArrayList.size() > 0 ? View.VISIBLE : View.GONE);
                    supplierPriceListAdapter.notifyDataSetChanged();
                }
                showProgressBar(false);
            }
        });
    }

    private void setUpView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notFoundLayout.setVisibility(productObjectArrayList.size() == 0 ? View.VISIBLE : View.GONE);
                productList.setVisibility(productObjectArrayList.size() == 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void expandGroup(final int position) {
        productList.expandGroup(position);
    }

    //---------------------------------------------------------------------supplier price list read purpose--------------------------------------------------------------------------
    private void readSupplierPriceList() {
        showProgressBar(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read_supplier_price_list", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productId));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("supplier_price_list");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    supplierPriceArrayList.add(new SupplierObject(
                                            jsonArray.getJSONObject(i).getString("supplier_price_list_id"),
                                            jsonArray.getJSONObject(i).getString("supplier_id"),
                                            jsonArray.getJSONObject(i).getString("name"),
                                            jsonArray.getJSONObject(i).getString("address"),
                                            jsonArray.getJSONObject(i).getString("price"),
                                            jsonArray.getJSONObject(i).getString("unit"),
                                            false,
                                            false
                                    ));
                                }
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
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                notifyDataSetChanged();
            }
        }).start();
    }

    //----------------------------------------------------------------------cloud create , update, delete--------------------------------------------------------------------------------

    /**
     * create
     */
    private void checkingInput() {
        String name = etProduct.getText().toString();
        String price = etPrice.getText().toString();
        String unit = etUnit.getText().toString();
        String category = etCategory.getText().toString();
        if (!name.equals("") && !price.equals("") && !unit.equals("") && !category.equals("")) {
            showProgressBar(true);
            if (!isUpdate) storeProduct();
            else updateProduct();

        } else Toast.makeText(this, "Every field above is required!", Toast.LENGTH_SHORT).show();
    }

    private void storeProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("name", etProduct.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("price", etPrice.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("unit", etUnit.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("category", etCategory.getText().toString()));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                                productId = jsonObjectLoginResponse.getString("product_id");
                                loopSupplierListTobeStore(true);
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
                    }
                }
            }
        }).start();
    }

    private void loopSupplierListTobeStore(final boolean isCreate) {
        if (supplierPriceArrayList.size() > 0) {
            for (int i = 0; i < supplierPriceArrayList.size(); i++) {
                /**
                 * create new
                 */
                if (supplierPriceArrayList.get(i).isNewAdded()) {
                    storeSupplierPriceList(supplierPriceArrayList.get(i), isCreate);
                }
                /**
                 * update
                 */
                else if (supplierPriceArrayList.get(i).isUpdated()) {
                    updateSupplierPriceList(supplierPriceArrayList.get(i));
                }

                if (i == supplierPriceArrayList.size() - 1) {
                    showProgressBar(false);
                    CustomToast(getApplicationContext(), "Successfully");
                }


            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isCreate) reset();
                    Toast.makeText(ProductActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
                    showProgressBar(false);
                }
            });
        }
    }

    private void storeSupplierPriceList(final SupplierObject supplierObject, final boolean isCreate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("supplier_id", supplierObject.getSupplier_id()));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productId));
                apiDataObjectArrayList.add(new ApiDataObject("price", supplierObject.getPrice()));
                apiDataObjectArrayList.add(new ApiDataObject("unit", supplierObject.getUnit()));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                                if (isCreate) reset();
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
                    }
                }
            }
        }).start();
    }

    /**
     * update
     */
    private void updateProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productId));
                apiDataObjectArrayList.add(new ApiDataObject("name", etProduct.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("price", etPrice.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("unit", etUnit.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("category", etCategory.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("supplier_price_list_ids", TextUtils.join(", ", deleteId)));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                                loopSupplierListTobeStore(false);
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
                    }
                }
            }
        }).start();
    }

    private void updateSupplierPriceList(final SupplierObject supplierObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("unit", supplierObject.getUnit()));
                apiDataObjectArrayList.add(new ApiDataObject("price", supplierObject.getPrice()));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productId));
                apiDataObjectArrayList.add(new ApiDataObject("supplier_id", supplierObject.getSupplier_id()));
                apiDataObjectArrayList.add(new ApiDataObject("supplier_price_list_id", supplierObject.getSupplier_price_list_id()));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                    }
                }
            }
        }).start();
    }

    /*
     * delete
     * */
    private void deleteProduct(final String productId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productId));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                                CustomToast(getApplicationContext(), "Delete Successfully!");
                                refresh();
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
                    }
                }
            }
        }).start();
    }

    //-------------------------------------------------local create purpose-----------------------------------------------------------------------------
    @Override
    public void addSupplierPrice(SupplierObject supplierObject) {
        if (supplierPriceArrayList.size() > 0) {
            for (int i = 0; i < supplierPriceArrayList.size(); i++) {
                if (supplierObject.getSupplier_id().equals(supplierPriceArrayList.get(i).getSupplier_id())) {
                    supplierPriceArrayList.remove(i);
                    supplierPriceArrayList.add(supplierObject);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
        supplierPriceArrayList.add(supplierObject);
        notifyDataSetChanged();
    }

    //-------------------------------------------------local update purpose-----------------------------------------------------------------------------
    /*
     * update purpose
     * */
    public void updateProduct(ProductObject parentObject, ProductObject childObject) {
        isUpdate = true;
        showDisplayLayout(false);

        etProduct.append(childObject.getName());
        etPrice.append(childObject.getPrice());
        etUnit.append(childObject.getUnit());
        etCategory.append(parentObject.getCategory());

        showProgressBar(true);
        productId = childObject.getProduct_id();
        readSupplierPriceList();
    }

    @Override
    public void editSupplierPriceList(SupplierObject object) {
        openAddSupplierPriceDialog(true, object);
    }

    /*
     * delete purpose
     * */
    public void deleteSupplierPriceList(int position) {
        isUpdate = true;
        deleteId.add(supplierPriceArrayList.get(position).getSupplier_price_list_id());
        supplierPriceArrayList.remove(position);
        notifyDataSetChanged();
    }

    public void deleteConfirmation(final boolean deleteProduct, final int position, final String delete_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Request");
        builder.setMessage("Are you sure that you want to do so?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!deleteProduct)
                            deleteSupplierPriceList(position);
                        else
                            deleteProduct(delete_id);
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

    //-------------------------------------------------common listener-----------------------------------------------------------------------------
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                checkingInput();
                break;
            case R.id.add_button:
                isUpdate = false;
                showDisplayLayout(false);
                break;
            case R.id.back_button:
                reset();
                showDisplayLayout(true);
                break;
            case R.id.add_supplier_button:
                openAddSupplierPriceDialog(false, null);
                break;
        }
    }

    private void openAddSupplierPriceDialog(boolean isUpdate, SupplierObject supplierObject) {
        DialogFragment dialogFragment = new AddSupplierPriceDialog();
        if (isUpdate) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("object", supplierObject);
            dialogFragment.setArguments(bundle);
        }
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private void refresh() {
        reset();
        readProduct();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void reset() {
        if (displayProductLayout.getVisibility() == View.VISIBLE) {
            productObjectArrayList.clear();
            notifyDataSetChanged();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etProduct.setText("");
                    etPrice.setText("");
                    etUnit.setText("");
                    etCategory.setText("");
                    supplierPriceArrayList.clear();
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String s) {
        showProgressBar(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reset();
                query = s;
                readProduct();
            }
        }, 500);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_action_menu, menu);

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
            case R.id.customer:
                openCustomerDialog();
                return true;
            case R.id.supplier:
                openSupplierDialog();
                return true;
            case R.id.delete:
                deleteConfirmation(true, -1, productId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (displayProductLayout.getVisibility() != View.VISIBLE) {
            reset();
            showDisplayLayout(true);
        } else super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        openAddSupplierPriceDialog(true, supplierPriceArrayList.get(i));
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        try {
            if (swipeRefreshLayout.getChildAt(0) != null) {
                swipeRefreshLayout.setEnabled(productList.getFirstVisiblePosition() == 0 && productList.getChildAt(0).getTop() == 0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        return false;
    }

    @Override
    public void onGroupCollapse(int i) {
        productList.expandGroup(i);
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
}
