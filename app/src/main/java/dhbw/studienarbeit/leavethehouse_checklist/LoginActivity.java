package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private Button logButton;

    public FirebaseAuth auth;
    private FirebaseFirestore mDatabase;

    private static final String TAG = "LoginActivity";
    private Set<String> allTasksInSelectedList;
    private Set<String> checkedTaskPositions;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();


        repository = Repository.getInstance();


        // check if user is signed in. getCurrentUser() will be null if not signed in
        if (auth.getCurrentUser() != null) {

            // get shared preferences and check for last actions

//            Log.d(TAG, "User: " + auth.getCurrentUser().getEmail());
            SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);
////
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
        logButton = (Button) findViewById(R.id.loginButton);


        logButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, LoginActivity.class)));

        logButton.setOnClickListener(v -> {
            String email = inputEmail.getText().toString();
            final String password = inputPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                return;
            }

            //authenticate user
            auth.signInWithEmailAndPassword(email, password)
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
                            Intent intent = new Intent(LoginActivity.this, ChecklistOverviewActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        });
    }


    public void signUpLabelClick(View view) {
//        setContentView(R.layout.activity_sign_up);
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    }

    public void forgotPasswordLabelClick(View view) {

    }

    public void onBackPressed () {
        finish();
    }
}