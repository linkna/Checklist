package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    public FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        repository= Repository.getInstance();


        // check if user is signed in. getCurrentUser() will be null if not signed in
        if (mAuth.getCurrentUser() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            repository.setCurrentUser(currentUser);
            repository.setUid(currentUser.getUid());
            // get shared preferences and check for last actions
            SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);
            if (sharedPreferences != null && !sharedPreferences.getAll().isEmpty()) {
                Intent intent = new Intent(LoginActivity.this, LastActionActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(LoginActivity.this, ChecklistOverviewActivity.class);
                startActivity(intent);
                finish();
            }
        }

        inputEmail = (EditText) findViewById(R.id.emailEditText);
        inputPassword = (EditText) findViewById(R.id.passwordEditText);
        Button logButton = (Button) findViewById(R.id.loginButton);


        logButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, LoginActivity.class)));

        logButton.setOnClickListener(v -> {
            String email = inputEmail.getText().toString();
            final String password = inputPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), getString(R.string.enterEmail), Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), getString(R.string.enterPassword), Toast.LENGTH_SHORT).show();
                return;
            }

            //authenticate user
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                inputPassword.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            repository.setCurrentUser(currentUser);
                            repository.setUid(currentUser.getUid());
                            startActivity(new Intent(LoginActivity.this, ChecklistOverviewActivity.class));
                            finish();
                        }
                    });
        });
    }


    public void signUpLabelClick(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    }

    public void forgotPasswordLabelClick(View view) {
        inputEmail = (EditText) findViewById(R.id.emailEditText);
        String emailAddress = inputEmail.getText().toString();
        if(emailAddress.isEmpty()){
            inputEmail.setError(getString(R.string.error_email_not_valid));
        }else {
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(LoginActivity.this, getString(R.string.error_reset_password), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void onBackPressed () {
        finish();
    }
}