package dhbw.studienarbeit.leavethehouse_checklist;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditListActivity extends BaseActivity {

    private TextView title;

    private Button editTitleBtn, newTaskBtn, saveEditTextPopupBtn, cancelBtnPopup;

    private ListView taskList;

    PopupWindow editTextPopupWindow;
    private TextView inputType, editTextPopup;
    private View editTextPopupView;
    private PopupWindow deletePopupWindow;
    private View deletePopupView;
    private FirebaseFirestore mDatabase;
    private List<Checklist> allListsOfUser;


    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        mDatabase=FirebaseFirestore.getInstance();

        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();
        allListsOfUser = repository.getAllListsOfUser();

        getSupportActionBar().setTitle(R.string.editList);


        taskList = findViewById(R.id.taskOverviewListView);
        title = findViewById(R.id.titleTextView);
        editTitleBtn = findViewById(R.id.editTitleButton);
        newTaskBtn = findViewById(R.id.newTaskButton);


        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, selectedList.getTasks());
        taskList.setAdapter(mAdapter);

        title.setText(selectedList.getTitle());


        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        editTextPopupView = inflater.inflate(R.layout.edittext_popup_window, null);

        // create the popup window
//        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.8);
//        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        editTextPopupWindow = new PopupWindow(editTextPopupView, width, height, true);

        // elements in editText popup Window
        inputType = editTextPopupWindow.getContentView().findViewById(R.id.titleTextView);
        editTextPopup = editTextPopupWindow.getContentView().findViewById(R.id.editText);
        saveEditTextPopupBtn = editTextPopupWindow.getContentView().findViewById(R.id.saveButton);
        cancelBtnPopup = editTextPopupWindow.getContentView().findViewById(R.id.cancelButton);


        editTitleBtn.setOnClickListener(v -> {
            inputType.setText(R.string.newTitle);
            // show the popup window
            editTextPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            saveEditTextPopupBtn.setOnClickListener(v1 -> {
                String newTitle = editTextPopup.getText().toString();
                ArrayList<String> checklistTitleList = new ArrayList<>();

                // check if list title exists for current user - No equal titles are allowed.
               allListsOfUser.forEach(checklist -> {
                   checklistTitleList.add(checklist.getTitle());
                });
                if (checklistTitleList.stream().anyMatch(titleToMatch -> titleToMatch.equalsIgnoreCase(newTitle))) {
                    editTextPopup.setError(getString(R.string.errorTitleExists));
                } else {
                    // write to database, if successfull change repository
                    String id = selectedList.getId();
                    DocumentReference ref= mDatabase.collection("Checklist").document(id);

                    ref.update("title", newTitle)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Update db Title", "db updated with title ");
                                selectedList.setTitle(newTitle);
                                repository.setSelectedList(selectedList);
                                title.setText(newTitle);
                                editTextPopupWindow.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Update database", "Error writing document", e);
                                Toast.makeText(EditListActivity.this, "Fehler beim speichern.", Toast.LENGTH_SHORT).show();
                            });
                }
                Toast.makeText(EditListActivity.this, "clicked on save", Toast.LENGTH_SHORT).show();
//                editTextPopupWindow.dismiss();
            });
            cancelBtnPopup.setOnClickListener(v2 -> {
//                Toast.makeText(EditListActivity.this, "clicked on cancel", Toast.LENGTH_SHORT).show();
                editTextPopupWindow.dismiss();
            });
        });


        newTaskBtn.setOnClickListener(v -> {
            inputType.setText(R.string.newTask);
            // show the popup window
            editTextPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

            saveEditTextPopupBtn.setOnClickListener(v1 -> {
                String newTask = editTextPopup.getText().toString();
                List<String> tasks = selectedList.getTasks();
                tasks.add(newTask);

                String id = selectedList.getId();
                DocumentReference ref= mDatabase.collection("Checklist").document(id);

                ref.update("tasks", tasks)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Update db Title", "db updated with title ");
                            selectedList.setTasks(tasks);
                            repository.setSelectedList(selectedList);
                            ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, tasks);
                            taskList.setAdapter(myAdapter);
                            editTextPopupWindow.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Update database", "Error writing document", e);
                            Toast.makeText(EditListActivity.this, "Fehler beim speichern.", Toast.LENGTH_SHORT).show();
                        });
                Toast.makeText(EditListActivity.this, "clicked on save", Toast.LENGTH_SHORT).show();
            });

            cancelBtnPopup.setOnClickListener(v2 -> {
//                Toast.makeText(EditListActivity.this, "clicked on cancel", Toast.LENGTH_SHORT).show();
                editTextPopupWindow.dismiss();
            });

//      
        });


        // inflate the layout of the delete popup window
        deletePopupView = inflater.inflate(R.layout.delete_popup_window, null);
        // create the popup window
        deletePopupWindow = new PopupWindow(deletePopupView, width, height, true);




    }

    public void onBackPressed (){
        Intent intent = new Intent(EditListActivity.this, TaskChecklistActivity.class);
        startActivity(intent);
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