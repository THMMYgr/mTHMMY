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

import java.util.concurrent.ExecutionException;

import gr.thmmy.mthmmy.R;

import static gr.thmmy.mthmmy.utils.Thmmy.CERTIFICATE_ERROR;
import static gr.thmmy.mthmmy.utils.Thmmy.FAILED;
import static gr.thmmy.mthmmy.utils.Thmmy.OK;
import static gr.thmmy.mthmmy.utils.Thmmy.OTHER_ERROR;
import static gr.thmmy.mthmmy.utils.Thmmy.WRONG_PASSWORD;
import static gr.thmmy.mthmmy.utils.Thmmy.WRONG_USER;
import static gr.thmmy.mthmmy.utils.Thmmy.authenticate;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private EditText inputUsername;
    private EditText inputPassword;
    Button btnLogin;
    Button btnGuest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGuest = (Button) findViewById(R.id.btnContinueAsGuest);

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "Login");

                // Check for empty data in the form
                if (!validate()) {
                    onLoginFailed();
                    return;
                }

                btnLogin.setEnabled(false);

                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();

                String username = inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                // login user
                try {
                    switch (new loginAsync().execute(username, password).get()) {
                        case WRONG_USER:
                            Toast.makeText(getApplicationContext(),
                                    "Wrong username!", Toast.LENGTH_LONG)
                                    .show();
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
                        case OK:
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "Login successful!", Toast.LENGTH_LONG)
                                    .show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                btnLogin.setEnabled(true);
            }
        });

        // Link to Register Screen
        btnGuest.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                //TO-DO
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        btnLogin.setEnabled(true);
    }

    private class loginAsync extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            return authenticate(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Integer result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public boolean validate() {
        boolean valid = true;

        String email = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();

        if (email.isEmpty()) {
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
}

