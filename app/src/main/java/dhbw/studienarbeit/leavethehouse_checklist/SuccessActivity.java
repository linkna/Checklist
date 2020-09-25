package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import dhbw.studienarbeit.leavethehouse_checklist.R;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Button returnButton = findViewById(R.id.zurück);
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.activity_login);
                startActivity(new Intent(SuccessActivity.this, LoginActivity.class));
            }
        });
    }
}