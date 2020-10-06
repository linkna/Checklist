package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = this.getSharedPreferences("checkedItems", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String title = (String) item.getTitle();
        if (title.equalsIgnoreCase(getString(R.string.logout))) {
            editor.clear();
            editor.apply();
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        } else if (title.equalsIgnoreCase(getString(R.string.profil))) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (title.equalsIgnoreCase(getString(R.string.listOverview))) {
            startActivity(new Intent(this, ChecklistOverviewActivity.class));
        }else if(title.equalsIgnoreCase(getString(R.string.last_action))){
            startActivity(new Intent(this, LastActionActivity.class));
        }

        //respond to menu item selection
        return true;
    }


}