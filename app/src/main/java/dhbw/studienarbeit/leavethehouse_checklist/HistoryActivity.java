package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class HistoryActivity extends BaseActivity {
    private static final String TAG = "HistoryActivity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button listOverviewButton = findViewById(R.id.continueButton);
        ListView overviewList = findViewById(R.id.overviewListView);

        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();

        getSupportActionBar().setTitle(selectedList.getTitle());


        DocumentReference ref = mDatabase.collection("Checklist").document(selectedList.getId());

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    ArrayList<HashMap<String, Object>> history = (ArrayList<HashMap<String, Object>>) document.get("history");
                    if (history != null) {
                        setListItems(history, overviewList, selectedList);
                    }

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });



        listOverviewButton.setOnClickListener(v -> {
            startActivity( new Intent(HistoryActivity.this, ChecklistOverviewActivity.class));
            finish();
        });
    }

    private void setListItems(ArrayList<HashMap<String, Object>> history, ListView listView, Checklist selectedList) {
        ArrayAdapter mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, history) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText((CharSequence) history.get(position).get("date"));
                ArrayList checkedTasks = (ArrayList)Objects.requireNonNull(history.get(position).get("checkedTasks"));

                ArrayList<String> uncheckedTasks = new ArrayList();

                selectedList.getTasks().forEach(task -> {
                    if(!checkedTasks.contains(task)){
                        uncheckedTasks.add(String.valueOf(task));
                    }
                });
                if(uncheckedTasks.isEmpty()){
                text2.setText(checkedTasks.size()+ "/"+selectedList.getTasks().size()+ " bestätigt.");
                }else {
                    text2.setText(checkedTasks.size() + "/" + selectedList.getTasks().size() + " "+getString(R.string.approved)+ " " +getString(R.string.missingTasks)+" "+ uncheckedTasks.stream().map(i->i.toString()).collect(Collectors.joining(", ")));
                }
                return view;
            }
        };

        listView.setAdapter(mAdapter);
    }

    public void onBackPressed() {
        startActivity( new Intent(HistoryActivity.this, TaskChecklistActivity.class));
        finish();
    }
}