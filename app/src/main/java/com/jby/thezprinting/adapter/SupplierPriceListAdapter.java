package com.jby.thezprinting.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.SupplierObject;

import java.util.ArrayList;

import mobile.sarproj.com.layout.SwipeLayout;


public class SupplierPriceListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SupplierObject> supplierObjectArrayList;
    /*
     ** for delete purpose
     */
    private SparseBooleanArray deleteItem;
    /*
     * type
     * */
    private boolean isNormalList = true;
    /*
     * call back
     * */
    private SupplierAdapterCallBack customerAdapterCallBack;

    public SupplierPriceListAdapter(Context context, ArrayList<SupplierObject> supplierObjectArrayList, SupplierAdapterCallBack customerAdapterCallBack) {
        this.context = context;
        this.supplierObjectArrayList = supplierObjectArrayList;
        this.customerAdapterCallBack = customerAdapterCallBack;
        deleteItem = new SparseBooleanArray();

    }

    @Override
    public int getCount() {
        return supplierObjectArrayList.size();
    }

    @Override
    public SupplierObject getItem(int i) {
        return supplierObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(this.context, R.layout.supplier_price_list_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final SupplierObject object = getItem(i);
        viewHolder.name.setText(object.getName());
        viewHolder.address.setText(object.getAddress());
        viewHolder.price.setText(String.format("RM%s ", object.getPrice()));
        viewHolder.unit.setText(object.getUnit());
        /*
         * delete
         * */
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerAdapterCallBack.deleteConfirmation(false, i, null);
            }
        });

        viewHolder.parent_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerAdapterCallBack.editSupplierPriceList(supplierObjectArrayList.get(i));
            }
        });

        //delete  purpose
        if (deleteItem.size() > 0 && deleteItem.get(i)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.selected_color));
        } else view.setBackgroundColor(context.getResources().getColor(R.color.white));

        return view;
    }
    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder {
        private RelativeLayout parent_view;
        private TextView name, address, price, unit;
        private ImageView delete;

        ViewHolder(View view) {
            parent_view = view.findViewById(R.id.parent_view);
            name = view.findViewById(R.id.customer_dialog_list_view_item_name);
            address = view.findViewById(R.id.customer_dialog_list_view_item_address);
            price = view.findViewById(R.id.price);
            unit = view.findViewById(R.id.unit);
            delete = view.findViewById(R.id.delete_view);
        }
    }

    /*-------------------------------------------------------delete purpose--------------------------------------------------------------*/
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

    public interface SupplierAdapterCallBack {
        void editSupplierPriceList(SupplierObject object);
        void deleteConfirmation(boolean deleteProduct, int position, String deleteId);
    }
}
