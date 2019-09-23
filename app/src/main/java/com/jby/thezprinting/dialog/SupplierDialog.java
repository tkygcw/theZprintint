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
import com.jby.thezprinting.adapter.SupplierAdapter;
import com.jby.thezprinting.database.CustomSqliteHelper;
import com.jby.thezprinting.database.FrameworkClass;
import com.jby.thezprinting.database.ResultCallBack;
import com.jby.thezprinting.object.SupplierObject;
import com.jby.thezprinting.others.ExpandableHeightListView;
import com.jby.thezprinting.others.SwipeDismissTouchListener;
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

import static com.jby.thezprinting.database.CustomSqliteHelper.TB_DEFAULT_CUSTOMER;
import static com.jby.thezprinting.database.CustomSqliteHelper.TB_DEFAULT_SUPPLIER;
import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;


public class SupplierDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener,
        ResultCallBack, View.OnClickListener, AbsListView.MultiChoiceModeListener, SupplierAdapter.SupplierAdapterCallBack {
    View rootView;
    private ExpandableHeightListView supplierList;
    private ArrayList<SupplierObject> supplierObjectArrayList;
    private SupplierAdapter supplierAdapter;
    /*
     * display layout supplier
     * */
    private LinearLayout displayParentLayout;
    private Button addNewButton;

    private TextView labelFavouriteSupplier;
    private LinearLayout supplierNotFoundLayout;

    private ExpandableHeightListView favouriteSupplierList;
    private ArrayList<SupplierObject> favouriteSupplierArrayList;
    private SupplierAdapter favouriteSupplierAdapter;
    /*
     * add layout supplier
     * */
    private LinearLayout addParentLayout;
    private EditText supplier, supplierAddress, supplierContact, supplierWebsite, supplierEmail;
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
    private String supplierID = "";

    /*
     * search
     * */
    private SearchView supplierDialogSearch;
    private String query = "";
    /*
     * activity control purpose
     * */

    public SupplierDialogCallBack supplierDialogCallBack;

    public SupplierDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.supplier_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        /*
         * display layout
         * */
        displayParentLayout = rootView.findViewById(R.id.display_supplier_layout);
        addNewButton = rootView.findViewById(R.id.add_new_button);

        supplierDialogSearch = rootView.findViewById(R.id.supplier_dialog_search);
        supplierList = rootView.findViewById(R.id.supplier_dialog_supplier_list);

        labelFavouriteSupplier = rootView.findViewById(R.id.supplier_dialog_label_recent_choose);
        favouriteSupplierList = rootView.findViewById(R.id.supplier_dialog_favourite_supplier_list);

        /*
         * add supplier layout
         * */
        addParentLayout = rootView.findViewById(R.id.add_supplier_layout);
        addButton = rootView.findViewById(R.id.add_button);
        cancelAddButton = rootView.findViewById(R.id.back_button);

        supplier = rootView.findViewById(R.id.supplier);
        supplierAddress = rootView.findViewById(R.id.address);
        supplierContact = rootView.findViewById(R.id.contact);
        supplierEmail = rootView.findViewById(R.id.email);
        supplierWebsite = rootView.findViewById(R.id.website);

        favouriteSupplierArrayList = new ArrayList<>();
        supplierObjectArrayList = new ArrayList<>();

        supplierAdapter = new SupplierAdapter(getActivity(), supplierObjectArrayList, true, this);
        favouriteSupplierAdapter = new SupplierAdapter(getActivity(), favouriteSupplierArrayList, false, this);

        supplierNotFoundLayout = rootView.findViewById(R.id.supplier_not_found_layout);

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
                try {
                    supplierDialogCallBack = (SupplierDialogCallBack) getActivity();

                } catch (ClassCastException e) {
                    supplierDialogCallBack = (SupplierDialogCallBack) getParentFragment();

                }
                supplierList.setOnItemClickListener(this);
                favouriteSupplierList.setOnItemClickListener(this);
            }
        }
        cancelAddButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        addNewButton.setOnClickListener(this);

        supplierList.setAdapter(supplierAdapter);
        supplierList.setExpanded(true);
        supplierList.setMultiChoiceModeListener(this);
        supplierList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        favouriteSupplierList.setAdapter(favouriteSupplierAdapter);
        favouriteSupplierList.setExpanded(true);

        supplierDialogSearch.setOnQueryTextListener(this);

        frameworkClass = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_DEFAULT_SUPPLIER);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                frameworkClass.new Read("*").orderByDesc("supplier_id").perform();
            }
        }, 200);
        fetchCustomer(query);
        /*
         * show display supplier layout
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
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().supplier,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("supplier");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    supplierObjectArrayList.add(new SupplierObject(
                                            jsonArray.getJSONObject(i).getString("supplier_id"),
                                            jsonArray.getJSONObject(i).getString("name"),
                                            jsonArray.getJSONObject(i).getString("address"),
                                            jsonArray.getJSONObject(i).getString("email"),
                                            jsonArray.getJSONObject(i).getString("contact"),
                                            jsonArray.getJSONObject(i).getString("website")));
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                supplierAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setUpVisibility() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (supplierObjectArrayList.size() > 0) {
                    supplierList.setVisibility(View.VISIBLE);
                    supplierNotFoundLayout.setVisibility(View.GONE);
                } else {
                    supplierList.setVisibility(View.GONE);
                    supplierNotFoundLayout.setVisibility(View.VISIBLE);
                }
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
            favouriteSupplierList.setVisibility(View.GONE);
            labelFavouriteSupplier.setVisibility(View.GONE);
        } else {
            favouriteSupplierList.setVisibility(View.VISIBLE);
            labelFavouriteSupplier.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.supplier_dialog_favourite_supplier_list:
                supplierDialogCallBack.selectedItem(favouriteSupplierArrayList.get(i));
                /*
                 * store value into favourite list
                 * */
                frameworkClass.new create("supplier_id, name, address, email, contact, website",
                        new String[]{
                                favouriteSupplierArrayList.get(i).getSupplier_id(),
                                favouriteSupplierArrayList.get(i).getName(),
                                favouriteSupplierArrayList.get(i).getAddress(),
                                favouriteSupplierArrayList.get(i).getEmail(),
                                favouriteSupplierArrayList.get(i).getContact(),
                                favouriteSupplierArrayList.get(i).getWebsite(),
                        }).perform();
                break;
            case R.id.supplier_dialog_supplier_list:
                supplierDialogCallBack.selectedItem(supplierObjectArrayList.get(i));
                /*
                 * store value into favourite list
                 * */
                frameworkClass.new create("supplier_id, name, address, email, contact, website",
                        new String[]{
                                supplierObjectArrayList.get(i).getSupplier_id(),
                                supplierObjectArrayList.get(i).getName(),
                                supplierObjectArrayList.get(i).getAddress(),
                                supplierObjectArrayList.get(i).getEmail(),
                                supplierObjectArrayList.get(i).getContact(),
                                supplierObjectArrayList.get(i).getWebsite(),
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
                if (favouriteSupplierArrayList.size() < 3) {
                    Log.d("haha", "haha: big loop");
                    //add item into favouriteSupplierList when size = 0
                    if (favouriteSupplierArrayList.size() <= 0) {
                        favouriteSupplierArrayList.add(new SupplierObject(
                                jsonArray.getJSONObject(i).getString("supplier_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                jsonArray.getJSONObject(i).getString("address"),
                                jsonArray.getJSONObject(i).getString("email"),
                                jsonArray.getJSONObject(i).getString("contact"),
                                jsonArray.getJSONObject(i).getString("website")
                        ));
                    }
                    //favouriteSupplierList.size > 0
                    else {
                        //check repeat values
                        for (int j = 0; j < favouriteSupplierArrayList.size(); j++) {
                            if (!favouriteSupplierArrayList.get(j).getName().equals(jsonArray.getJSONObject(i).getString("name")))
                                count++;
                        }
                        //if count == favourite.size() mean that one is new item
                        if (count == favouriteSupplierArrayList.size())
                            favouriteSupplierArrayList.add(new SupplierObject(
                                    jsonArray.getJSONObject(i).getString("supplier_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("address"),
                                    jsonArray.getJSONObject(i).getString("email"),
                                    jsonArray.getJSONObject(i).getString("contact"),
                                    jsonArray.getJSONObject(i).getString("website")
                            ));
                        count = 0;
                    }
                } else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        favouriteSupplierAdapter.notifyDataSetChanged();
        if (favouriteSupplierArrayList.size() <= 0)
            labelFavouriteSupplier.setVisibility(View.GONE);
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
        supplierDialogSearch.setVisibility(!show ? View.VISIBLE : View.GONE);

        supplier.setText("");
        supplierAddress.setText("");
        supplierContact.setText("");
        supplierEmail.setText("");
        supplierWebsite.setText("");

        isUpdate = false;
    }

    /*-----------------------------------------------------------------create delete update----------------------------------------------------------------------------------*/
    private void checkingInput(boolean isUpdate) {
        if (!supplier.getText().toString().equals("")) {
            if (!isUpdate) storeSupplier();
            else updateSupplier();
        } else {
            CustomToast(getActivity(), "Name is required!");
        }
    }

    private void storeSupplier() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("name", supplier.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("address", supplierAddress.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("contact", supplierContact.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("website", supplierWebsite.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("email", supplierEmail.getText().toString()));


                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().supplier,
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

    private void deleteSupplier() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("supplier_id", TextUtils.join(", ", getSelectedItem())));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().supplier,
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

    private void updateSupplier() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("supplier_id", supplierID));
                apiDataObjectArrayList.add(new ApiDataObject("name", supplier.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("address", supplierAddress.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("contact", supplierContact.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("website", supplierWebsite.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("email", supplierEmail.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("update", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().supplier,
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
        supplierObjectArrayList.clear();
        notifyDataSetChanged();
    }

    /*---------------------------------------------------------------multiple delete------------------------------------------------------------------*/
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkDeleteItemCount = supplierList.getCheckedItemCount();
        // Set the  CAB title according to total checkDeleteItem items
        actionMode.setTitle(checkDeleteItemCount + "  Selected");

        // Calls  toggleSelection method from ListViewAdapter Class
        supplierAdapter.toggleSelection(position);
        checkDeleteItem = supplierList.getCheckedItemPositions();
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
                final int checkDeleteItemCount = supplierObjectArrayList.size();
                supplierAdapter.removeSelection();
                for (int i = 0; i < checkDeleteItemCount; i++) {
                    supplierList.setItemChecked(i, true);
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
        supplierAdapter.removeSelection();
        list.clear();
    }

    public ArrayList<String> getSelectedItem() {
        for (int i = 0; i < supplierList.getCount(); i++) {
            if (checkDeleteItem.get(i)) {
                list.add(supplierObjectArrayList.get(i).getSupplier_id());
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
        for (int i = supplierList.getCount() - 1; i >= 0; i--) {
            if (supplierAdapter.getSelectedIds().get(i)) {
                supplierObjectArrayList.remove(i);
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
                        deleteSupplier();
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
    public void editSupplier(SupplierObject object) {
        showAddCustomerLayout(true);

        supplierID = object.getSupplier_id();
        supplier.append(object.getName());
        supplierAddress.append(object.getAddress());
        supplierContact.append(object.getContact());
        supplierEmail.append(object.getEmail());
        supplierWebsite.append(object.getWebsite());
        isUpdate = true;
    }


    public interface SupplierDialogCallBack {
        void selectedItem(SupplierObject object);
    }
}
