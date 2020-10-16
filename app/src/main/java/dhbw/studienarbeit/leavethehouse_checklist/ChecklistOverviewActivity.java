package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ChecklistOverviewActivity extends BaseActivity {

    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    public ListView overviewList;
    public FloatingActionButton addListButton;


    ArrayList<String> checklistTitleList;
    Task<List<Checklist>> checklistFuture;


    private static final String TAG = "ChecklistOverviewActivity";
    private TextView noListTextView;
    private Button importBtn;
    private List<Map<String, Object>> checklistMap;
    private Checklist selectedList;
    private Repository repository;
    private List<String> sharedLists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_overview);


        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        repository = Repository.getInstance();

        overviewList = findViewById(R.id.overviewListView);
        noListTextView = findViewById(R.id.noListTextView);
        addListButton = findViewById(R.id.addListButton);
        importBtn = findViewById(R.id.cancelButton);

        checklistTitleList = new ArrayList<>();
        checklistMap = new ArrayList<>();
        selectedList = new Checklist();

        setListItems(overviewList, checklistTitleList);

        String uid = getUid();


        checklistFuture = getChecklistData(uid);
        updateList(checklistFuture);
//        List<String> sharedLists = repository.getSharedLists();
//        Log.d(TAG, String.valueOf(sharedLists));

        // get shared checklist ids and write them to repository
        mDatabase.collection("SharedLists")
                .document(mAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                sharedLists=(List<String>) document.get("checklistID");
                                repository.setSharedLists((List<String>) document.get("checklistID"));

                                if(!sharedLists.isEmpty()) {
                                    importBtn.setVisibility(View.VISIBLE);
                                    importBtn.setOnClickListener(v -> {
                                        Intent intent = new Intent(ChecklistOverviewActivity.this, ImportListActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });

                                    Toast.makeText(ChecklistOverviewActivity.this, getString(R.string.new_shared_list), Toast.LENGTH_SHORT).show();
                                }
                                    Log.d(TAG, "checklistID data: " + document.get("checklistID"));

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });




        overviewList.setOnItemClickListener((parent, view, position, id) -> {

            String clickedTitle = checklistTitleList.get(position);

            for (Map map : checklistMap) {
                if (String.valueOf(map.get("title")).equals(clickedTitle)) {

                    selectedList.setId(String.valueOf(map.get("id")));
                    selectedList.setTitle(String.valueOf(map.get("title")));
                    selectedList.setTasks((ArrayList<String>) map.get("tasks"));
                    selectedList.setUserid(String.valueOf(map.get("userid")));

                    repository.setSelectedList(selectedList);


                    Log.d(TAG, String.valueOf(map.get("title")));
                    Log.d(TAG, selectedList.getTitle());

                }
            }

            Intent intent = new Intent(ChecklistOverviewActivity.this, TaskChecklistActivity.class);
            startActivity(intent);
            finish();

        });

        addListButton.setOnClickListener(view -> {
            Intent intent = new Intent(ChecklistOverviewActivity.this, AddListActivity.class);
            startActivity(intent);
            finish();
        });

    }

//    private List<String> getSharedLists(String email) {
//
//        List<String> sharedList;
//
//        Task<DocumentSnapshot> sharedListTask = mDatabase
//                .collection("SharedLists")
//                .document(email)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    documentSnapshot.get("checklistID");
//                });
//
//        if (sharedListTask.isSuccessful() && sharedListTask.getResult() != null && sharedListTask.getResult().get("checklistID") instanceof List) {
//            sharedList = (List<String>) sharedListTask.getResult().get("sharedList");
//        }else{
//            sharedList = Collections.emptyList();
//        }
//        return sharedList;
//    }

    @SuppressWarnings("ConstantConditions")
    private void updateList(Task<List<Checklist>> checklistFuture) {
        checklistFuture.onSuccessTask(checklists -> {
            if (checklists == null) throw new AssertionError();
            checklists.forEach(checklist -> {
                checklistTitleList.add(checklist.getTitle());
                Map<String, Object> tmpMap = new HashMap<>();
                tmpMap.put("id", checklist.getId());
                tmpMap.put("userid", checklist.getUserid());
                tmpMap.put("title", checklist.getTitle());
                tmpMap.put("tasks", checklist.getTasks());
                checklistMap.add(tmpMap);
                Log.d(TAG, "Titel: " + checklist.getTitle());
                Log.d(TAG, "Tasks: " + checklist.getTasks());
            });
            repository.setAllListsOfUser(checklists);

            if (checklistTitleList.isEmpty()) {
                noListTextView.setText(getString(R.string.noLists));
            }
            setListItems(overviewList, checklistTitleList);

            return null;
        });
    }

    private void setListItems(ListView overviewList, List<String> titles) {
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        overviewList.setAdapter(mAdapter);
    }


    public Task<List<Checklist>> getChecklistData(String uid) {
        if (uid == null) {
            throw new AssertionError("Uid should never be null. User should be logged in.");
        }

        TaskCompletionSource<List<Checklist>> checklist = new TaskCompletionSource<>();

        CollectionReference checklistRef = mDatabase.collection("Checklist");

        Task<QuerySnapshot> checklistDatabaseTask = checklistRef.whereEqualTo("userid", uid).get();
        checklistDatabaseTask.addOnSuccessListener(queryDocumentSnapshots -> checklist.setResult(queryDocumentSnapshots.getDocuments().stream().map(documentSnapshot ->

                new Checklist.ChecklistBuilder().setId(documentSnapshot.getId())
                        .setTitle(documentSnapshot.getString("title"))
                        .setTasks((ArrayList<String>) documentSnapshot.get("tasks"))
                        .setUserid(documentSnapshot.getString("userid"))
                        .build()).collect(Collectors.toList())));

        return checklist.getTask();
    }


    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (mAuth.getCurrentUser() != null) {
            repository.setCurrentUser(currentUser);
            if (currentUser == null) throw new AssertionError();
            repository.setUid(currentUser.getUid());
            return currentUser.getUid();
        }
        Log.d(TAG, "no user is logged in");
        return null;
    }
    public void onBackPressed() {
        finish();
    }

}