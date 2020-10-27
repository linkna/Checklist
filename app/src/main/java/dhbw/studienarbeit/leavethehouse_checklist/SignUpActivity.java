package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    public EditText inputFirstName, inputLastName, inputEmail, inputPassword;
    public Button signUpButton;
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

        signUpButton.setOnClickListener((View.OnClickListener) v -> {
            inputFirstName = (EditText) findViewById(R.id.firstNameEditText);
            inputLastName = (EditText) findViewById(R.id.lastNameEditText);
            inputEmail = (EditText) findViewById(R.id.emailEditText);
            inputPassword = (EditText) findViewById(R.id.passwordEditText);

            String email = inputEmail.getText().toString();
            final String password = inputPassword.getText().toString();
            String firstname = inputFirstName.getText().toString();
            String lastname = inputLastName.getText().toString();

            if (validateInput(email, password, firstname,lastname)) {

                userInput.put("firstname", firstname);
                userInput.put("lastname", lastname);
                userInput.put("email", email.toLowerCase());

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {

                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser == null) throw new AssertionError();
                                String uid = currentUser.getUid();
                                mDatabase.collection("User").document(uid).set(userInput);

                                Map<String, Object> data = new HashMap<>();
                                List<String> template = new ArrayList<>();
                                template.add("templateHome");
                                template.add( "templateOffice");
                                data.put("exist", true);
                                data.put("checklistID", template);
                                mDatabase.collection("SharedLists").document(email).set(data);

                                Toast.makeText(SignUpActivity.this, getString(R.string.successfull_signed_up), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                //startActivity(new Intent(SignUpActivity.this, ChecklistOverviewActivity.class));
                                finish();
                            }
                        });
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