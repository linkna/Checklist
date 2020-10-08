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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String title = (String) item.getTitle();
        if (title.equalsIgnoreCase(getString(R.string.logout))) {
            startActivity(new Intent(this, LogoutActivity.class));
            finish();
        } else if (title.equalsIgnoreCase(getString(R.string.profil))) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        } else if (title.equalsIgnoreCase(getString(R.string.listOverview))) {
            startActivity(new Intent(this, ChecklistOverviewActivity.class));
            finish();
        }else if(title.equalsIgnoreCase(getString(R.string.last_action))){
            startActivity(new Intent(this, LastActionActivity.class));
            finish();
        }

        //respond to menu item selection
        return true;
    }


}