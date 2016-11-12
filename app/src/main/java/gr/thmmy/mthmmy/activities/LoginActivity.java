package gr.thmmy.mthmmy.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private EditText inputUsername;
    private EditText inputPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnGuest = (Button) findViewById(R.id.btnContinueAsGuest);

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String username = inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!username.isEmpty() && !password.isEmpty()) {
                    // login user
                    try {
                        switch(new loginAsync().execute(username,password).get()){
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
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                        }
                        if(new loginAsync().execute(username,password).get() == OK){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnGuest.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                //TO-DO
            }
        });

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
}

