package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Map;
import java.util.Objects;

public class TaskChecklistActivity extends BaseActivity {


    private ListView taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_checklist);

        taskList = findViewById(R.id.taskOverviewListView);

        Repository repository = Repository.getInstance();

        //Bundle extras = getIntent().getExtras();

        Checklist selectedList=repository.getSelectedList();

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, selectedList.getTasks());
        taskList.setAdapter(mAdapter);

        Log.d("Tasklist: ", selectedList.getTitle() + " Titel der Liste" );

    }
}