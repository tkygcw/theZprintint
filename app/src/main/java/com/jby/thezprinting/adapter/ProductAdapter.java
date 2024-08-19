package com.jby.thezprinting.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.ProductObject;

import java.util.ArrayList;


public class ProductAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ProductObject> productObjectArrayList;
    /*
     ** for delete purpose
     */
    private SparseBooleanArray deleteItem;
    /*
     * type
     * */
    private boolean isNormalList;

    public ProductAdapter(Context context, ArrayList<ProductObject> productObjectArrayList, boolean isNormalList) {
        this.context = context;
        this.productObjectArrayList = productObjectArrayList;
        this.isNormalList = isNormalList;
        deleteItem = new SparseBooleanArray();

    }

    @Override
    public int getCount() {
        return productObjectArrayList.size();
    }

    @Override
    public ProductObject getItem(int i) {
        try {
            return productObjectArrayList.get(i);
        } catch (Exception e) {
            return new ProductObject("", "", "", "");
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(this.context, R.layout.product_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final ProductObject object = getItem(i);
        viewHolder.name.setText(object.getName());
        viewHolder.description.setText(object.getDescription());
        viewHolder.price.setText(String.format("RM %s", object.getPrice()));
        //delete  purpose
        if (deleteItem.size() > 0 && deleteItem.get(i)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.selected_color));
        } else view.setBackgroundColor(context.getResources().getColor(R.color.white));

        return view;
    }
    /*-------------------------------------------------------search purpose--------------------------------------------------------------*/

    private static class ViewHolder {
        private TextView name, description, price;

        ViewHolder(View view) {
            name = view.findViewById(R.id.product_dialog_list_view_item_name);
            description = view.findViewById(R.id.product_dialog_list_view_item_description);
            price = view.findViewById(R.id.product_dialog_list_view_item_price);
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

}
