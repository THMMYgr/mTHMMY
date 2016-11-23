package gr.thmmy.mthmmy.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import gr.thmmy.mthmmy.R;

public class LoginActivity extends BaseActivity {

//-----------------------------------------CLASS VARIABLES------------------------------------------
    /* --Graphics-- */
    private Button btnLogin;
    private EditText inputUsername;
    private EditText inputPassword;
    private String username;
    private String password;
    /* --Graphics End-- */

    //Other variables
    private static final String TAG = "LoginActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Variables initialization
        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnGuest = (Button) findViewById(R.id.btnContinueAsGuest);

        //Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "Login");

                //Get username and password strings
                username = inputUsername.getText().toString().trim();
                password = inputPassword.getText().toString().trim();

                //Check for empty data in the form
                if (!validate()) {
                    onLoginFailed();
                    return;
                }

                //Login user
                new LoginTask().execute(username, password);
            }
        });

        //Guest Button Action
        btnGuest.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                //Session data update
                _prefs.edit().putString(USER_NAME, GUEST_PREF_USERNAME).apply();
                _prefs.edit().putInt(LOG_STATUS, LOGGED_IN).apply();

                //Go to main
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
        //Handle empty text fields
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

//--------------------------------------------LOGIN-------------------------------------------------
    private class LoginTask extends AsyncTask<String, Void, Integer> {
        //Class variables
        ProgressDialog progressDialog;

        @Override
        protected Integer doInBackground(String... params) {
            Thmmy.login(params[0], params[1], "-1"); //Attempt login
            return _prefs.getInt(LOG_STATUS, OTHER_ERROR);
        }

        @Override
        protected void onPreExecute() { //Show a progress dialog until done
            btnLogin.setEnabled(false); //Login button shouldn't be pressed during this

            progressDialog = new ProgressDialog(LoginActivity.this,
                    R.style.AppTheme);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
        }


        @Override
        protected void onPostExecute(Integer result) { //Handle attempt result
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
                case LOGGED_IN: //Successful login
                    Toast.makeText(getApplicationContext(),
                            "Login successful!", Toast.LENGTH_LONG)
                            .show();
                    //Go to main
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    break;
            }
            //Login failed
            btnLogin.setEnabled(true); //Re-enable login button
            progressDialog.dismiss(); //Hide progress dialog
        }
    }
//---------------------------------------LOGIN ENDS-------------------------------------------------
}

