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

import com.jby.thezprinting.R;
import com.jby.thezprinting.adapter.CustomerAdapter;
import com.jby.thezprinting.database.CustomSqliteHelper;
import com.jby.thezprinting.database.FrameworkClass;
import com.jby.thezprinting.database.ResultCallBack;
import com.jby.thezprinting.object.CustomerObject;
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
import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;


public class CustomerDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener,
        ResultCallBack, View.OnClickListener, AbsListView.MultiChoiceModeListener, CustomerAdapter.CustomerAdapterCallBack {
    View rootView;
    private ExpandableHeightListView customerList;
    private ArrayList<CustomerObject> customerObjectArrayList;
    private CustomerAdapter customerAdapter;
    /*
     * display layout customer
     * */
    private LinearLayout displayParentLayout;
    private Button addNewButton;

    private TextView labelFavouriteCustomer;
    private LinearLayout customerNotFoundLayout;

    private ExpandableHeightListView favouriteCustomerList;
    private ArrayList<CustomerObject> favouriteCustomerArrayList;
    private CustomerAdapter favouriteCustomerAdapter;
    /*
     * add layout customer
     * */
    private LinearLayout addParentLayout;
    private EditText customer, customerAddress, customerContact;
    private Button addButton, cancelAddButton;

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
    private String customerID = "";

    /*
     * search
     * */
    private SearchView customerDialogSearch;
    private String query = "";
    /*
     * activity control purpose
     * */
    public CustomerDialogCallBack customerDialogCallBack;

    public CustomerDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.customer_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        /*
         * display layout
         * */
        displayParentLayout = rootView.findViewById(R.id.display_customer_layout);
        addNewButton = rootView.findViewById(R.id.add_new_button);

        customerDialogSearch = rootView.findViewById(R.id.customer_dialog_search);
        customerList = rootView.findViewById(R.id.customer_dialog_customer_list);

        labelFavouriteCustomer = rootView.findViewById(R.id.customer_dialog_label_recent_choose);
        favouriteCustomerList = rootView.findViewById(R.id.customer_dialog_favourite_customer_list);

        /*
         * add customer layout
         * */
        addParentLayout = rootView.findViewById(R.id.add_customer_layout);
        addButton = rootView.findViewById(R.id.add_button);
        cancelAddButton = rootView.findViewById(R.id.back_button);

        customer = rootView.findViewById(R.id.customer);
        customerAddress = rootView.findViewById(R.id.address);
        customerContact = rootView.findViewById(R.id.contact);

        favouriteCustomerArrayList = new ArrayList<>();
        customerObjectArrayList = new ArrayList<>();

        customerAdapter = new CustomerAdapter(getActivity(), customerObjectArrayList, true, this);
        favouriteCustomerAdapter = new CustomerAdapter(getActivity(), favouriteCustomerArrayList, false, this);

        customerNotFoundLayout = rootView.findViewById(R.id.customer_not_found_layout);

        handler = new Handler();

    }

    private void objectSetting() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            boolean isMainActivity = bundle.getBoolean("isMainActivity");
            /*
             * if not main activity then initialize these things
             * */
            if (!isMainActivity) {
                customerDialogCallBack = (CustomerDialogCallBack) getActivity();
                customerList.setOnItemClickListener(this);
                favouriteCustomerList.setOnItemClickListener(this);
            }
        }
        cancelAddButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        addNewButton.setOnClickListener(this);

        customerList.setAdapter(customerAdapter);
        customerList.setExpanded(true);
        customerList.setMultiChoiceModeListener(this);
        customerList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        favouriteCustomerList.setAdapter(favouriteCustomerAdapter);
        favouriteCustomerList.setExpanded(true);

        customerDialogSearch.setOnQueryTextListener(this);

        frameworkClass = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_DEFAULT_CUSTOMER);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                frameworkClass.new Read("*").where("company_id = " + SharedPreferenceManager.getCompanyId(getActivity())).orderByDesc("customer_id").perform();
                fetchCustomer(query);
            }
        }, 200);
        /*
         * show display customer layout
         * */
        showAddCustomerLayout(false);
    }

    private void fetchCustomer(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("query", query));
                apiDataObjectArrayList.add(new ApiDataObject("company_id", SharedPreferenceManager.getCompanyId(getActivity())));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().customer,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("customer");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    customerObjectArrayList.add(new CustomerObject(
                                            jsonArray.getJSONObject(i).getString("customer_id"),
                                            jsonArray.getJSONObject(i).getString("name"),
                                            jsonArray.getJSONObject(i).getString("address"),
                                            jsonArray.getJSONObject(i).getString("contact")));
                                }
                                setUpVisibility();
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

    private void notifyDataSetChanged() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customerAdapter.notifyDataSetChanged();
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
                    if (customerObjectArrayList.size() > 0) {
                        customerList.setVisibility(View.VISIBLE);
                        customerNotFoundLayout.setVisibility(View.GONE);
                    } else {
                        customerList.setVisibility(View.GONE);
                        customerNotFoundLayout.setVisibility(View.VISIBLE);
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
    public boolean onQueryTextChange(String query) {
        //hide favourite list
        if (query.length() > 0) hide(true);
        else hide(false);
        /*
         * reset
         * */
        reset();

        this.query = query;
        fetchCustomer(query);
        return false;
    }

    private void hide(boolean hide) {
        if (hide) {
            favouriteCustomerList.setVisibility(View.GONE);
            labelFavouriteCustomer.setVisibility(View.GONE);
        } else {
            favouriteCustomerList.setVisibility(View.VISIBLE);
            labelFavouriteCustomer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.customer_dialog_favourite_customer_list:
                customerDialogCallBack.selectedItem(favouriteCustomerArrayList.get(i));
                /*
                 * store value into favourite list
                 * */
                frameworkClass.new create("customer_id, name, address, contact, company_id",
                        new String[]{
                                favouriteCustomerArrayList.get(i).getCustomerID(),
                                favouriteCustomerArrayList.get(i).getName(),
                                favouriteCustomerArrayList.get(i).getAddress(),
                                favouriteCustomerArrayList.get(i).getContact(),
                                SharedPreferenceManager.getCompanyId(getActivity())
                        }).perform();
                break;
            case R.id.customer_dialog_customer_list:
                customerDialogCallBack.selectedItem(customerObjectArrayList.get(i));
                /*
                 * store value into favourite list
                 * */
                frameworkClass.new create("customer_id, name, address, contact, company_id",
                        new String[]{
                                customerObjectArrayList.get(i).getCustomerID(),
                                customerObjectArrayList.get(i).getName(),
                                customerObjectArrayList.get(i).getAddress(),
                                customerObjectArrayList.get(i).getContact(),
                                SharedPreferenceManager.getCompanyId(getActivity())
                        }).perform();
                break;
        }
        dismiss();
    }

    /*-------------------------------------------------------local database control----------------------------------------------------------------------------------*/
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
                if (favouriteCustomerArrayList.size() < 3) {
                    Log.d("haha", "haha: big loop");
                    //add item into favouriteCustomerArrayList when size = 0
                    if (favouriteCustomerArrayList.size() <= 0) {
                        favouriteCustomerArrayList.add(new CustomerObject(
                                jsonArray.getJSONObject(i).getString("customer_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                jsonArray.getJSONObject(i).getString("address"),
                                jsonArray.getJSONObject(i).getString("contact")
                        ));
                    }
                    //favouriteCustomerArrayList.size > 0
                    else {
                        //check repeat values
                        for (int j = 0; j < favouriteCustomerArrayList.size(); j++) {
                            if (!favouriteCustomerArrayList.get(j).getName().equals(jsonArray.getJSONObject(i).getString("name")))
                                count++;
                        }
                        //if count == favourite.size() mean that one is new item
                        if (count == favouriteCustomerArrayList.size())
                            favouriteCustomerArrayList.add(new CustomerObject(
                                    jsonArray.getJSONObject(i).getString("customer_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("address"),
                                    jsonArray.getJSONObject(i).getString("contact")
                            ));
                        count = 0;
                    }
                } else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        favouriteCustomerAdapter.notifyDataSetChanged();
        if (favouriteCustomerArrayList.size() <= 0)
            labelFavouriteCustomer.setVisibility(View.GONE);
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
                showAddCustomerLayout(true);
                break;
            case R.id.add_button:
                checkingInput(isUpdate);
                break;
            case R.id.back_button:
                showAddCustomerLayout(false);
                break;
        }
    }

    /*-------------------------------------------------------------add / display layout control-----------------------------------------------------------------*/
    private void showAddCustomerLayout(boolean show) {
        addParentLayout.setVisibility(show ? View.VISIBLE : View.GONE);

        displayParentLayout.setVisibility(!show ? View.VISIBLE : View.GONE);
        customerDialogSearch.setVisibility(!show ? View.VISIBLE : View.GONE);

        customer.setText("");
        customerAddress.setText("");
        customerContact.setText("");

        isUpdate = false;
    }

    /*-----------------------------------------------------------------create delete update----------------------------------------------------------------------------------*/
    private void checkingInput(boolean isUpdate) {
        if (!customer.getText().toString().equals("")) {
            if (!isUpdate) storeCustomer();
            else updateCustomer();
        } else {
            CustomToast(getActivity(), "Name is required!");
        }
    }

    private void storeCustomer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("name", customer.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("address", customerAddress.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("contact", customerContact.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("company_id", SharedPreferenceManager.getCompanyId(getActivity())));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().customer,
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
                                        reset();
                                        fetchCustomer(query);
                                        showAddCustomerLayout(false);
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

    private void deleteCustomer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", TextUtils.join(", ", getSelectedItem())));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().customer,
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

    private void updateCustomer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerID));
                apiDataObjectArrayList.add(new ApiDataObject("name", customer.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("address", customerAddress.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("contact", customerContact.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().customer,
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
                                        reset();
                                        showAddCustomerLayout(false);
                                        fetchCustomer(query);
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
        customerObjectArrayList.clear();
        notifyDataSetChanged();
    }

    /*---------------------------------------------------------------multiple delete------------------------------------------------------------------*/
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkDeleteItemCount = customerList.getCheckedItemCount();
        // Set the  CAB title according to total checkDeleteItem items
        actionMode.setTitle(checkDeleteItemCount + "  Selected");

        // Calls  toggleSelection method from ListViewAdapter Class
        customerAdapter.toggleSelection(position);
        checkDeleteItem = customerList.getCheckedItemPositions();
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
                final int checkDeleteItemCount = customerObjectArrayList.size();
                customerAdapter.removeSelection();
                for (int i = 0; i < checkDeleteItemCount; i++) {
                    customerList.setItemChecked(i, true);
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
        customerAdapter.removeSelection();
        list.clear();
    }

    public ArrayList<String> getSelectedItem() {
        for (int i = 0; i < customerList.getCount(); i++) {
            if (checkDeleteItem.get(i)) {
                list.add(customerObjectArrayList.get(i).getCustomerID());
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
        for (int i = customerList.getCount() - 1; i >= 0; i--) {
            if (customerAdapter.getSelectedIds().get(i)) {
                customerObjectArrayList.remove(i);
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
                        deleteCustomer();
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

    @Override
    public void editCustomer(CustomerObject object) {
        showAddCustomerLayout(true);

        customerID = object.getCustomerID();
        customer.append(object.getName());
        customerAddress.append(object.getAddress());
        customerContact.append(object.getContact());
        isUpdate = true;
    }


    public interface CustomerDialogCallBack {
        void selectedItem(CustomerObject object);
    }
}
