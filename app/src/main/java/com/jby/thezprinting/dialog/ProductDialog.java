package com.jby.thezprinting.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.thezprinting.R;
import com.jby.thezprinting.adapter.ProductAdapter;
import com.jby.thezprinting.database.CustomSqliteHelper;
import com.jby.thezprinting.database.FrameworkClass;
import com.jby.thezprinting.database.ResultCallBack;
import com.jby.thezprinting.object.DocumentObject;
import com.jby.thezprinting.object.ProductObject;
import com.jby.thezprinting.object.ProductObject;
import com.jby.thezprinting.others.ExpandableHeightListView;
import com.jby.thezprinting.others.SwipeDismissTouchListener;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;
import com.jby.thezprinting.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.database.CustomSqliteHelper.TB_DEFAULT_CUSTOMER;
import static com.jby.thezprinting.database.CustomSqliteHelper.TB_DEFAULT_PRODUCT;
import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;


public class ProductDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener,
        ResultCallBack, View.OnClickListener, AbsListView.MultiChoiceModeListener {
    View rootView;
    private ExpandableHeightListView productList;
    private ArrayList<ProductObject> productObjectArrayList;
    private ProductAdapter productAdapter;
    /*
     * display layout product
     * */
    private LinearLayout displayParentLayout;
    private Button addNewButton;

    private TextView labelFavouriteProduct;
    private LinearLayout productNotFoundLayout;

    private ExpandableHeightListView favouriteProductList;
    private ArrayList<ProductObject> favouriteProductArrayList;
    private ProductAdapter favouriteProductAdapter;
    /*
     * add layout product
     * */
    private LinearLayout addParentLayout;
    private EditText product, productPrice, productDescription, productQuantity;
    private Button addButton, updateButton, cancelAddButton;

    private FrameworkClass frameworkClass;

    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    /*
     * delete purpose
     * */
    SparseBooleanArray checkDeleteItem;
    List<String> list = new ArrayList<String>();
    ActionMode actionMode;
    /*
     * update control
     * */
    private boolean isUpdate = false;
    private String productID = "";
    /*
     * search
     * */
    private SearchView productDialogSearch;
    private String query = "";
    /*
     * activity control purpose
     * */
    boolean isMainActivity = false;
    boolean isListUpdate = false;
    /*
     * object
     * */
    private ProductObject productObject;

    public ProductDialogCallBack productDialogCallBack;

    public ProductDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.product_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        /*
         * display layout
         * */
        displayParentLayout = rootView.findViewById(R.id.display_product_layout);
        addNewButton = rootView.findViewById(R.id.add_new_button);

        productDialogSearch = rootView.findViewById(R.id.product_dialog_search);
        productList = rootView.findViewById(R.id.product_dialog_product_list);

        labelFavouriteProduct = rootView.findViewById(R.id.product_dialog_label_recent_choose);
        favouriteProductList = rootView.findViewById(R.id.product_dialog_favourite_product_list);

        /*
         * add product layout
         * */
        addParentLayout = rootView.findViewById(R.id.add_product_layout);
        addButton = rootView.findViewById(R.id.add_button);
        updateButton = rootView.findViewById(R.id.update_button);
        cancelAddButton = rootView.findViewById(R.id.back_button);

        product = rootView.findViewById(R.id.item);
        productDescription = rootView.findViewById(R.id.itemDescription);
        productQuantity = rootView.findViewById(R.id.quantity);
        productPrice = rootView.findViewById(R.id.price);

        favouriteProductArrayList = new ArrayList<>();
        productObjectArrayList = new ArrayList<>();

        productAdapter = new ProductAdapter(getActivity(), productObjectArrayList, true);
        favouriteProductAdapter = new ProductAdapter(getActivity(), favouriteProductArrayList, false);

        productNotFoundLayout = rootView.findViewById(R.id.product_not_found_layout);

        handler = new Handler();

    }

    private void objectSetting() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            isMainActivity = bundle.getBoolean("isMainActivity");
            isListUpdate = bundle.getBoolean("isUpdateList");
            /*
             * if not main activity then initialize these things
             * */
            if (!isMainActivity) {
                productDialogCallBack = (ProductDialogCallBack) getActivity();
                /*
                 * update list from activity detail
                 * */
                if (isListUpdate) {
                    editProduct((ProductObject) Objects.requireNonNull(bundle.getSerializable("updateObject")));
                } else {
                    productList.setOnItemClickListener(this);
                    favouriteProductList.setOnItemClickListener(this);
                    /*
                     * show product list rather than add page
                     * */
                    showAddProductLayout(false);
                }
            } else {
                /*
                * hide quantity layout when open from main activity
                * */
                rootView.findViewById(R.id.layout_quantity).setVisibility(View.GONE);

                productList.setOnItemClickListener(this);
                /*
                 * show product list rather than add page
                 * */
                showAddProductLayout(false);
            }
        }
        cancelAddButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        updateButton.setOnClickListener(this);
        addNewButton.setOnClickListener(this);

        productList.setAdapter(productAdapter);
        productList.setExpanded(true);
        productList.setMultiChoiceModeListener(this);
        productList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        favouriteProductList.setAdapter(favouriteProductAdapter);
        favouriteProductList.setExpanded(true);

        productDialogSearch.setOnQueryTextListener(this);

        frameworkClass = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_DEFAULT_PRODUCT);
        /*
         * if not update then fetch product list
         * */
        if (!isListUpdate) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    frameworkClass.new Read("*").where("company_id =" + SharedPreferenceManager.getCompanyId(getActivity())).orderByDesc("product_id").perform();
                    fetchProduct(query);
                }
            }, 200);
        }
    }

    private void fetchProduct(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("company_id", SharedPreferenceManager.getCompanyId(getActivity())));
                apiDataObjectArrayList.add(new ApiDataObject("query", query));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reset();
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            JSONArray jsonArray;
                                            try {
                                                jsonArray = jsonObjectLoginResponse.getJSONArray("product");
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    productObjectArrayList.add(new ProductObject(
                                                            jsonArray.getJSONObject(i).getString("product_id"),
                                                            jsonArray.getJSONObject(i).getString("name"),
                                                            jsonArray.getJSONObject(i).getString("price"),
                                                            jsonArray.getJSONObject(i).getString("description")));
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    setUpVisibility();
                                }
                            });
                        } else {
                            CustomToast(getActivity(), "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getActivity(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getActivity(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                notifyDataSetChanged();
            }
        }).start();
    }

    private void notifyDataSetChanged() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    productAdapter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpVisibility() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (productObjectArrayList.size() > 0) {
                        productList.setVisibility(View.VISIBLE);
                        productNotFoundLayout.setVisibility(View.GONE);
                    } else {
                        productList.setVisibility(View.GONE);
                        productNotFoundLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
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

    /*-----------------------------------------------------------------search ----------------------------------------------------------------------------------*/
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        //hide favourite list
        if (query.length() > 0) hide(true);
        else hide(false);
        this.query = query;
        fetchProduct(query);
        return false;
    }

    private void hide(boolean hide) {
        if (hide) {
            favouriteProductList.setVisibility(View.GONE);
            labelFavouriteProduct.setVisibility(View.GONE);
        } else {
            favouriteProductList.setVisibility(View.VISIBLE);
            labelFavouriteProduct.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        showAddProductLayout(true);
        //hide add button when it's onclick from main activity
        showAddButton(!isMainActivity);

        editProduct(adapterView.getId() == R.id.product_dialog_favourite_product_list ? favouriteProductArrayList.get(i) : productObjectArrayList.get(i));
    }

    /*-------------------------------------------------------local database control----------------------------------------------------------------------------------*/
    private void addToCache(ProductObject productObject) {
        frameworkClass.new create("product_id, name, price, description, company_id",
                new String[]{
                        productObject.getProduct_id(),
                        productObject.getName(),
                        productObject.getPrice(),
                        productObject.getDescription(),
                        SharedPreferenceManager.getCompanyId(getActivity())
                }).perform();
    }

    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        JSONObject jsonObject = null;
        int count = 0;
        try {
            jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");

            for (int i = 0; i < jsonArray.length(); i++) {
                if (favouriteProductArrayList.size() < 3) {
                    Log.d("haha", "haha: big loop");
                    //add item into favouriteProductArrayList when size = 0
                    if (favouriteProductArrayList.size() <= 0) {
                        favouriteProductArrayList.add(new ProductObject(
                                jsonArray.getJSONObject(i).getString("product_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                jsonArray.getJSONObject(i).getString("price"),
                                jsonArray.getJSONObject(i).getString("description")
                        ));
                    }
                    //favouriteProductArrayList.size > 0
                    else {
                        //check repeat values
                        for (int j = 0; j < favouriteProductArrayList.size(); j++) {
                            if (!favouriteProductArrayList.get(j).getName().equals(jsonArray.getJSONObject(i).getString("name")))
                                count++;
                        }
                        //if count == favourite.size() mean that one is new item
                        if (count == favouriteProductArrayList.size())
                            favouriteProductArrayList.add(new ProductObject(
                                    jsonArray.getJSONObject(i).getString("product_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("price"),
                                    jsonArray.getJSONObject(i).getString("description")
                            ));
                        count = 0;
                    }
                } else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        favouriteProductAdapter.notifyDataSetChanged();
        if (favouriteProductArrayList.size() <= 0)
            labelFavouriteProduct.setVisibility(View.GONE);
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_new_button:
                showAddProductLayout(true);
                showAddButton(true);
                break;
            case R.id.add_button:
                checkingInput(isUpdate);
                break;
            case R.id.update_button:
                checkingUpdateInput();
                break;
            case R.id.back_button:
                if (isListUpdate) dismiss();
                else showAddProductLayout(false);
                break;
        }
    }

    /*-------------------------------------------------------------add / display layout control-----------------------------------------------------------------*/
    private void showAddProductLayout(boolean show) {
        addParentLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        displayParentLayout.setVisibility(!show ? View.VISIBLE : View.GONE);
        productDialogSearch.setVisibility(!show ? View.VISIBLE : View.GONE);
        /*
        * button control
        * */
        updateButton.setVisibility(!show ? View.VISIBLE : View.GONE);
        //change text when open from main activity
        updateButton.setText(isMainActivity ? "Update Item" :  "Update & ADD");

        product.setText("");
        productPrice.setText("");
        productDescription.setText("");
        productQuantity.setText("");

        isUpdate = false;
    }

    private void showAddButton(boolean show){
        addButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /*-----------------------------------------------------------------create delete update----------------------------------------------------------------------------------*/
    private void checkingInput(boolean isUpdate) {
        //create new purpose
        if (!isUpdate) {
            if (!product.getText().toString().equals("")) {
                storeProduct();
            } else {
                CustomToast(getActivity(), "Name is required!");
            }
        } else {
            if (!product.getText().toString().equals("") && !productQuantity.getText().toString().equals("") && !productPrice.getText().toString().equals("")) {
                productDialogCallBack.selectedProduct(new DocumentObject(productObject.getProduct_id(), product.getText().toString(), productDescription.getText().toString(), productPrice.getText().toString(), productQuantity.getText().toString(), subTotal()));
                addToCache(productObject);
                dismiss();
            } else {
                CustomToast(getActivity(), "Name, Quantity, Price is required!");
            }
        }
    }

    private void checkingUpdateInput() {
        if (!product.getText().toString().equals("")) {
            updateProduct();
        } else {
            CustomToast(getActivity(), "Name is required!");
        }
    }

    private String subTotal() {
        float price = Float.parseFloat(productPrice.getText().toString());
        String quantity = productQuantity.getText().toString();
        float subTotal = price * Float.parseFloat(quantity);
        return String.format("%.2f", subTotal);
    }

    private void storeProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("name", product.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("description", productDescription.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("price", productPrice.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("company_id", SharedPreferenceManager.getCompanyId(getActivity())));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
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
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CustomToast(getActivity(), "Store Successfully!");
                                        /*
                                         * store to cloud only
                                         * */
                                        if (isMainActivity) {
                                            reset();
                                            fetchProduct(query);
                                            showAddProductLayout(false);
                                        }
                                        /*
                                         * after store item to cloud then add into list
                                         * */
                                        else {
                                            try {
                                                productDialogCallBack.selectedProduct(new DocumentObject(jsonObjectLoginResponse.getString("product_id"), product.getText().toString(), productDescription.getText().toString(), productPrice.getText().toString(), productQuantity.getText().toString(), subTotal()));
                                                addToCache(new ProductObject(jsonObjectLoginResponse.getString("product_id"), product.getText().toString(), productPrice.getText().toString(), productDescription.getText().toString()));
                                                dismiss();
                                            } catch (JSONException e) {
                                                CustomToast(getActivity(), "Unable to add item!");
                                                e.printStackTrace();
                                            }
                                        }
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
                notifyDataSetChanged();
            }
        }).start();
    }

    private void deleteProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", TextUtils.join(", ", getSelectedItem())));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
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
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CustomToast(getActivity(), "Delete Successfully!");
                                        updateListAfterDelete();
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
                notifyDataSetChanged();
            }
        }).start();
    }

    private void updateProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productID));
                apiDataObjectArrayList.add(new ApiDataObject("name", product.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("description", productDescription.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("price", productPrice.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
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
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CustomToast(getActivity(), "Update Successfully!");
                                        /*
                                         * update item from cloud only
                                         * */
                                        if (isMainActivity) {
                                            reset();
                                            showAddProductLayout(false);
                                            fetchProduct(query);
                                        }
                                        /*
                                         * after update then store into list
                                         * */
                                        else {
                                            productDialogCallBack.selectedProduct(new DocumentObject(productID, product.getText().toString(), productDescription.getText().toString(), productPrice.getText().toString(), productQuantity.getText().toString(), subTotal()));
                                            addToCache(new ProductObject(productID, product.getText().toString(), productPrice.getText().toString(), productDescription.getText().toString()));
                                            dismiss();
                                        }
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
                notifyDataSetChanged();
            }
        }).start();
    }

    private void reset() {
        productObjectArrayList.clear();
        notifyDataSetChanged();
    }

    /*---------------------------------------------------------------multiple delete------------------------------------------------------------------*/
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkDeleteItemCount = productList.getCheckedItemCount();
        // Set the  CAB title according to total checkDeleteItem items
        actionMode.setTitle(checkDeleteItemCount + "  Selected");

        // Calls  toggleSelection method from ListViewAdapter Class
        productAdapter.toggleSelection(position);
        checkDeleteItem = productList.getCheckedItemPositions();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
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
                final int checkDeleteItemCount = productObjectArrayList.size();
                productAdapter.removeSelection();
                for (int i = 0; i < checkDeleteItemCount; i++) {
                    productList.setItemChecked(i, true);
                }
                actionMode.setTitle(checkDeleteItemCount + "  Selected");
                return true;
            case R.id.delete:
                deleteConfirmationDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        productAdapter.removeSelection();
        list.clear();
    }

    public ArrayList<String> getSelectedItem() {
        for (int i = 0; i < productList.getCount(); i++) {
            if (checkDeleteItem.get(i)) {
                list.add(productObjectArrayList.get(i).getProduct_id());
                Log.d("haha", "haha: size: " + list.size());
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
        for (int i = productList.getCount() - 1; i >= 0; i--) {
            if (productAdapter.getSelectedIds().get(i)) {
                productObjectArrayList.remove(i);
            }
        }
        getActionMode().finish();
    }

    public void deleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Request");
        builder.setMessage("Are you sure that you want to do so?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteProduct();
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

    public void editProduct(ProductObject object) {
        this.productObject = object;
        showAddProductLayout(true);

        productID = object.getProduct_id();
        product.append(object.getName());
        productPrice.append(object.getPrice());
        productDescription.append(object.getDescription());
        productQuantity.append(object.getQuantity());

        isUpdate = true;
        updateButton.setVisibility(View.VISIBLE);
    }

    public interface ProductDialogCallBack {
        void selectedProduct(DocumentObject object);
    }
}
