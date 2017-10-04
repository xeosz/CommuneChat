package my.edu.tarc.kusm_wa14student.communechat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class LoginActivity extends AppCompatActivity {

    //Views
    EditText etPassword;
    AutoCompleteTextView etLogin;
    Button btnLogin;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize view
        etPassword = (EditText) findViewById(R.id.editText_password);
        etLogin = (AutoCompleteTextView) findViewById(R.id.editText_login);
        btnLogin = (Button) findViewById(R.id.button_login);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_login);
        progressBar.setVisibility(View.INVISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private class AuthenicationTask extends AsyncTask<Void, Void, Boolean> {

        String username;
        String password;

        private AuthenicationTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean result = false;


            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressBar.setVisibility(View.GONE);
        }
    }

}
