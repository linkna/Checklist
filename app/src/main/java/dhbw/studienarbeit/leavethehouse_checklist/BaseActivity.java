package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String title= (String) item.getTitle();
        if(title.equalsIgnoreCase(getString(R.string.logout))){
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }else if(title.equalsIgnoreCase(getString(R.string.profil))){
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }else if(title.equalsIgnoreCase(getString(R.string.listOverview))){
            Intent intent = new Intent(this, ChecklistOverviewActivity.class);
            startActivity(intent);
        }

        //respond to menu item selection
        return true;
    }


}