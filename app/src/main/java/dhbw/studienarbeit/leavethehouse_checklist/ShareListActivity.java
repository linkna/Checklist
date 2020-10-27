package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareListActivity extends AppCompatActivity {

    private static final String TAG = "ShareListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);

        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();

        TextView title = findViewById(R.id.titleToShareTextView);
        ListView taskList = findViewById(R.id.taskOverviewListView);
        EditText email = findViewById(R.id.emailEditText);
        Button cancelBtn = findViewById(R.id.cancelButton);
        Button shareBtn = findViewById(R.id.shareListButton);

        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedList.getTasks());
        taskList.setAdapter(mAdapter);

        title.setText(selectedList.getTitle());


        shareBtn.setOnClickListener(v -> {
            String input = email.getText().toString();
            if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                writeSharedListToDB(mDatabase, selectedList, email, input);
            } else {
                email.setError(getString(R.string.error_email_valid));
            }
        });

        cancelBtn.setOnClickListener(v -> {
            startActivity(new Intent(ShareListActivity.this, TaskChecklistActivity.class));
            finish();
        });


    }

    private void writeSharedListToDB(FirebaseFirestore mDatabase, Checklist selectedList, EditText email, String input) {
        // search for user document in collection SharedLists and write list id to it
        DocumentReference docRef = mDatabase.collection("SharedLists").document(input);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
//                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    Map<String, Object> data = new HashMap<>();
                    ArrayList<String> ids = new ArrayList<>();
                    if(document.get("checklistID") != null) {
                        ids = (ArrayList<String>) document.get("checklistID");
                    }
                    if(!ids.contains(selectedList.getId())) {
                        ids.add(selectedList.getId());
                        data.put("checklistID", ids);
                        mDatabase.collection("SharedLists").document(input).set(data)
                                .addOnSuccessListener(aVoid -> {
                            startActivity(new Intent(ShareListActivity.this, TaskChecklistActivity.class));
                                    Toast.makeText(ShareListActivity.this, getString(R.string.successfull_shared)+ input, Toast.LENGTH_SHORT).show();
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(ShareListActivity.this, getString(R.string.sharing_failed), Toast.LENGTH_SHORT).show();
                        });
                    }else{
                        Toast.makeText(ShareListActivity.this, getString(R.string.error_is_already_shared), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    email.setError(getString(R.string.user_not_found));
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    public void onBackPressed() {
        startActivity(new Intent(ShareListActivity.this, TaskChecklistActivity.class));
        finish();
    }
}