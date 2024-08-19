package com.jby.thezprinting.registration;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.jby.thezprinting.HomeActivity;
import com.jby.thezprinting.R;
import com.jby.thezprinting.adapter.UserAdapter;
import com.jby.thezprinting.database.CustomSqliteHelper;
import com.jby.thezprinting.database.FrameworkClass;
import com.jby.thezprinting.database.ResultCallBack;
import com.jby.thezprinting.object.UserObject;
import com.jby.thezprinting.shareObject.ApiDataObject;
import com.jby.thezprinting.shareObject.ApiManager;
import com.jby.thezprinting.shareObject.AsyncTaskManager;
import com.jby.thezprinting.shareObject.NetworkConnection;
import com.jby.thezprinting.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.thezprinting.database.CustomSqliteHelper.TB_USER;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ResultCallBack, AdapterView.OnItemClickListener {
    private AutoCompleteTextView loginActivityAutoUsername;
    private EditText loginActivityPassword;
    private TextView loginActivityForgotPassword, loginActivityVersion;
    private ImageView loginActivityShowPassword, loginActivityCancelUsername;
    private LinearLayout loginActivityMainLayout;
    private ProgressBar loginActivityProgressBar;

    private boolean show = true;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    private ArrayList<UserObject> userObjectArrayList;
    private UserAdapter userAdapter;

    private FrameworkClass frameworkClass;
    private String readType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isLogin();
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        loginActivityAutoUsername = findViewById(R.id.activity_login_auto_complete_username);
        loginActivityPassword = findViewById(R.id.activity_login_password);

        loginActivityForgotPassword = findViewById(R.id.activity_login_forgot_password);
        loginActivityVersion = findViewById(R.id.activity_login_version_name);

        loginActivityShowPassword = findViewById(R.id.activity_login_show_password);
        loginActivityCancelUsername = findViewById(R.id.activity_login_cancel_username);

        loginActivityMainLayout = findViewById(R.id.activity_login_parent_layout);
        loginActivityProgressBar = findViewById(R.id.login_activity_progress_bar);

        userObjectArrayList = new ArrayList<>();

        frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_USER);
        handler = new Handler();

    }

    private void objectSetting() {
        /*
         * full screen
         * */
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        loginActivityShowPassword.setOnClickListener(this);
        loginActivityCancelUsername.setOnClickListener(this);
        loginActivityAutoUsername.setOnItemClickListener(this);
        displayVersion();
        /*
         * for auto complete purpose
         * */
        readUser();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_login_show_password:
                showPasswordSetting();
                break;
            case R.id.activity_login_cancel_username:
                loginActivityAutoUsername.setText("");
                break;
        }
    }

    //    show/ hide password setting
    private void showPasswordSetting() {
        if (show) {
            loginActivityShowPassword.setImageDrawable(getResources().getDrawable(R.drawable.activity_login_hide_icon));
            loginActivityPassword.setTransformationMethod(null);
            show = false;
        } else {
            loginActivityShowPassword.setImageDrawable(getResources().getDrawable(R.drawable.activity_login_show_icon));
            loginActivityPassword.setTransformationMethod(new PasswordTransformationMethod());
            show = true;
        }
    }

    //    sign in setting
    public void checking(View v) {
        loginActivityProgressBar.setVisibility(View.VISIBLE);
        final String username = loginActivityAutoUsername.getText().toString().trim();
        final String password = loginActivityPassword.getText().toString().trim();
        closeKeyBoard();

        if (new NetworkConnection(this).checkNetworkConnection()) {
            if (!username.equals("") && !password.equals("")) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        signIn(username, password);
                    }
                }, 200);
            } else {
                showSnackBar("Invalid username or password!");
                loginActivityProgressBar.setVisibility(View.GONE);
            }

        } else {
            showSnackBar("No Internet connection!");
            loginActivityProgressBar.setVisibility(View.GONE);
        }
    }

    public void closeKeyBoard() {
        View view = getCurrentFocus();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void signIn(String username, String password) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("password", password));
        apiDataObjectArrayList.add(new ApiDataObject("username", username));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().registration,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
//                        setup user detail
                        whenLoginSuccessful(jsonObjectLoginResponse);

                    } else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        showSnackBar("Invalid email or password");
                        loginActivityProgressBar.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        loginActivityProgressBar.setVisibility(View.GONE);
    }

    //    snackBar setting
    private void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(loginActivityMainLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void whenLoginSuccessful(JSONObject jsonObject) {
        try {
            checkLocalUserAvailability(jsonObject);
            SharedPreferenceManager.setCompanyId(this, jsonObject.getString("company_id"));
            SharedPreferenceManager.setUserId(this, jsonObject.getString("user_id"));
            //intent
            startActivity(new Intent(this, HomeActivity.class));
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void isLogin() {
        Stetho.initializeWithDefaults(this);
        if (!SharedPreferenceManager.getCompanyId(this).equals("default") && !SharedPreferenceManager.getUserId(this).equals("default")) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void displayVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Version " + pInfo.versionName;
            loginActivityVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------local user account purpose-------------------------------------------------------------------------
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        loginActivityPassword.setText(userObjectArrayList.get(i).getPassword());
    }

    private void readUser() {
        frameworkClass.new Read("*").perform();
    }

    /*
     * check user
     * */
    private void checkLocalUserAvailability(JSONObject jsonObject) throws JSONException {
        int count = frameworkClass.new Read("username").where("username = '" + loginActivityAutoUsername.getText().toString() + "'").count();
        /*
         * update user if existed
         * */
        if (count > 0) {
            frameworkClass.new Update("username, company, logo, password",
                    jsonObject.getString("username") + "," +
                            jsonObject.getString("company") + "," +
                            jsonObject.getString("logo") + "," +
                            loginActivityPassword.getText().toString())
                    .where("username = ?", loginActivityAutoUsername.getText().toString())
                    .perform();
        }
        /*
         * create new user
         * */
        else {
            storeUserDetail(jsonObject);
        }
    }

    private void storeUserDetail(JSONObject jsonObject) throws JSONException {
        frameworkClass.new create("username, company, logo, password",
                new String[]{
                        jsonObject.getString("username"),
                        jsonObject.getString("company"),
                        jsonObject.getString("logo"),
                        loginActivityPassword.getText().toString()
                }).perform();
    }


    @Override
    public void createResult(String status) {
    }

    @Override
    public void readResult(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                userObjectArrayList.add(new UserObject(
                                jsonArray.getJSONObject(i).getString("username"),
                                jsonArray.getJSONObject(i).getString("password"),
                                jsonArray.getJSONObject(i).getString("company"),
                                jsonArray.getJSONObject(i).getString("logo")
                        )
                );
            }
            loginActivityAutoUsername.setThreshold(1);
            userAdapter = new UserAdapter(this, R.layout.user_list_view_item, userObjectArrayList);
            loginActivityAutoUsername.setAdapter(userAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }
}
