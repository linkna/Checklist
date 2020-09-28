package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.Map;
import java.util.Objects;

public class TaskChecklistActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_checklist);
        Bundle extras = getIntent().getExtras();
        //ChecklistOverviewActivity.getSelectedList();
    }
}