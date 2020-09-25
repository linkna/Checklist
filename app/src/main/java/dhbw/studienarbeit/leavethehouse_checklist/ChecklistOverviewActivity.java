package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class ChecklistOverviewActivity extends AppCompatActivity {

    public static final String CHECKLIST = "Checklist";

    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private String uid;
    public TextView sampleText;
    public ListView overviewList;
    public FloatingActionButton addListButton;

    private ArrayAdapter<String> mAdapter;

    ArrayList<String> sampleItems;
    private List<String> checklistid;
    Task<List<Checklist>> checklistFuture;


    private static final String TAG = "MyActivity";
    private Checklist checklist;
    private TextView noListTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_overview);
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        overviewList = findViewById(R.id.overviewListView);


        sampleText = findViewById(R.id.sampleTextView);
        noListTextView = findViewById(R.id.noListTextView);

        addListButton = findViewById(R.id.addListButton);

        sampleItems = new ArrayList<String>();
//        sampleItems.add("item 1");
//        sampleItems.add("item 2");

        setListItems(overviewList, sampleItems);

        uid = getUid();
        checklistFuture=getChecklistData(uid);
        updateList();

        overviewList.setOnItemClickListener((parent, view, position, id) -> {
            //setChecklist(uid, "Testliste", sampleItems);
            //getUserChecklistIds();
            //checklistFuture=getChecklistData(uid);

            Toast.makeText(ChecklistOverviewActivity.this, sampleItems.get(position) + " " + uid, Toast.LENGTH_SHORT).show();
        });

        addListButton.setOnClickListener(view -> {
            Toast.makeText(ChecklistOverviewActivity.this, "clicked on add", Toast.LENGTH_SHORT).show();
        });


    }

    private void updateList() {
        checklistFuture.onSuccessTask(checklists -> {
           checklists.forEach(checklist -> {
               sampleItems.add(checklist.getTitle());
               Log.d(TAG, "Titel: " + checklist.getTitle());
               Log.d(TAG, "Tasks: " + checklist.getTasks());
           });

            if (sampleItems.isEmpty()){
                noListTextView.setText("Es wurden noch keine Listen angelegt.");
            }
            setListItems(overviewList, sampleItems);
           return null;
        });
    }


    private void setListItems(ListView overviewList, ArrayList titles) {
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        overviewList.setAdapter(mAdapter);
    }

    public void setChecklist(String uid, String title, ArrayList<String> tasks) {

        final Map<String, Object> checklist = new HashMap<>();
        checklist.put("userid", uid);
        checklist.put("title", title);
        checklist.put("tasks", tasks);
        mDatabase.collection("Checklist")
                .add(checklist)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        checklistid.add(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

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
                        // bitte wieder löschen:
                        sampleText.setText("Anzahl der Listen: " + checklistid.size() + " Erster Eintrag: " + checklistid.get(0));
                    } else {
                        throw new AssertionError("Should be a List with Strings");
                    }
                }
        );

    }


    public Task<List<Checklist>> getChecklistData(String uid) {

        TaskCompletionSource<List<Checklist>> checklist = new TaskCompletionSource<>();

        CollectionReference checklistRef = mDatabase.collection(CHECKLIST);

        Task<QuerySnapshot> checklistDatabaseTask =checklistRef.whereEqualTo("userid", uid).get();
                checklistDatabaseTask.addOnSuccessListener( queryDocumentSnapshots -> {

                    checklist.setResult(queryDocumentSnapshots.getDocuments().stream().map(documentSnapshot ->

                            new Checklist.ChecklistBuilder().setId(documentSnapshot.getId())
                            .setTitle(documentSnapshot.getString("title"))
                            .setTasks(documentSnapshot.get("tasks"))
                            .setUserid(documentSnapshot.getString("userid"))
                            .build()).collect(Collectors.toList()));

//                            new Checklist(
//                                   documentSnapshot.getId(),
//                                   documentSnapshot.getString("title"),
//                                   documentSnapshot.getString("userid"),
//                                   documentSnapshot.get("tasks", List.class))
//                           ).collect(Collectors.toList()));
                });

                return checklist.getTask();


//        checklistRef.whereEqualTo("userid", uid)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//
//                        collectionOfChecklists = task.getResult().getDocuments().stream().map(documentSnapshot ->
//                                new Checklist(documentSnapshot.getId(), documentSnapshot.getString("title"), documentSnapshot.getString("userid"), documentSnapshot.get("tasks", List.class))
//                        ).collect(Collectors.toList());
//
//
////                        for (QueryDocumentSnapshot document : task.getResult()) {
////                            Log.d(TAG, document.getId() + " => " + document.getData());
////
////                        }
//                    } else {
//                        Log.d(TAG, "Error getting documents: ", task.getException());
//                    }
//                });

    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (mAuth.getCurrentUser() != null) {
            return currentUser.getUid();
        }
        return null;
    }

}