package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LastActionActivity extends AppCompatActivity {

    private Set<String> checkedTaskPositions;
    private List<String> allTasks;
    private static final String TAG = "LastActionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_action);

        TextView titleTextView = findViewById(R.id.accessTextView);
        ListView taskListView = findViewById(R.id.taskOverviewListView);
        Button listOverviewButton = findViewById(R.id.continueButton);


        SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);


        if (sharedPreferences != null) {
            Set<String> keySet = sharedPreferences.getAll().keySet();
            for (String element : keySet) {
                if (element.equalsIgnoreCase("allTasksInSelectedList")) {
                    Set<String> allTasksInSelectedList = sharedPreferences.getStringSet(element, null);
                    allTasks = new ArrayList<>(Objects.requireNonNull(allTasksInSelectedList));
                    Collections.sort(allTasks, String.CASE_INSENSITIVE_ORDER);
                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allTasks);
                    taskListView.setAdapter(mAdapter);
                } else {
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
                                    row.setBackgroundColor(getResources().getColor(R.color.red));
                                }
                                return row;
                            }
                        });
                }
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