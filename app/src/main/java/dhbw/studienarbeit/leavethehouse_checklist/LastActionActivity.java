package dhbw.studienarbeit.leavethehouse_checklist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class LastActionActivity extends BaseActivity {

    private Set<String> checkedTaskPositions;
    private List<String> allTasks;
    String TAG = "LastActionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_action);

        TextView titleTextView = findViewById(R.id.titleTextView);
        ListView taskListView = findViewById(R.id.taskOverviewListView);
        Button listOverviewButton = findViewById(R.id.continueButton);


        SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);

//
        if (sharedPreferences != null) {

            Log.d(TAG, "shared preference " + sharedPreferences.getAll().keySet());
            Set<String> keySet = sharedPreferences.getAll().keySet();

            AtomicReference<String> listTitle = new AtomicReference<>();

            for (String element : keySet) {
                Log.d(TAG, "shared preference key: " + element);
                if (element.equalsIgnoreCase("allTasksInSelectedList")) {
                    Set<String> allTasksInSelectedList = sharedPreferences.getStringSet(element, null);
                    allTasks = new ArrayList<>(Objects.requireNonNull(allTasksInSelectedList));
                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allTasks);
                    taskListView.setAdapter(mAdapter);
                } else {
                    listTitle.set(element);
                    titleTextView.setText(element);
                    checkedTaskPositions = sharedPreferences.getStringSet(element, null);
                    if (checkedTaskPositions == null) throw new AssertionError();

                        taskListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allTasks) {
                            @Override
                            public View getView(int pos, View convertView, ViewGroup parent) {
                                View row = super.getView(pos, convertView, parent);

                                if (checkedTaskPositions.contains(String.valueOf(pos))) {                                    // do something change color
                                    row.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                }
                                else{
                                    // default state
                                    row.setBackgroundColor(getResources().getColor(R.color.red));
                                }
                                return row;
                            }
                        });
//                    }

                    }
//                }
            }
        }

        listOverviewButton.setOnClickListener(v -> {
            Intent intent = new Intent(LastActionActivity.this, ChecklistOverviewActivity.class);
            startActivity(intent);
            finish();
        });

    }
    public void onBackPressed() {
        Intent intent = new Intent(LastActionActivity.this, ChecklistOverviewActivity.class);
        startActivity(intent);
        finish();
    }
}