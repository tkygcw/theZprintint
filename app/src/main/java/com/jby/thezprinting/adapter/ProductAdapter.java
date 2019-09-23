package com.jby.thezprinting.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.ProductObject;

import java.util.ArrayList;

public class ProductAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ProductObject> productObjectArrayList;
    /*
     * call back
     * */
    private ProductAdapterCallBack productAdapterCallBack;

    public ProductAdapter(Context context, ArrayList<ProductObject> productObjectArrayList, ProductAdapterCallBack productAdapterCallBack) {
        this.context = context;
        this.productObjectArrayList = productObjectArrayList;
        this.productAdapterCallBack = productAdapterCallBack;
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
            convertView = layoutInflater.inflate(R.layout.product_list_view_parent_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        } else
            groupViewHolder = (GroupViewHolder) convertView.getTag();

        final ProductObject object = getGroup(groupPosition);
        groupViewHolder.category.setText(object.getCategory());
        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ProductObject getGroup(int i) {
        return productObjectArrayList.get(i);
    }

    private static class GroupViewHolder {
        private TextView category;

        GroupViewHolder(View view) {
            category = view.findViewById(R.id.category);
        }
    }

    @Override
    public int getGroupCount() {
        return productObjectArrayList.size();
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
            view = layoutInflater.inflate(R.layout.product_child_list_view_item, null);
            viewHolder = new ChildViewHolder(view);
            view.setTag(viewHolder);
        } else
            viewHolder = (ChildViewHolder) view.getTag();

        try {
            final ProductObject object = getChild(groupPosition, childPosition);
            viewHolder.name.setText(object.getName());
            viewHolder.price.setText(String.format("RM %s", object.getPrice()));
            viewHolder.unit.setText(object.getUnit());

            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    productAdapterCallBack.updateProduct(productObjectArrayList.get(groupPosition), productObjectArrayList.get(groupPosition).getChildProductObjectArrayList().get(childPosition));
                }
            });

            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    productAdapterCallBack.deleteConfirmation(true, -1, productObjectArrayList.get(groupPosition).getChildProductObjectArrayList().get(childPosition).getProduct_id());
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public int getChildrenCount(int i) {
        try {
            return productObjectArrayList.get(i).getChildProductObjectArrayList().size();
        } catch (NullPointerException e) {
            Toast.makeText(context, "No Item in this category!", Toast.LENGTH_SHORT).show();
            return -1;
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override
    public ProductObject getChild(int groupPosition, int childPosition) {
        try {
            return productObjectArrayList.get(groupPosition).getChildProductObjectArrayList().get(childPosition);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ChildViewHolder {
        private TextView name, price, unit;
        private LinearLayout parentLayout;
        private ImageView delete;

        ChildViewHolder(View view) {
            parentLayout = view.findViewById(R.id.parent_view);
            delete = view.findViewById(R.id.delete_view);
            name = view.findViewById(R.id.name);
            price = view.findViewById(R.id.price);
            unit = view.findViewById(R.id.unit);
        }
    }

    public interface ProductAdapterCallBack {
        void updateProduct(ProductObject parentObject, ProductObject childObject);

        void deleteConfirmation(boolean deleteProduct, int position, String id);
    }
}
