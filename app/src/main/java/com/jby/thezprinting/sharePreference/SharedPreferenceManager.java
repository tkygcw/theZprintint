package com.jby.thezprinting.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {

    private static String CompanyId = "company_id";
    private static String UserId = "user_id";

    private static SharedPreferences getSharedPreferences(Context context) {
        String SharedPreferenceFileName = "VegeApp";
        return context.getSharedPreferences(SharedPreferenceFileName, Context.MODE_PRIVATE);
    }

    public static void clear(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

    public static String getCompanyId(Context context) {
        return getSharedPreferences(context).getString(CompanyId, "default");
    }

    public static void setCompanyId(Context context, String companyId) {
        getSharedPreferences(context).edit().putString(CompanyId, companyId).apply();
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(UserId, "default");
    }

    public static void setUserId(Context context, String userId) {
        getSharedPreferences(context).edit().putString(UserId, userId).apply();
    }
}
