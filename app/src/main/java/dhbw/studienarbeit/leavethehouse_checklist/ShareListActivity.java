package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;

public class ShareListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);

        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();

        TextView title = findViewById(R.id.titleToShareTextView);
        ListView taskList = findViewById(R.id.taskOverviewListView);
        EditText email = findViewById(R.id.emailEditText);
        Button cancelBtn = findViewById(R.id.cancelButton);
        Button shareBtn = findViewById(R.id.shareListButton);

        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedList.getTasks());
        taskList.setAdapter(mAdapter);

        title.setText(selectedList.getTitle());

        shareBtn.setOnClickListener(v -> {
            String input = email.getText().toString();
            if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
               // search for user and write list as sharedlist to db

            }else {
                email.setError(getString(R.string.error_email_valid));
            }
        });

        cancelBtn.setOnClickListener(v -> {
            startActivity(new Intent(ShareListActivity.this, TaskChecklistActivity.class));
            finish();
        });


    }
    public void onBackPressed() {
        startActivity(new Intent(ShareListActivity.this, TaskChecklistActivity.class));
        finish();
    }
}