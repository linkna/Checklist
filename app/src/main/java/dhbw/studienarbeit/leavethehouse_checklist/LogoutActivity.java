package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Repository repository;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        repository = Repository.getInstance();

        SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        repository.setCurrentUser(null);
        editor.clear();
        editor.apply();
        mAuth.signOut();

        startActivity(new Intent(LogoutActivity.this, LoginActivity.class));
        finish();
    }
}