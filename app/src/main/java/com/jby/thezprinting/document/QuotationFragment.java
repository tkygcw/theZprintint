package com.jby.thezprinting.document;

import android.os.Bundle;
import android.os.Handler;
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
import com.jby.thezprinting.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.shareObject.CustomToast.CustomToast;

public class QuotationFragment extends Fragment implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener,
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
    private QuotationAdapter quotationAdapter;
    /*
     * Async Task
     * */
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    /*
     * sorting purpose
     * */
    public String startDate, endDate;

    private FloatingActionButton createButton;

    public QuotationFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_quotation, container, false);
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
        quotationAdapter = new QuotationAdapter(getActivity(), expandableParentObjectArrayList, "quotation");

        startDate = getDate(true);
        endDate = getDate(false);
        handler = new Handler();
    }

    private void objectSetting() {
        swipeRefreshLayout.setOnRefreshListener(this);
        createButton.setOnClickListener(this);

        expandableListView.setAdapter(quotationAdapter);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnScrollListener(this);
        setupNotFoundLayout();
        showProgressBar(true);
        fetchParentItem("");
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_document_found));
        notFoundLabel.setText("No quotation is created yet");
    }


    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        if (expandableListView.isGroupExpanded(i)) expandGroup(i);
        return true;
    }


    public void fetchParentItem(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read_parent", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("company_id", SharedPreferenceManager.getCompanyId(getActivity())));
                apiDataObjectArrayList.add(new ApiDataObject("query", query));
                apiDataObjectArrayList.add(new ApiDataObject("start_date", startDate));
                apiDataObjectArrayList.add(new ApiDataObject("end_date", endDate));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().quotation,
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
                        Log.d("haha", "quotation json: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        reset();
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("parent");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                /*
                                                 * parent item
                                                 * */
                                                expandableParentObjectArrayList.add(new ExpandableParentObject(jsonArray.getJSONObject(i).getString("date")));
                                                /*
                                                 * child item
                                                 * */
                                                expandableParentObjectArrayList.get(i).setDocumentObjectArrayList(separateDocument(jsonArray.getJSONObject(i).getString("quotation_detail")));
                                                expandGroup(i);
                                            }
                                        }
                                    } catch (JSONException | IndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }
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
                setVisibility();
            }
        }).start();
    }

    private ArrayList<DocumentObject> separateDocument(String documents) {
        ArrayList<DocumentObject> expandableChildObjectArrayList = new ArrayList<>();
        try {
            String[] document = documents.split(";");
            for (String s : document) {
                String[] productDetail = s.split("\\$");
                expandableChildObjectArrayList.add(new DocumentObject(
                        productDetail[0],
                        productDetail[1],
                        productDetail[2],
                        productDetail[3],
                        productDetail[5],
                        productDetail[6],
                        ""));
            }
        } catch (NullPointerException e) {
            CustomToast(getActivity(), "No Item Found!");
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            CustomToast(getActivity(), "Something went wrong!");
        }
        return expandableChildObjectArrayList;
    }

    private void expandGroup(final int position) {
        expandableListView.expandGroup(position);
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
                quotationAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showProgressBar(boolean show) {
        ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
    }

    @Override
    public boolean onChildClick(final ExpandableListView expandableListView, final View view, int i, int i1, long l) {
        expandableListView.setEnabled(false);

        Bundle bundle = new Bundle();
        bundle.putSerializable("object", (Serializable) expandableParentObjectArrayList.get(i).getDocumentObjectArrayList().get(i1));
        bundle.putString("created_at", expandableParentObjectArrayList.get(i).getDate());
        bundle.putString("action", "edit");
        bundle.putString("type", "quotation");
        openDetailActivity(bundle);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                expandableListView.setEnabled(true);
            }
        },200);
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.create_button) {
            Bundle bundle = new Bundle();
            bundle.putString("action", "create");
            bundle.putString("type", "quotation");
            openDetailActivity(bundle);
        }
    }

    private void openDetailActivity(Bundle bundle) {
        ((MainActivity) Objects.requireNonNull(getActivity())).openDetailActivity(bundle);
    }

    @Override
    public void onRefresh() {
        showProgressBar(true);
        expandableParentObjectArrayList.clear();
        fetchParentItem("");
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

    /*--------------------------------------------------------------search----------------------------------------------------------------------------*/
    public void reset() {
        expandableParentObjectArrayList.clear();
        notifyDataSetChanged();
    }

    /*--------------------------------------------------------------date----------------------------------------------------------------------------*/
    private String getDate(boolean from) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = (from ? calendar.getActualMinimum(Calendar.DATE) : calendar.get(Calendar.DAY_OF_MONTH));
        return year + "-" + String.format(Locale.getDefault(), "%02d", (month + (from ? 0 : 1))) + "-" + String.format(Locale.getDefault(), "%02d", day);
    }
}
