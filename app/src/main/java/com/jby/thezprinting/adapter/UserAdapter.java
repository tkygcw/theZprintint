package com.jby.thezprinting.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jby.thezprinting.R;
import com.jby.thezprinting.object.UserObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.List;

public class UserAdapter extends ArrayAdapter<UserObject> {
    private Context context;
    private int resourceId;
    private List<UserObject> items, tempItems, suggestions;

    public UserAdapter(@NonNull Context context, int resourceId, ArrayList<UserObject> items) {
        super(context, resourceId, items);
        this.items = items;
        this.context = context;
        this.resourceId = resourceId;
        tempItems = new ArrayList<>(items);
        suggestions = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(resourceId, parent, false);
            }
            UserObject userObject = getItem(position);
            TextView username = view.findViewById(R.id.username);
            TextView company = view.findViewById(R.id.company);
            ImageView logo = view.findViewById(R.id.logo);

            Picasso.get().load(userObject.getLogo()).into(logo);
            username.setText(userObject.getUsername());
            company.setText(userObject.getCompany());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Nullable
    @Override
    public UserObject getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            UserObject user = (UserObject) resultValue;
            return user.getUsername();
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence != null) {
                suggestions.clear();
                for (UserObject user : tempItems) {
                    if (user.getUsername().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        suggestions.add(user);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ArrayList<UserObject> tempValues = (ArrayList<UserObject>) filterResults.values;
            if (filterResults.count > 0) {
                clear();
                for (UserObject userObj : tempValues) {
                    add(userObj);
                }
                notifyDataSetChanged();
            } else {
                clear();
                notifyDataSetChanged();
            }
        }
    };
}
