package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    public EditText inputFirstName, inputLastName, inputEmail, inputPassword;
    public Button signUpButton;

    public int test = 0;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    Map<String, Object> userInput = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inputFirstName = (EditText) findViewById(R.id.firstNameEditText);
                inputLastName = (EditText) findViewById(R.id.lastNameEditText);
                inputEmail = (EditText) findViewById(R.id.emailEditText);
                inputPassword = (EditText) findViewById(R.id.passwordEditText);

                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();
                String firstname = inputFirstName.getText().toString();
                String lastname = inputLastName.getText().toString();

                if (validateInput(email, password, firstname,lastname)) {

                    userInput.put("firstname", inputFirstName.getText().toString());
                    userInput.put("lastname", inputLastName.getText().toString());

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    // Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        //Toast.makeText(SignUpActivity.this, "Registrierung fehlgeschlagen.", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(SignUpActivity.this, "Registrierung fehlgeschlagen. " + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Registrierung erfolgreich.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignUpActivity.this, ChecklistOverviewActivity.class));

                                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (currentUser == null) throw new AssertionError();
                                        String uid = currentUser.getUid();
                                        //save data to database. Table: User: document generated with current users uid.
                                        mDatabase.collection("User").document(uid).set(userInput);
                                        finish();
                                    }
                                }

                            });
                }
            }
        });

    }

    private boolean validateInput(String email, String password, String firstname, String lastname) {

        boolean isInputValid = true;

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Email-Adresse eingeben");
            isInputValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Passwort eingeben");
            isInputValid = false;
        }

        if (password.length() < 6) {
            inputPassword.setError("Password zu kurz. Mindestlänge 6 Zeichen");
            isInputValid = false;
        }
        if (!isEmailValid(inputEmail.getText().toString())) {
            inputEmail.setError("Bitte gültiges Email-Adressen-Format eingeben");
            isInputValid = false;
        }
        if (TextUtils.isEmpty(firstname)) {
            inputFirstName.setError("Vorname eingeben");
            isInputValid = false;
        }
        if (TextUtils.isEmpty(lastname)) {
            inputLastName.setError("Nachname eingeben");
            isInputValid = false;
        }

        return isInputValid;
    }

    boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void onBackPressed () {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}