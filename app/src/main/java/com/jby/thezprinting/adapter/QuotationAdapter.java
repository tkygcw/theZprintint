package com.jby.thezprinting.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.DocumentObject;
import com.jby.thezprinting.object.ExpandableParentObject;

import java.util.ArrayList;

public class QuotationAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ExpandableParentObject> expandableParentObjectArrayList;
    private String documentType = "quotation";

    public QuotationAdapter(Context context, ArrayList<ExpandableParentObject> expandableParentObjectArrayList, String documentType) {
        this.context = context;
        this.expandableParentObjectArrayList = expandableParentObjectArrayList;
        this.documentType = documentType;

    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*-----------------------------------------------------------------------------PARENT VIEW-------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.expandable_list_view_parent_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        try {
            final ExpandableParentObject object = getGroup(groupPosition);
            groupViewHolder.date.setText(setDate(object.getDate()));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ExpandableParentObject getGroup(int i) {
        try {
            return expandableParentObjectArrayList.get(i);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return new ExpandableParentObject();
        }
    }

    private static class GroupViewHolder {
        private TextView date;

        GroupViewHolder(View view) {
            date = view.findViewById(R.id.parent_date);

        }
    }

    @Override
    public int getGroupCount() {
        return expandableParentObjectArrayList.size();
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View view, final ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.expandable_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        final DocumentObject object = getChild(groupPosition, childPosition);
        try{
            viewHolder.id.setText(setQuotationPlaceHolder(object.getDocumentNo()));
            viewHolder.target.setText(object.getTarget());
            viewHolder.date.setText(object.getDate());
            viewHolder.price.setText(object.getPrice());
            viewHolder.personInCharge.setText("In charge:" + object.getPersonInCharge());
            setStatus(viewHolder.status, object.getStatus());
        }catch (NullPointerException e){
        }

        return view;
    }

    private String setQuotationPlaceHolder(String do_id) {
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for (int i = do_idBuilder.length(); i < 5; i++) {
            do_idBuilder.insert(0, "0");
        }
        return (documentType.equals("quotation") ? "#Q" : "#I") + do_idBuilder.toString();
    }

    private void setStatus(TextView view, String status) {
        if (status.equals("0")) {
            view.setBackground(context.getResources().getDrawable(R.drawable.custom_pending_status));
            view.setText("Pending");
        } else if (status.equals("1")) {
            view.setBackground(context.getResources().getDrawable(R.drawable.custom_confirm_status));
            view.setText("Confirmed");
            view.setText(documentType.equals("quotation") ? "Confirmed" : "Completed");
        } else {
            view.setBackground(context.getResources().getDrawable(R.drawable.custom_rejected_status));
            view.setText("Rejected");
        }
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public int getChildrenCount(int i) {
        return expandableParentObjectArrayList.get(i).getDocumentObjectArrayList().size();
    }

    @Override
    public DocumentObject getChild(int groupPosition, int childPosition) {
        try {
            return expandableParentObjectArrayList.get(groupPosition).getDocumentObjectArrayList().get(childPosition);
        } catch (IndexOutOfBoundsException e) {
            return new DocumentObject();
        }

    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        private TextView target, id, date, price, status, personInCharge;

        ChildViewHolder(View view) {
            target = view.findViewById(R.id.target);
            id = view.findViewById(R.id.id);
            date = view.findViewById(R.id.date);
            price = view.findViewById(R.id.price);
            status = view.findViewById(R.id.status);
            personInCharge = view.findViewById(R.id.personInCharge);
        }
    }
    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    private String setDate(String date) {
        if (date.equals(String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()))))
            return "Today";
        else return date;
    }

}
