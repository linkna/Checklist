package dhbw.studienarbeit.leavethehouse_checklist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends BaseActivity {

    private TextView firstName;
    private TextView lastName;
    private FirebaseAuth auth;
    private TextView email;
    private FirebaseUser currentUser;
    private String uid;
    private FirebaseFirestore mDatabase;
    private Repository repository;
    private Button changeEmailBtn;
    private Button changePasswordBtn;
    private Button deleteProfileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        repository = Repository.getInstance();


        firstName = findViewById(R.id.firstNameTextView);
        lastName = findViewById(R.id.lastNameTextView);
        email = findViewById(R.id.emailTextView);

        changeEmailBtn = findViewById(R.id.emailChangeButton);
        changePasswordBtn = findViewById(R.id.passwordChangeButton);
        deleteProfileBtn = findViewById(R.id.accountDeleteButton);

        changeEmailBtn.setTextColor(getColor(R.color.white));
        changePasswordBtn.setTextColor(getColor(R.color.white));
        deleteProfileBtn.setTextColor(getColor(R.color.white));

        currentUser = auth.getCurrentUser();
        uid = repository.getUid();

        email.setText(repository.getCurrentUser().getEmail());

        mDatabase.collection("User").document(uid).get().addOnCompleteListener(
                task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    repository.setUserDocumentSnapshot(documentSnapshot);
                    firstName.setText(documentSnapshot.getString("firstname"));
                    lastName.setText(documentSnapshot.getString("lastname"));
                }
        );


    }
}