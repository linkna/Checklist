package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class EditListActivity extends AppCompatActivity {

    private TextView title;

    private Button editBtn;

    private ListView taskList;

    PopupWindow popupWindow;
    private TextView inputName;
    private View popupView;
    private PopupWindow deletePopupWindow;
    private View deletePopupView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();

         taskList = findViewById(R.id.taskOverviewListView);
         title = findViewById(R.id.titleTextView);
         editBtn = findViewById(R.id.editTitleButton);


        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, selectedList.getTasks());
        taskList.setAdapter(mAdapter);

        title.setText(selectedList.getTitle());


        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        inputName = popupWindow.getContentView().findViewById(R.id.titleTextView);

        // inflate the layout of the delete popup window
         deletePopupView = inflater.inflate(R.layout.delete_popup_window, null);

        // create the popup window
         deletePopupWindow = new PopupWindow(deletePopupView, width, height, focusable);
    }

    public void editTitleClick(View view) {

        inputName.setText("neuer Titel");
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
       popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });


    }

    public void newTaskClick(View view) {

        inputName.setText("neue Aufgabe");

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    public void saveTitleClick(View view) {
        Toast.makeText(EditListActivity.this, "clicked on save", Toast.LENGTH_SHORT).show();
        popupWindow.dismiss();
    }

    public void deleteSelectedTasksClick(View view) {

                // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        deletePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
//        deletePopupView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                deletePopupWindow.dismiss();
//                return true;
//            }
//        });
    }

    public void accessDeleteClick(View view) {
        //do something to delete the tasks in database

        deletePopupWindow.dismiss();
    }
}