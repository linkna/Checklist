package dhbw.studienarbeit.leavethehouse_checklist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends BaseActivity {

    private TextView firstName;
    private TextView lastName;
    private FirebaseUser currentUser;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        repository = Repository.getInstance();


        firstName = findViewById(R.id.firstNameTextView);
        lastName = findViewById(R.id.lastNameTextView);
        TextView email = findViewById(R.id.emailTextView);

        Button changeEmailBtn = findViewById(R.id.emailChangeButton);
        Button changePasswordBtn = findViewById(R.id.passwordChangeButton);
        Button deleteProfileBtn = findViewById(R.id.accountDeleteButton);

        changeEmailBtn.setTextColor(getColor(R.color.white));
        changePasswordBtn.setTextColor(getColor(R.color.white));
        deleteProfileBtn.setTextColor(getColor(R.color.white));

        String uid = repository.getUid();

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