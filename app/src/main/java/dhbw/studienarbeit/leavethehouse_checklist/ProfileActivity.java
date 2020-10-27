package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;


public class ProfileActivity extends BaseActivity {

    private Button changePasswordBtn, deleteProfileBtn;
    private TextView firstName;
    private TextView lastName;
    private Repository repository;
    private PopupWindow deletePopupWindow;
    private Button cancelDeleteBtnPopup, deleteBtnPopup;
    private ListView taskListPopup;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        final FirebaseUser currentUser = auth.getCurrentUser();

        repository = Repository.getInstance();

        SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);

        firstName = findViewById(R.id.firstNameTextView);
        lastName = findViewById(R.id.lastNameTextView);
        TextView emailTextView = findViewById(R.id.emailTextView);

        changePasswordBtn = findViewById(R.id.passwordChangeButton);
        deleteProfileBtn = findViewById(R.id.accountDeleteButton);



        String uid = repository.getUid();

        emailTextView.setText(repository.getCurrentUser().getEmail());

        mDatabase.collection("User").document(uid).get().addOnCompleteListener(
                task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot == null) throw new AssertionError();
                    repository.setUserDocumentSnapshot(documentSnapshot);
                    firstName.setText(documentSnapshot.getString("firstname"));
                    lastName.setText(documentSnapshot.getString("lastname"));
                }
        );

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
// inflate the layout of the delete popup window
        View deletePopupView = inflater.inflate(R.layout.delete_popup_window, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        deletePopupWindow = new PopupWindow(deletePopupView, width, height, true);

        cancelDeleteBtnPopup = deletePopupWindow.getContentView().findViewById(R.id.cancelButton);
        deleteBtnPopup = deletePopupWindow.getContentView().findViewById(R.id.deleteTasksButton);
        taskListPopup = deletePopupWindow.getContentView().findViewById(R.id.taskOverviewListView);
        TextView titlePopup = deletePopupWindow.getContentView().findViewById(R.id.titleToDeleteTextView);
        EditText emailEditText = deletePopupWindow.getContentView().findViewById(R.id.emailEditText);
        EditText passwordEditText = deletePopupWindow.getContentView().findViewById(R.id.passwordEditText);

        //todo change password setOnClickListener
        changePasswordBtn.setOnClickListener(v -> {
            assert currentUser != null;
            auth.sendPasswordResetEmail(Objects.requireNonNull(currentUser.getEmail()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, getString(R.string.email_sent), Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(ProfileActivity.this, getString(R.string.error_reset_password), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        deleteProfileBtn.setOnClickListener(v -> {
            taskListPopup.setVisibility(View.INVISIBLE);
            titlePopup.setText("");
            emailEditText.setVisibility(View.VISIBLE);
            passwordEditText.setVisibility(View.VISIBLE);
            deletePopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            deleteBtnPopup.setOnClickListener(v1 -> {
                boolean isInputValid = true;
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Passwort eingeben");
                    isInputValid = false;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailEditText.setError("Bitte gültiges Email-Adressen-Format eingeben");
                    isInputValid =false;
                }

                if (isInputValid) {

                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, password);

                // Prompt the user to re-provide their sign-in credentials
                    assert currentUser != null;
                    currentUser.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
//                                String email1 = currentUser.getEmail();
                                currentUser.delete()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Log.d(TAG, "User account deleted.");
//                                                deletePopupWindow.dismiss();

                                                // delete documents in database
                                                CollectionReference checklistRef = mDatabase.collection("Checklist");
                                                Task<QuerySnapshot> checklistDatabaseTask = checklistRef.whereEqualTo("userid", uid).get();
                                                checklistDatabaseTask.addOnSuccessListener(queryDocumentSnapshots ->
                                                {
                                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                                        checklistRef.document(documentSnapshot.getId()).delete()
                                                                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                                                                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                                                    }
                                                });

                                                CollectionReference userRef = mDatabase.collection("User");
                                                userRef.document(uid).delete()
                                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                                                        .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));

                                                CollectionReference sharedListsRef = mDatabase.collection("SharedLists");
                                                sharedListsRef.document(email).delete()
                                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                                                        .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));;

                                                sharedPreferences.edit().clear().apply();
                                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Log.d("TAG", "User account deletion unsucessful.");
                                                Toast.makeText(ProfileActivity.this, R.string.deletion_failed, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(ProfileActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            });
            cancelDeleteBtnPopup.setOnClickListener(v1 -> {
                deletePopupWindow.dismiss();
            });
        });
    }

    @Override
    protected void onStop() {
        if(deletePopupWindow.isShowing()) {
            deletePopupWindow.dismiss();
        }
        super.onStop();
    }

    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this, ChecklistOverviewActivity.class);
        startActivity(intent);
        finish();
    }
}