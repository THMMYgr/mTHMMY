package gr.thmmy.mthmmy.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import gr.thmmy.mthmmy.R;

import static gr.thmmy.mthmmy.utils.Thmmy.CERTIFICATE_ERROR;
import static gr.thmmy.mthmmy.utils.Thmmy.FAILED;
import static gr.thmmy.mthmmy.utils.Thmmy.LOGGED_IN;
import static gr.thmmy.mthmmy.utils.Thmmy.OTHER_ERROR;
import static gr.thmmy.mthmmy.utils.Thmmy.WRONG_PASSWORD;
import static gr.thmmy.mthmmy.utils.Thmmy.WRONG_USER;
import static gr.thmmy.mthmmy.utils.Thmmy.login;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private Button btnLogin;
    private EditText inputUsername;
    private EditText inputPassword;
    private String username;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnGuest = (Button) findViewById(R.id.btnContinueAsGuest);

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "Login");

                username = inputUsername.getText().toString().trim();
                password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!validate()) {
                    onLoginFailed();
                    return;
                }

                // login user
                new LoginTask().execute(username, password);
            }
        });

        // Guest Button Action
        btnGuest.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(USER_NAME, GUEST_PREF_USERNAME);
                editor.putBoolean(IS_LOGGED_IN, true);
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        btnLogin.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        if (username.isEmpty()) {
            inputUsername.setError("Enter a valid username");
            valid = false;
        } else {
            inputUsername.setError(null);
        }

        if (password.isEmpty()) {
            inputPassword.setError("Enter a valid password");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }

    private class LoginTask extends AsyncTask<String, Void, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected Integer doInBackground(String... params) {
            setLoginData(login(params[0], params[1], "-1"));
            return loginData.getStatus();
        }

        @Override
        protected void onPreExecute() {
            btnLogin.setEnabled(false);

            progressDialog = new ProgressDialog(LoginActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
        }


        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case WRONG_USER:
                    Toast.makeText(getApplicationContext(),
                            "Wrong username!", Toast.LENGTH_LONG).show();
                    break;
                case WRONG_PASSWORD:
                    Toast.makeText(getApplicationContext(),
                            "Wrong password!", Toast.LENGTH_LONG)
                            .show();
                    break;
                case FAILED:
                    Toast.makeText(getApplicationContext(),
                            "Check your connection!", Toast.LENGTH_LONG)
                            .show();
                    break;
                case CERTIFICATE_ERROR:
                    Toast.makeText(getApplicationContext(),
                            "Certificate error!", Toast.LENGTH_LONG)
                            .show();
                    break;
                case OTHER_ERROR:
                    Toast.makeText(getApplicationContext(),
                            "Check your connection!", Toast.LENGTH_LONG)
                            .show();
                    break;
                case LOGGED_IN:
                    SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(USER_NAME, username);
                    editor.putBoolean(IS_LOGGED_IN, true);
                    editor.apply();

                    Toast.makeText(getApplicationContext(),
                            "Login successful!", Toast.LENGTH_LONG)
                            .show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    break;
            }
            btnLogin.setEnabled(true);
            progressDialog.dismiss();
        }
    }
}

