package com.jby.thezprinting.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.DocumentObject;

import java.util.ArrayList;


public class DocumentDetailAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DocumentObject> documentObjectArrayList;
    /*
     ** for delete purpose
     */
    private SparseBooleanArray deleteItem;

    public DocumentDetailAdapter(Context context, ArrayList<DocumentObject> documentObjectArrayList) {
        this.context = context;
        this.documentObjectArrayList = documentObjectArrayList;
        deleteItem = new SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return documentObjectArrayList.size();
    }

    @Override
    public DocumentObject getItem(int i) {
        return documentObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(this.context, R.layout.document_detail_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        DocumentObject object = getItem(i);
        viewHolder.item.setText(object.getItem());
        viewHolder.quantity.setText(object.getQuantity());
        viewHolder.price.setText("RM " + object.getPrice());
        viewHolder.sub_total.setText("RM " + object.getSubTotal());
        //delete and spoil purpose
        if (deleteItem.size() > 0 && deleteItem.get(i)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.selected_color));
        } else view.setBackgroundColor(context.getResources().getColor(R.color.white));

        return view;
    }
    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder {
        private TextView item, quantity, price, sub_total;

        ViewHolder(View view) {
            item = view.findViewById(R.id.item);
            quantity = view.findViewById(R.id.quantity);
            price = view.findViewById(R.id.price);
            sub_total = view.findViewById(R.id.sub_total);
        }
    }

    /*------------------------------------------------------multiple delete purpose-----------------------------------------------------------*/

    public void toggleSelection(int position) {
        selectView(position, !deleteItem.get(position));
    }

    // Remove selection after unchecked
    public void removeSelection() {
        deleteItem = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    private void selectView(int position, boolean value) {
        if (value) {
            deleteItem.put(position, true);
        } else {
            deleteItem.delete(position);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return deleteItem;
    }
}
