package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ChecklistOverviewActivity extends BaseActivity {

    public static final String CHECKLIST = "Checklist";

    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private String uid;
    public TextView sampleText;
    public ListView overviewList;
    public FloatingActionButton addListButton;


    ArrayList<String> checklistTitleList;
    private List<String> checklistid;
    Task<List<Checklist>> checklistFuture;


    private static final String TAG = "MyActivity";
    private TextView noListTextView;
    private List<Map<String, Object>> checklistMap;
    private Checklist selectedList;
    private Repository repository;


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

        checklistTitleList = new ArrayList<>();
        checklistMap = new ArrayList<>();
        selectedList = new Checklist();


//        sampleItems.add("item 1");
//        sampleItems.add("item 2");

        setListItems(overviewList, checklistTitleList);

        uid = getUid();


        checklistFuture = getChecklistData(uid);
        updateList();


        overviewList.setOnItemClickListener((parent, view, position, id) -> {
            //setChecklist(uid, "Testliste", sampleItems);
            //getUserChecklistIds();
            //checklistFuture=getChecklistData(uid);

            String clickedTitle = checklistTitleList.get(position);

            //Todo: überprüfen ob das funktioniert: Map = Arraylist size=5 aber leer
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


    private void updateList() {
        checklistFuture.onSuccessTask(checklists -> {
            checklists.forEach(checklist -> {
                checklistTitleList.add(checklist.getTitle());
                Map<String, Object> tmpMap = new HashMap<>();
                tmpMap.put("id", checklist.getId());
                tmpMap.put("userid", checklist.getUserid());
                tmpMap.put("title", checklist.getTitle());
                tmpMap.put("tasks", checklist.getTasks());
                ;
                checklistMap.add(tmpMap);
                Log.d(TAG, "Titel: " + checklist.getTitle());
                Log.d(TAG, "Tasks: " + checklist.getTasks());
            });
            repository.setAllListsOfUser(checklists);

//           Log.d(TAG, "145: Titel in Map " +String.valueOf(checklistMap.get(1).get("title")));

            if (checklistTitleList.isEmpty()) {
                noListTextView.setText(getString(R.string.noLists));
            }
            setListItems(overviewList, checklistTitleList);
            return null;
        });

    }


    private void setListItems(ListView overviewList, List<String> titles) {
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        overviewList.setAdapter(mAdapter);
    }

    /**
     * @param uid    uid of current user
     * @param title  title of the new checklist
     * @param tasks
     * @param titles Titles of existing checklists for current user
     */
    public void writeChecklist(String uid, String title, List<String> tasks, List<String> titles) {
        Map<String, Object> newChecklist = new HashMap<>();

        // check if list title exists for current user - No equal titles are allowed.
        if (titles.stream().anyMatch(titleToMatch -> titleToMatch.equalsIgnoreCase(title))) {
            // Fehlermeldung, Eintrag bereits vorhanden
            Toast.makeText(ChecklistOverviewActivity.this, title + getString(R.string.errorTitleExists), Toast.LENGTH_SHORT).show();
        } else {
            newChecklist.put("userid", uid);
            newChecklist.put("title", title);
            newChecklist.put("tasks", tasks);


            // write new checklist to database
            mDatabase.collection("Checklist")
                    .add(newChecklist)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        checklistid.add(documentReference.getId());
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }
    }


    // wahrscheinlich unnötig. Die checklist-Tabelle kann direkt anhand der userid gefiltert werden (Ref.whereEqualTo(...).get())
    public void getUserChecklistIds() {

        final DocumentReference userDocRef = mDatabase.collection("User").document(uid);

        userDocRef.get().addOnCompleteListener(
                (task) -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot == null) {
                        // no checklists, show empty List
                        overviewList.setAdapter(null);
                        TextView message = findViewById(R.id.noListTextView);
                        message.setText("Bitte Checkliste anlegen");
                    } else if (documentSnapshot.get("checklistid") instanceof List) {
                        checklistid = (List<String>) documentSnapshot.get("checklistid");

                    } else {
                        throw new AssertionError("Should be a List with Strings");
                    }
                }
        );

    }


    public Task<List<Checklist>> getChecklistData(String uid) {
        if (uid == null) {
            throw new AssertionError("Uid should never be null. User should be logged in.");
        }

        TaskCompletionSource<List<Checklist>> checklist = new TaskCompletionSource<>();

        CollectionReference checklistRef = mDatabase.collection(CHECKLIST);

        Task<QuerySnapshot> checklistDatabaseTask = checklistRef.whereEqualTo("userid", uid).get();
        checklistDatabaseTask.addOnSuccessListener(queryDocumentSnapshots -> {

            checklist.setResult(queryDocumentSnapshots.getDocuments().stream().map(documentSnapshot ->

                    new Checklist.ChecklistBuilder().setId(documentSnapshot.getId())
                            .setTitle(documentSnapshot.getString("title"))
                            .setTasks((ArrayList<String>) documentSnapshot.get("tasks"))
                            .setUserid(documentSnapshot.getString("userid"))
                            .build()).collect(Collectors.toList()));
        });

        return checklist.getTask();
    }


    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (mAuth.getCurrentUser() != null) {
            repository.setCurrentUser(currentUser);
            repository.setUid(currentUser.getUid());
            return currentUser.getUid();
        }
        Log.d(TAG, "no user is logged in");
        return null;
    }

}