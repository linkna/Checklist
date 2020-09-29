package dhbw.studienarbeit.leavethehouse_checklist;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();


        firstName = findViewById(R.id.firstNameTextView);
        lastName = findViewById(R.id.lastNameTextView);
        email = findViewById(R.id.emailTextView);

        currentUser = auth.getCurrentUser();
        uid = currentUser.getUid();

        email.setText(currentUser.getEmail());

        mDatabase.collection("User").document(uid).get().addOnCompleteListener(
                task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    firstName.setText(documentSnapshot.getString("firstname"));
                    lastName.setText(documentSnapshot.getString("lastname"));
                }
        );


    }
}