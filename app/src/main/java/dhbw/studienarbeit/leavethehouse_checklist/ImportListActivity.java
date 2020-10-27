package dhbw.studienarbeit.leavethehouse_checklist;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ImportListActivity extends AppCompatActivity {

    private static final String TAG = "ImportListActivity";
    private ArrayAdapter<String> mAdapter;
    private List <String>tasks;
    private String listID;
   private Repository repository;
    private PopupWindow importListPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_list);

        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        repository = Repository.getInstance();
        String uid = repository.getUid();
        String email = repository.getCurrentUser().getEmail();

        ListView sharedListView = findViewById(R.id.overviewListView);
        Button cancelBtn = findViewById(R.id.cancelButton);


        List<String> sharedListIDs = repository.getSharedLists();

        SharedLists sharedLists = getSharedListsFromDB(mDatabase);
        List<Map<String, Object>> sharedListMap = sharedLists.getSharedListsMap();
        List<String> sharedTitles = sharedLists.getTitles();


        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View importPopupView = inflater.inflate(R.layout.import_popup_window, null);

        // create the popup window
//        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.8);
//        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        importListPopupWindow = new PopupWindow(importPopupView, width, height, true);

        // elements in popup Window
        Button importBtnPopup = importListPopupWindow.getContentView().findViewById(R.id.importButton);
        Button deleteBtnPopup = importListPopupWindow.getContentView().findViewById(R.id.deleteButton);
        Button cancelBtnPopup = importListPopupWindow.getContentView().findViewById(R.id.cancelButton);
        TextView titleToImport = importListPopupWindow.getContentView().findViewById(R.id.sharedListTitleTextView);
        ListView tasksToImportListView = importListPopupWindow.getContentView().findViewById(R.id.overviewListView);

        sharedListView.setOnItemClickListener((parent, view, position, id) -> {
            importListPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            String title= sharedTitles.get(position);

            titleToImport.setText(title);
            for (Map<String, ?> list : sharedListMap) {
                if (Objects.requireNonNull(list.get("title")).equals(title)) {
                    listID = (String) list.get("id");
                    tasks = (List<String>) list.get("tasks");
                    Collections.sort(tasks, String.CASE_INSENSITIVE_ORDER);
                    if (tasks == null) throw new AssertionError();
                    mAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            tasks);
                    tasksToImportListView.setAdapter(mAdapter);
                }
            }

            importBtnPopup.setOnClickListener(v -> {
                Map<String, Object> newChecklist = new HashMap<>();
                if (titleExists(title)){
                    newChecklist.put("title", title+" "+ getString(R.string.imported));
                }else {
                    newChecklist.put("title", title);
                }
                newChecklist.put("tasks", tasks);
                newChecklist.put("userid", uid);
                // write new checklist to database and remove from shared lists
                mDatabase.collection("Checklist")
                        .add(newChecklist)
                        .addOnSuccessListener(documentReference -> {
                            sharedListIDs.remove(listID);

                            if (email == null) throw new AssertionError();
                            mDatabase.collection("SharedLists")
                                    .document(email).update("checklistID", sharedListIDs);

                            Toast.makeText(ImportListActivity.this, getString(R.string.successfull_imported)+" "+newChecklist.get("title"), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ImportListActivity.this, ChecklistOverviewActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error writing document", e);
                            Toast.makeText(ImportListActivity.this, getString(R.string.import_failed), Toast.LENGTH_SHORT).show();
                        });
            });

            deleteBtnPopup.setOnClickListener(v -> {
                sharedListIDs.remove(listID);

                if (email == null) throw new AssertionError();
                mDatabase.collection("SharedLists")
                        .document(email).update("checklistID", sharedListIDs)
                        .addOnSuccessListener(aVoid ->  Toast.makeText(ImportListActivity.this, getString(R.string.shared_list_deleted), Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error writing document", e);
                            Toast.makeText(ImportListActivity.this, getString(R.string.deletion_failed), Toast.LENGTH_SHORT).show();
                        });
                startActivity(new Intent(ImportListActivity.this, ChecklistOverviewActivity.class));
                finish();
            });

            cancelBtnPopup.setOnClickListener(v -> importListPopupWindow.dismiss());
        });

        cancelBtn.setOnClickListener(v -> {
            startActivity(new Intent(ImportListActivity.this, ChecklistOverviewActivity.class));
            finish();
        });


    }

    @Override
    protected void onStop() {
        importListPopupWindow.dismiss();
        super.onStop();
    }

    private boolean titleExists(String title) {
        boolean exists = false;
        List<Checklist> allListsOfUser = repository.getAllListsOfUser();
        for (Checklist checklist : allListsOfUser) {
            if (checklist.getTitle().equalsIgnoreCase(title)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    private SharedLists getSharedListsFromDB(FirebaseFirestore mDatabase) {
        ListView sharedListView = findViewById(R.id.overviewListView);
        List<Map<String, Object>> sharedListMap = new ArrayList<>();
        List<String> sharedTitles = new ArrayList<>();
        List<String> sharedListIDs = Repository.getInstance().getSharedLists();

        for (String sharedListID : sharedListIDs) {
            mDatabase.collection("Checklist").document(sharedListID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Map<String, Object> tmpMap = new HashMap<>();
                        tmpMap.put("id", document.getId());
                        tmpMap.put("title", document.get("title"));
                        tmpMap.put("tasks", document.get("tasks"));
                        sharedListMap.add(tmpMap);
                        sharedTitles.add((String) document.get("title"));


                        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sharedTitles);
                        sharedListView.setAdapter(mAdapter);
                    } else {
                        if(sharedListIDs.size()==1) {
                            Toast.makeText(ImportListActivity.this, getString(R.string.no_document_in_db), Toast.LENGTH_LONG).show();
                        }
                        sharedListIDs.remove(sharedListID);
                        mDatabase.collection("SharedLists")
                                .document(repository.getCurrentUser().getEmail()).update("checklistID", sharedListIDs)
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "Error writing document", e);
                                });
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }
        return new SharedLists(sharedTitles,
                sharedListMap);
    }

    public void onBackPressed() {
        startActivity(new Intent(ImportListActivity.this, ChecklistOverviewActivity.class));
        finish();
    }

    public static class SharedLists{
        private List<String> titles;
        private List<Map<String, Object>> sharedListsMap;

        public SharedLists(List<String> titles, List<Map<String, Object>> sharedListsMap){
            this.titles=titles;
            this.sharedListsMap=sharedListsMap;
        }
        public List<String> getTitles() {
            return titles;
        }
        public List<Map<String, Object>> getSharedListsMap() {
            return sharedListsMap;
        }
    }


}