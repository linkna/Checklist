package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportListActivity extends AppCompatActivity {

    private static final String TAG = "ImportListActivity";
    private ArrayAdapter mAdapter;
    private List tasks;
    private String listID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_list);

        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        Repository repository = Repository.getInstance();
        String uid = repository.getUid();
        String email = repository.getCurrentUser().getEmail();

        ListView sharedListView = findViewById(R.id.overviewListView);
        Button cancelBtn = findViewById(R.id.cancelButton);


        List<String> sharedListIDs = repository.getSharedLists();


        List<Map<String, Object>> sharedListMap = new ArrayList<>();
        List<String> sharedTitles = new ArrayList<>();

        for (String sharedListID : sharedListIDs) {
            mDatabase.collection("Checklist").document(sharedListID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Map<String, Object> tmpMap = new HashMap<>();
                        tmpMap.put("id", document.getId());
                        tmpMap.put("title", document.get("title"));
                        tmpMap.put("tasks", document.get("tasks"));
                        sharedListMap.add(tmpMap);
                        sharedTitles.add((String) document.get("title"));

                        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sharedTitles);
                        sharedListView.setAdapter(mAdapter);
                    } else {
                        Toast.makeText(ImportListActivity.this, getString(R.string.no_document_in_db), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View importPopupView = inflater.inflate(R.layout.import_popup_window, null);

        // create the popup window
//        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.8);
//        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        PopupWindow importListPopupWindow = new PopupWindow(importPopupView, width, height, true);

        // elements in editText popup Window

        Button importBtnPopup = importListPopupWindow.getContentView().findViewById(R.id.importButton);
        Button cancelBtnPopup = importListPopupWindow.getContentView().findViewById(R.id.cancelButton);
        TextView titleToImport = importListPopupWindow.getContentView().findViewById(R.id.sharedListTitleTextView);
        ListView tasksToImportListView = importListPopupWindow.getContentView().findViewById(R.id.overviewListView);

        sharedListView.setOnItemClickListener((parent, view, position, id) -> {
            importListPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            String title= sharedTitles.get(position);
            titleToImport.setText(title);
            sharedListMap.forEach(list -> {
                if(list.get("title").equals(title)){
                    listID = (String) list.get("id");
                    tasks = (List) list.get("tasks");
                    mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks);
                    tasksToImportListView.setAdapter(mAdapter);
                }
            });

            importBtnPopup.setOnClickListener(v -> {
                Map<String, Object> newChecklist = new HashMap<>();
                newChecklist.put("title", title);
                newChecklist.put("tasks", tasks);
                newChecklist.put("userid", uid);
                // write new checklist to database and remove from shared lists
                mDatabase.collection("Checklist")
                        .add(newChecklist)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                            sharedListIDs.remove(listID);

                            mDatabase.collection("SharedLists")
                                    .document(email).update("checklistID", sharedListIDs);

                            startActivity(new Intent(ImportListActivity.this, ChecklistOverviewActivity.class));
                            finish();
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });

            });

            cancelBtnPopup.setOnClickListener(v -> importListPopupWindow.dismiss());
        });

        cancelBtn.setOnClickListener(v -> {
            startActivity(new Intent(ImportListActivity.this, ChecklistOverviewActivity.class));
            finish();
        });


    }

    public void onBackPressed() {
        startActivity(new Intent(ImportListActivity.this, ChecklistOverviewActivity.class));
        finish();
    }
}