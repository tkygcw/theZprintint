package com.jby.thezprinting.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.CustomerObject;

import java.util.ArrayList;


public class CustomerAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<CustomerObject> customerObjectArrayList;
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
    private  CustomerAdapterCallBack customerAdapterCallBack;

    public CustomerAdapter(Context context, ArrayList<CustomerObject> customerObjectArrayList, boolean isNormalList, CustomerAdapterCallBack customerAdapterCallBack) {
        this.context = context;
        this.customerObjectArrayList = customerObjectArrayList;
        this.isNormalList = isNormalList;
        this.customerAdapterCallBack = customerAdapterCallBack;
        deleteItem = new SparseBooleanArray();

    }

    @Override
    public int getCount() {
        return customerObjectArrayList.size();
    }

    @Override
    public CustomerObject getItem(int i) {
        return customerObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(this.context, R.layout.customer_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final CustomerObject object = getItem(i);
        viewHolder.name.setText(object.getName());
        viewHolder.address.setText(object.getAddress());
        //delete  purpose
        if (deleteItem.size() > 0 && deleteItem.get(i)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.selected_color));
        } else view.setBackgroundColor(context.getResources().getColor(R.color.white));

        /*
        * set edit visibility
        * */
        viewHolder.edit.setVisibility(isNormalList ? View.VISIBLE : View.GONE);
        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerAdapterCallBack.editCustomer(object);
            }
        });
        return view;
    }
    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder {
        private TextView name, address;
        private ImageView edit;

        ViewHolder(View view) {
            name = view.findViewById(R.id.customer_dialog_list_view_item_name);
            address = view.findViewById(R.id.customer_dialog_list_view_item_address);
            edit = view.findViewById(R.id.edit);
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

    public interface CustomerAdapterCallBack{
        void editCustomer(CustomerObject object);
    }

}
