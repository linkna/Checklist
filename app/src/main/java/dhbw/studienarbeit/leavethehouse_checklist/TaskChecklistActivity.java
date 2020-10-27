package dhbw.studienarbeit.leavethehouse_checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TaskChecklistActivity extends BaseActivity {

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


        // save selected items as shared preference for last action
        taskList.setOnItemClickListener((parent, view, position, id) -> {
            editor.clear();
            Set<String> checkedTasks = new HashSet();
            Set<String> taskSet = new HashSet<>(selectedList.getTasks());

            //get checked items
            SparseBooleanArray checked = taskList.getCheckedItemPositions();
            for (int i = 0; i < checked.size(); i++)
                if (checked.valueAt(i)) {
                    checkedTasks.add(String.valueOf((checked.keyAt(i))));
                }
            editor.putStringSet("allTasksInSelectedList", taskSet);
            editor.putStringSet(selectedList.getTitle(), checkedTasks);

            editor.apply();
        });
    }

    public void editListLabelClick(View view) {
        startActivity(new Intent(TaskChecklistActivity.this, EditListActivity.class));
        finish();
    }

    public void shareListLabelClick(View view) {
        startActivity(new Intent(TaskChecklistActivity.this, ShareListActivity.class));
        finish();
    }

    public void onBackPressed() {
        startActivity(new Intent(TaskChecklistActivity.this, ChecklistOverviewActivity.class));
        finish();
    }
}