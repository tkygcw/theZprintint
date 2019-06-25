package com.jby.thezprinting.others;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnection {

    Context context;
    public NetworkConnection(Context context){
        this.context = context;
    }

    public boolean checkNetworkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return (networkInfo !=  null && networkInfo.isConnected());
    }
}
