package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskChecklistActivity extends BaseActivity {


    private ArrayList<String> checkedTasks;
    private ArrayList<Integer> checkedItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_checklist);

        ListView taskList = findViewById(R.id.taskOverviewListView);
        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();

        SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        getSupportActionBar().setTitle(selectedList.getTitle());


        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, selectedList.getTasks());
        taskList.setAdapter(mAdapter);
//        if (repository.getCheckedItems() != null) {
//            repository.getCheckedItems().keySet().forEach(position -> {
//                taskList.setItemChecked(position, true);
//            });
//        }

//        // set checked items
//
//        if (sharedPreferences != null && sharedPreferences.getStringSet(selectedList.getTitle(), null) != null) {
//
//            Set<String> tmpSet = sharedPreferences.getStringSet(selectedList.getTitle(), null);
//
//            tmpSet.forEach(s -> {
//                Integer position = Integer.valueOf(s);
//                taskList.setItemChecked(position, true);
//                Log.d("TaskCkecklistActivity ", s + " checked position");
//            });
//
//
//        }


        // Testen: speichern der ausgewählten Elemente als shared Preference für die letzte Liste
        taskList.setOnItemClickListener((parent, view, position, id) -> {
            editor.clear();
//            Map<Integer, String> checkedItems = new HashMap<>();
            Set<String> checkedTasks = new HashSet();
            Set<String> taskSet = new HashSet<>(selectedList.getTasks());
            //get checked items
            SparseBooleanArray checked = taskList.getCheckedItemPositions();
            for (int i = 0; i < checked.size(); i++)
                if (checked.valueAt(i)) {
                    checkedTasks.add(String.valueOf((checked.keyAt(i))));
                    // editor.putInt(taskList.getItemAtPosition(checked.keyAt(i)).toString(), checked.keyAt(i));

//                    checkedItems.put(checked.keyAt(i), taskList.getItemAtPosition(checked.keyAt(i)).toString());
                    String item = taskList.getItemAtPosition(checked.keyAt(i)).toString();

                }
            editor.putStringSet("allTasksInSelectedList", taskSet);
            editor.putStringSet(selectedList.getTitle(), checkedTasks);

            editor.apply();
//            repository.setCheckedItems(checkedItems);


        });

        Log.d("Tasklist: ", selectedList.getTitle() + " Titel der Liste");

    }

    public void editListLabelClick(View view) {
        Intent intent = new Intent(TaskChecklistActivity.this, EditListActivity.class);
        startActivity(intent);
        finish();
    }

    public void onBackPressed() {
        Intent intent = new Intent(TaskChecklistActivity.this, ChecklistOverviewActivity.class);
        startActivity(intent);
        finish();
    }
}