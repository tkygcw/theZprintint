package com.jby.thezprinting.document;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.thezprinting.MainActivity;
import com.jby.thezprinting.R;
import com.jby.thezprinting.adapter.QuotationAdapter;
import com.jby.thezprinting.object.DocumentObject;
import com.jby.thezprinting.object.ExpandableParentObject;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;


public class InvoiceFragment extends Fragment implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    View rootView;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    /*
     * list view
     * */
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExpandableListView expandableListView;
    private ArrayList<ExpandableParentObject> expandableParentObjectArrayList;
    private QuotationAdapter invoiceAdapter;
    private int groupPosition;
    /*
     * Async Task
     * */
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    private FloatingActionButton createButton;

    public InvoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_invoice, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        createButton = rootView.findViewById(R.id.create_button);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_layout);
        expandableListView = rootView.findViewById(R.id.fragment_quotation_expandable_list_view);
        expandableParentObjectArrayList = new ArrayList<>();
        invoiceAdapter = new QuotationAdapter(getActivity(), expandableParentObjectArrayList, "invoice");
    }

    private void objectSetting() {
        swipeRefreshLayout.setOnRefreshListener(this);
        createButton.setOnClickListener(this);
        expandableListView.setAdapter(invoiceAdapter);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnScrollListener(this);
        setupNotFoundLayout();
        fetchParentItem();
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.invoice_not_found));
        notFoundLabel.setText("No invoice is created yet");
    }


    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        groupPosition = i;
        if (expandableListView.isGroupExpanded(i)) expandableListView.collapseGroup(i);
        else {
            //close view
            closeOtherChildView(i);
            expandableParentObjectArrayList.get(i).getDocumentObjectArrayList().clear();
            fetchChildItem(i);
        }
        return true;
    }


    private void fetchParentItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read_parent", "1"));

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
                        Log.d("haha", "invoice json: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("parent");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    expandableParentObjectArrayList.add(new ExpandableParentObject(jsonArray.getJSONObject(i).getString("date")));
                                }
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
                setVisibility();
                preOpenChild();

            }
        }).start();
    }

    private void preOpenChild() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (expandableParentObjectArrayList.size() > 0) fetchChildItem(0);
            }
        });
    }


    private void closeOtherChildView(int position) {
        for (int i = 0; i < expandableParentObjectArrayList.size(); i++) {
            if (i != position) expandableListView.collapseGroup(i);
        }
    }


    /*------------------------------------child-------------------------------*/
    private void fetchChildItem(final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read_child", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("date", expandableParentObjectArrayList.get(position).getDate()));
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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("child");
                                setChildValue(jsonArray, position);
                            } else {
                                expandableParentObjectArrayList.remove(groupPosition);
                                setVisibility();
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

    private void setChildValue(JSONArray jsonArray, final int position) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    expandableParentObjectArrayList.get(position).setDocumentObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    expandableListView.expandGroup(position);
                    expandableListView.setSelectedGroup(position);
                    notifyDataSetChanged();
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private DocumentObject setChildObject(JSONObject jsonObject) {
        DocumentObject object = null;
        try {
            object = new DocumentObject(
                    jsonObject.getString("invoice_id"),
                    jsonObject.getString("created_at"),
                    jsonObject.getString("target"),
                    jsonObject.getString("status"),
                    jsonObject.getString("username"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void setVisibility() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                expandableListView.setVisibility(expandableParentObjectArrayList.size() > 0 ? View.VISIBLE : View.GONE);
                notFoundLayout.setVisibility(expandableParentObjectArrayList.size() > 0 ? View.GONE : View.VISIBLE);
                showProgressBar(false);
                notifyDataSetChanged();
            }
        });
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invoiceAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showProgressBar(boolean show) {
        ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("object", (Serializable) expandableParentObjectArrayList.get(i).getDocumentObjectArrayList().get(i1));
        bundle.putString("created_at", expandableParentObjectArrayList.get(i).getDate());
        bundle.putString("action", "edit");
        bundle.putString("type", "invoice");
        openDetailActivity(bundle);
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.create_button) {
            Bundle bundle = new Bundle();
            bundle.putString("action", "create");
            bundle.putString("type", "invoice");
            openDetailActivity(bundle);
        }
    }

    private void openDetailActivity(Bundle bundle) {
        ((MainActivity) Objects.requireNonNull(getActivity())).openDetailActivity(bundle);
    }

    @Override
    public void onRefresh() {
        groupPosition = 0;
        showProgressBar(true);
        expandableParentObjectArrayList.clear();
        fetchParentItem();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        try {
            if (swipeRefreshLayout.getChildAt(0) != null) {
                swipeRefreshLayout.setEnabled(expandableListView.getFirstVisiblePosition() == 0 && expandableListView.getChildAt(0).getTop() == 0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
