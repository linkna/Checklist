package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ChecklistOverviewActivity extends AppCompatActivity {

    public static final String CHECKLIST = "Checklist";

    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private String uid;
    public TextView sampleText;
    public ListView overviewList;

    ArrayList<String> sampleItems;
    private List<String> checklistid;
    private List<Checklist> collectionOfChecklists;


    private static final String TAG = "MyActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_overview);
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        overviewList = (ListView) findViewById(R.id.overviewListView);


        sampleText = (TextView) findViewById(R.id.sampleTextView);

        sampleItems = new ArrayList<String>();
        sampleItems.add("item 1");
        sampleItems.add("item 2");

        setListItems(overviewList, sampleItems);

        uid = getUid();


        overviewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //setChecklist(uid, "Testliste", sampleItems);
                //getUserChecklistIds();
                getChecklistData(uid);


                Toast.makeText(ChecklistOverviewActivity.this, sampleItems.get(position) + " " + uid, Toast.LENGTH_SHORT).show();
            }

        });


    }


    private void setListItems(ListView overviewList, ArrayList titles) {
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        overviewList.setAdapter(itemsAdapter);
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



    public void getChecklistData(String uid) {


        CollectionReference checklistRef = mDatabase.collection(CHECKLIST);
        checklistRef.whereEqualTo("userid", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        collectionOfChecklists = task.getResult().getDocuments().stream().map(documentSnapshot ->
                                new Checklist(documentSnapshot.getId(), documentSnapshot.getString("title"), documentSnapshot.getString("userid"), documentSnapshot.get("tasks", List.class))
                        ).collect(Collectors.toList());


//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            Log.d(TAG, document.getId() + " => " + document.getData());
//
//                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (mAuth.getCurrentUser() != null) {
            return currentUser.getUid();
        }
        return null;
    }

}