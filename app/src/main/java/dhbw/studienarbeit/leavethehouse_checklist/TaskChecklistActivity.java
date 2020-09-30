package dhbw.studienarbeit.leavethehouse_checklist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TaskChecklistActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_checklist);

        ListView taskList = findViewById(R.id.taskOverviewListView);
        Repository repository = Repository.getInstance();
        Checklist selectedList=repository.getSelectedList();


        getSupportActionBar().setTitle(selectedList.getTitle());

        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, selectedList.getTasks());
        taskList.setAdapter(mAdapter);

        Log.d("Tasklist: ", selectedList.getTitle() + " Titel der Liste" );

    }

    public void editListLabelClick(View view) {
        Intent intent = new Intent(TaskChecklistActivity.this, EditListActivity.class);
        startActivity(intent);
    }
}