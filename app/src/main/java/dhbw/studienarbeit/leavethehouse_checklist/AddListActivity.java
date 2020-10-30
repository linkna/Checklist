package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddListActivity extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private Repository repository;
    private ArrayAdapter<String> myAdapter;
    private List<Checklist> allListsOfUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        repository = Repository.getInstance();

        String uid = repository.getUid();
        allListsOfUser = repository.getAllListsOfUser();

        Button addTaskBtn = findViewById(R.id.addTaskButton);
        Button saveBtn = findViewById(R.id.saveButton);
        Button cancelBtn = findViewById(R.id.cancelButton);

        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText taskEditText = findViewById(R.id.taskEditText);

        ListView taskListView = findViewById(R.id.taskOverviewListView);
        List<String> taskList = new ArrayList();

        addTaskBtn.setOnClickListener(v -> {
            String task = taskEditText.getText().toString();
            if (task.isEmpty()||task.trim().length()==0) {
                taskEditText.setError(getString(R.string.error_empty_textfield));
            }else {
                if(taskList.contains(task.trim())){
                    taskEditText.setError(getString(R.string.error_Task_Exists));
                }else {
                    taskList.add(task.trim());
                    Collections.sort(taskList, String.CASE_INSENSITIVE_ORDER);
                    taskEditText.setText("");
                    myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
                    taskListView.setAdapter(myAdapter);
                }
            }
        });

        saveBtn.setOnClickListener(v -> {
            String newTitle = titleEditText.getText().toString();
            Map<String, Object> newChecklist = new HashMap<>();
            ArrayList<String> checklistTitleList = new ArrayList<>();
            if (newTitle.isEmpty()||newTitle.trim().length()==0) {
                titleEditText.setError(getString(R.string.error_empty_textfield));
            } else {
                // check if list title exists for current user - No equal titles are allowed.
                allListsOfUser.forEach(checklist -> checklistTitleList.add(checklist.getTitle()));
                if (checklistTitleList.stream().anyMatch(titleToMatch -> titleToMatch.equalsIgnoreCase(newTitle))) {
                    titleEditText.setError(getString(R.string.error_Title_Exists));
                } else {
                    if (taskList.size() == 0) {
                        Toast.makeText(AddListActivity.this, R.string.error_empty_Tasks, Toast.LENGTH_SHORT).show();
                    } else {
                        newChecklist.put("tasks", taskList);
                        newChecklist.put("title", newTitle);
                        newChecklist.put("userid", uid);
                        // write to database, if successfull change repository
                        // write new checklist to database
                        mDatabase.collection("Checklist")
                                .add(newChecklist)
                                .addOnSuccessListener(documentReference -> {
                                    Intent intent = new Intent(AddListActivity.this, ChecklistOverviewActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Log.w("AddList", "Error writing document", e));
                    }
                }
            }
        });

        cancelBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AddListActivity.this, ChecklistOverviewActivity.class);
            startActivity(intent);
            finish();
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(AddListActivity.this, ChecklistOverviewActivity.class);
        startActivity(intent);
        finish();
    }
}