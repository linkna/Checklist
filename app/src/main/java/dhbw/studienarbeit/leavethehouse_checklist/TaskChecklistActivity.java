package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskChecklistActivity extends BaseActivity {
    private static final String TAG = "TaskChecklistActivity" ;
    Calendar calendar;
    SimpleDateFormat simpledateformat;
    String Date;

    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_checklist);

        mDatabase = FirebaseFirestore.getInstance();

        ListView taskList = findViewById(R.id.taskOverviewListView);
        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();

        SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        getSupportActionBar().setTitle(selectedList.getTitle());


        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, selectedList.getTasks());
        taskList.setAdapter(mAdapter);

        // save selected items as shared preference for last action
        taskList.setOnItemClickListener((parent, view, position, id) -> {
        editor.clear();
        Set<String> checkedTasks = getCheckedTasksPositions(taskList);
//        Set<String> checkedTasks = new HashSet();
        Set<String> taskSet = new HashSet<>(selectedList.getTasks());
//
//        //get checked items positions
//        SparseBooleanArray checked = taskList.getCheckedItemPositions();
//        for (int i = 0; i < checked.size(); i++)
//            if (checked.valueAt(i)) {
//                checkedTasks.add(String.valueOf((checked.keyAt(i))));
//            }
        editor.putStringSet("allTasksInSelectedList", taskSet);
        editor.putStringSet(selectedList.getTitle(), checkedTasks);

        editor.apply();
        });

        Button readyButton = findViewById(R.id.readyButton);

        readyButton.setOnClickListener(v -> {
            Set<String> checkedTasks = getCheckedTasksPositions(taskList);
            Set<String> taskSet = new HashSet<>(selectedList.getTasks());
            calendar = Calendar.getInstance();
            simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date = simpledateformat.format(calendar.getTime());


            DocumentReference ref = mDatabase.collection("Checklist").document(selectedList.getId());

            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete( Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ArrayList history = new ArrayList();
                            if(document.get("history")!=null) {
                                history = (ArrayList<Object>) document.get("history");
                            }
                            List<String> checkedList = new ArrayList<>();
                            checkedTasks.forEach(position -> {
                                checkedList.add(selectedList.getTasks().get(Integer.parseInt(position)));
                            });

                            HashMap<String, Object> newEntry = new HashMap<>();
                            newEntry.put("date", Date);
                            newEntry.put("checkedTasks", checkedList);

                                history.add(newEntry);


                            ref.update("history", history)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "erfolgreich!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, getString(R.string.error_writing_to_db), e);
                                        Toast.makeText(TaskChecklistActivity.this, getString(R.string.saving_failed), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            Toast.makeText(TaskChecklistActivity.this, getString(R.string.saved_to_history)+" "+Date +": "+ checkedTasks.size()+"/"+taskSet.size()+" "+getString(R.string.approved), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(TaskChecklistActivity.this, ChecklistOverviewActivity.class));
            finish();
        });

}

    private Set<String> getCheckedTasksPositions(ListView taskList){
        Set<String> checkedTasksPositions = new HashSet();
        SparseBooleanArray checked = taskList.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++)
            if (checked.valueAt(i)) {
                checkedTasksPositions.add(String.valueOf((checked.keyAt(i))));
            }
        return checkedTasksPositions;
    }

    public void editListLabelClick(View view) {
        startActivity(new Intent(TaskChecklistActivity.this, EditListActivity.class));
        finish();
    }

    public void shareListLabelClick(View view) {
        startActivity(new Intent(TaskChecklistActivity.this, ShareListActivity.class));
        finish();
    }

    public void onBackPressed() {
        startActivity(new Intent(TaskChecklistActivity.this, ChecklistOverviewActivity.class));
        finish();
    }

    public void historyLabelClick(View view) {
        startActivity(new Intent(TaskChecklistActivity.this, HistoryActivity.class));
        finish();
    }
}