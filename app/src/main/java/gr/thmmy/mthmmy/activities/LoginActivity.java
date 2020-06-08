package gr.thmmy.mthmmy.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.main.MainActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;

import static gr.thmmy.mthmmy.session.SessionManager.BANNED_USER;
import static gr.thmmy.mthmmy.session.SessionManager.CONNECTION_ERROR;
import static gr.thmmy.mthmmy.session.SessionManager.EXCEPTION;
import static gr.thmmy.mthmmy.session.SessionManager.FAILURE;
import static gr.thmmy.mthmmy.session.SessionManager.SUCCESS;
import static gr.thmmy.mthmmy.session.SessionManager.WRONG_PASSWORD;
import static gr.thmmy.mthmmy.session.SessionManager.WRONG_USER;

public class LoginActivity extends BaseActivity {

    //-----------------------------------------CLASS VARIABLES------------------------------------------
    /* --Graphics-- */
    private AppCompatButton btnLogin;
    private EditText inputUsername;
    private EditText inputPassword;
    private ImageView logo;
    private String username;
    private String password;
    /* --Graphics End-- */

    private LoginTask loginTask;
    private boolean initialRedirect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialRedirect = getIntent().getBooleanExtra("REDIRECT", false);

        PreferenceManager.setDefaultValues(this, R.xml.app_preferences_user, false);

        //Variables initialization
        inputUsername = findViewById(R.id.username);
        inputPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        AppCompatButton btnGuest = findViewById(R.id.btnContinueAsGuest);
        logo = findViewById(R.id.logo);

        Glide.with(this).load(R.drawable.logo_animated).into(logo);

        //Login button Click Event
        btnLogin.setOnClickListener(view -> {

            //Get username and password strings
            username = inputUsername.getText().toString().trim();
            password = inputPassword.getText().toString().trim();

            //Check for empty data in the form
            if (!validate()) {
                onLoginFailed();
                return;
            }

            //Login user
            loginTask = new LoginTask();
            loginTask.execute(username, password);
        });

        //Guest Button Action
        btnGuest.setOnClickListener(view -> {
            //Session data update
            sessionManager.guestLogin();

            //Go to main
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (loginTask != null && loginTask.getStatus() == AsyncTask.Status.RUNNING) {
            loginTask.cancel(true);
        }
        if(!isTaskRoot())
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        btnLogin.setEnabled(true);
    }

    private boolean validate() {
        //Handle empty text fields
        boolean valid = true;

        if (username.isEmpty()) {
            inputUsername.setError("Enter a valid username");
            inputUsername.requestFocus();
            valid = false;
        } else {
            inputUsername.setError(null);
        }

        if (password.isEmpty()) {
            inputPassword.setError("Enter a valid password", null);
            if (valid)
                inputPassword.requestFocus();
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }

    //--------------------------------------------LOGIN-------------------------------------------------
    private class LoginTask extends AsyncTask<String, Void, Integer> {
        //Class variables
        private LinearLayout spinner;
        private ScrollView loginContent;

        @Override
        protected Integer doInBackground(String... params) {
            return sessionManager.login(params[0], params[1]);
        }

        @Override
        protected void onPreExecute() { //Show a progress dialog until done
            btnLogin.setEnabled(false); //Login button shouldn't be pressed during this

            spinner = findViewById(R.id.login_progress_bar);
            loginContent = findViewById(R.id.inner_scroll_view);

            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            loginContent.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);
        }


        @Override
        protected void onPostExecute(Integer result) { //Handle attempt result
            switch (result) {
                case SUCCESS: //Successful login
                    Toast.makeText(getApplicationContext(),
                            "Welcome, " + sessionManager.getUsername() + "!", Toast.LENGTH_LONG)
                            .show();
                    BaseApplication.getInstance().logFirebaseAnalyticsEvent(FirebaseAnalytics.Event.LOGIN, null);
                    if(initialRedirect){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else
                        onBackPressed();

                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    break;
                case WRONG_USER:
                    Toast.makeText(getApplicationContext(),
                            "Wrong username!", Toast.LENGTH_LONG).show();
                    inputUsername.requestFocus();
                    break;
                case WRONG_PASSWORD:
                    Toast.makeText(getApplicationContext(),
                            "Wrong password!", Toast.LENGTH_LONG).show();
                    inputPassword.requestFocus();
                    break;
                case BANNED_USER:
                    Toast.makeText(getApplicationContext(),
                            "You are banned!", Toast.LENGTH_LONG).show();
                    inputPassword.requestFocus();
                    break;
                case FAILURE:
                    Toast.makeText(getApplicationContext(),
                            "Login failed...", Toast.LENGTH_LONG).show();
                    break;
                case CONNECTION_ERROR:
                    Toast.makeText(getApplicationContext(),
                            "Connection Error", Toast.LENGTH_LONG).show();
                    break;
                case EXCEPTION:
                    Toast.makeText(getApplicationContext(),
                            "Error", Toast.LENGTH_LONG).show();
                    break;

            }
            //Login failed
            btnLogin.setEnabled(true); //Re-enable login button

            loginContent.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            btnLogin.setEnabled(true); //Re-enable login button
            loginContent.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.INVISIBLE);
        }

    }
//---------------------------------------LOGIN ENDS-------------------------------------------------
}