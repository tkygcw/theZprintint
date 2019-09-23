package com.jby.thezprinting.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 3/11/2018.
 */

public class CustomSqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 2;

    public static final String TB_DEFAULT_CUSTOMER = "tb_default_customer";
    public static final String TB_DEFAULT_SUPPLIER = "tb_default_supplier";

    private static final String CREATE_TB_DEFAULT_CUSTOMER = "CREATE TABLE "+ TB_DEFAULT_CUSTOMER +
            "(customer_id Text," +
            "name Text," +
            "address Text," +
            "contact Text)";

    private static final String CREATE_TB_DEFAULT_SUPPLIER = "CREATE TABLE "+ TB_DEFAULT_SUPPLIER +
            "(supplier_id Text," +
            "name Text," +
            "address Text," +
            "email Text," +
            "contact Text," +
            "website Text)";


   public CustomSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TB_DEFAULT_CUSTOMER);
        sqLiteDatabase.execSQL(CREATE_TB_DEFAULT_SUPPLIER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_DEFAULT_CUSTOMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_DEFAULT_SUPPLIER);
        onCreate(sqLiteDatabase);
    }
}
