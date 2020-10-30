package dhbw.studienarbeit.leavethehouse_checklist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EditListActivity extends BaseActivity {

    private static final String TAG = "EditListActivity";
    private TextView title;

    private Button saveEditTextPopupBtn;
    private Button cancelEditTextBtnPopup;
    private Button deleteBtnPopup;
    private Button cancelDeleteBtnPopup;

    private ListView taskListView;

    private PopupWindow editTextPopupWindow, deletePopupWindow;
    private TextView inputType, editTextPopup;
    private FirebaseFirestore mDatabase;
    private List<Checklist> allListsOfUser;
    private ListView taskListPopup;
    private List<String> tasksToDelete;
    private String listId;
    private ArrayAdapter<String> myAdapter;


    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        mDatabase = FirebaseFirestore.getInstance();

        Repository repository = Repository.getInstance();
        Checklist selectedList = repository.getSelectedList();
        allListsOfUser = repository.getAllListsOfUser();

        listId = selectedList.getId();
        List<String> taskList = selectedList.getTasks();

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.editList);

        taskListView = findViewById(R.id.taskOverviewListView);
        title = findViewById(R.id.accessTextView);
        Button editTitleBtn = findViewById(R.id.editTitleButton);
        Button addTaskBtn = findViewById(R.id.addTaskButton);
        Button deleteTaskBtn = findViewById(R.id.deleteTasksButton);
        Button deleteListBtn = findViewById(R.id.deleteListButton);

        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, taskList);
        taskListView.setAdapter(myAdapter);

        title.setText(selectedList.getTitle());

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View editTextPopupView = inflater.inflate(R.layout.edittext_popup_window, null);

        // create the popup window
//        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.8);
//        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        editTextPopupWindow = new PopupWindow(editTextPopupView, width, height, true);

        // elements in editText popup Window
        inputType = editTextPopupWindow.getContentView().findViewById(R.id.accessTextView);
        editTextPopup = editTextPopupWindow.getContentView().findViewById(R.id.editText);
        saveEditTextPopupBtn = editTextPopupWindow.getContentView().findViewById(R.id.saveButton);
        cancelEditTextBtnPopup = editTextPopupWindow.getContentView().findViewById(R.id.cancelButton);


        editTitleBtn.setOnClickListener(v -> {
            inputType.setText(R.string.newTitle);
            // show the popup window
            editTextPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

            saveEditTextPopupBtn.setOnClickListener(v1 -> {
                ArrayList<String> checklistTitleList = new ArrayList<>();
                String newTitle = editTextPopup.getText().toString();
                editTextPopup.setText("");

                if (newTitle.isEmpty()) {
                    editTextPopup.setError(getString(R.string.error_empty_textfield));
                } else {
                    // check if list title exists for current user - No equal titles are allowed.
                    allListsOfUser.forEach(checklist -> checklistTitleList.add(checklist.getTitle()));
                    if (checklistTitleList.stream().anyMatch(titleToMatch -> titleToMatch.equalsIgnoreCase(newTitle))) {
                        editTextPopup.setError(getString(R.string.error_Title_Exists));
                    } else {
                        // write to database, if successful change repository
                        DocumentReference ref = mDatabase.collection("Checklist").document(listId);

                        ref.update("title", newTitle)
                                .addOnSuccessListener(aVoid -> {
                                    selectedList.setTitle(newTitle);
                                    repository.setSelectedList(selectedList);
                                    title.setText(newTitle);
                                    editTextPopupWindow.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, getString(R.string.error_writing_to_db), e);
                                    Toast.makeText(EditListActivity.this, getString(R.string.saving_failed), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            });
            cancelEditTextBtnPopup.setOnClickListener(v1 -> {
                editTextPopup.setText("");
                editTextPopupWindow.dismiss();
            });
        });


        addTaskBtn.setOnClickListener(v -> {
            TextView taskEditText = findViewById(R.id.taskEditText);
            String task = taskEditText.getText().toString();
            if (task.isEmpty() || task.trim().length() == 0) {
                taskEditText.setError(getString(R.string.error_empty_textfield));
            } else {
                if (taskList.contains(task.trim())) {
                    taskEditText.setError(getString(R.string.error_Task_Exists));
                } else {
                    taskList.add(task.trim());
                    Collections.sort(taskList, String.CASE_INSENSITIVE_ORDER);
                    taskEditText.setText("");
                    // write to database, if successful change repository
                    DocumentReference ref = mDatabase.collection("Checklist").document(listId);
                    ref.update("tasks", taskList)
                            .addOnSuccessListener(aVoid -> {
                                selectedList.setTasks(taskList);
                                repository.setSelectedList(selectedList);
                                myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, taskList);
                                taskListView.setAdapter(myAdapter);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, getString(R.string.error_writing_to_db), e);
                                Toast.makeText(EditListActivity.this, getString(R.string.saving_failed), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });


        // inflate the layout of the delete popup window
        View deletePopupView = inflater.inflate(R.layout.delete_popup_window, null);
        // create the popup window
        deletePopupWindow = new PopupWindow(deletePopupView, width, height, true);

        cancelDeleteBtnPopup = deletePopupWindow.getContentView().findViewById(R.id.cancelButton);
        deleteBtnPopup = deletePopupWindow.getContentView().findViewById(R.id.deleteTasksButton);
        taskListPopup = deletePopupWindow.getContentView().findViewById(R.id.taskOverviewListView);
        TextView titlePopup = deletePopupWindow.getContentView().findViewById(R.id.titleToDeleteTextView);


        deleteListBtn.setOnClickListener(v -> {
            titlePopup.setVisibility(View.VISIBLE);
            titlePopup.setText(selectedList.getTitle());
            deletePopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);


            deleteBtnPopup.setOnClickListener(v1 -> {
                DocumentReference ref = mDatabase.collection("Checklist").document(listId);
                ref.delete()
                        .addOnSuccessListener(aVoid -> {
                            titlePopup.setText("");
                            titlePopup.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(EditListActivity.this, ChecklistOverviewActivity.class));
                            finish();

                        })
                        .addOnFailureListener(e -> {
                            titlePopup.setText("");
                            titlePopup.setVisibility(View.INVISIBLE);
                            deletePopupWindow.dismiss();
                            Log.d(TAG, getString(R.string.error_writing_to_db), e);
                            Toast.makeText(EditListActivity.this, getString(R.string.deletion_failed), Toast.LENGTH_SHORT).show();
                        });
            });
            cancelDeleteBtnPopup.setOnClickListener(v1 -> {
                titlePopup.setText("");
                titlePopup.setVisibility(View.INVISIBLE);
                deletePopupWindow.dismiss();
            });
        });


        deleteTaskBtn.setOnClickListener(v -> {
            taskListPopup.setVisibility(View.VISIBLE);

            tasksToDelete = new ArrayList<>();
            //get checked items
            SparseBooleanArray checked = taskListView.getCheckedItemPositions();
            for (int i = 0; i < checked.size(); i++)
                if (checked.valueAt(i)) {
                    String item = taskListView.getItemAtPosition(checked.keyAt(i)).toString();
                    tasksToDelete.add(item);
                }

            // show the popup window
            deletePopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasksToDelete);
            taskListPopup.setAdapter(myAdapter);

            deleteBtnPopup.setOnClickListener(v1 -> {
//                    if (tasksToDelete.size() != taskList.size()) {
                for (String task : tasksToDelete) {
                    taskList.remove(task);
                }

                DocumentReference ref = mDatabase.collection("Checklist").document(listId);
                // write to database, if successful change repository
                ref.update("tasks", taskList)
                        .addOnSuccessListener(aVoid -> {
                            selectedList.setTasks(taskList);
                            repository.setSelectedList(selectedList);
                            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, taskList);
                            taskListView.setAdapter(myAdapter);
                            taskListPopup.setVisibility(View.INVISIBLE);
                            deletePopupWindow.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAG, getString(R.string.error_writing_to_db), e);
                            Toast.makeText(EditListActivity.this, getString(R.string.deletion_failed), Toast.LENGTH_SHORT).show();
                        });

                myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[0]);
                taskListPopup.setAdapter(myAdapter);
            });
            cancelDeleteBtnPopup.setOnClickListener(v1 -> {
                myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[0]);
                taskListPopup.setAdapter(myAdapter);
                taskListPopup.setVisibility(View.INVISIBLE);
                deletePopupWindow.dismiss();
            });
        });


    }

    @Override
    protected void onStop() {
        if(deletePopupWindow.isShowing()) {
            deletePopupWindow.dismiss();
        }
        super.onStop();
    }


    public void onBackPressed() {
        Intent intent = new Intent(EditListActivity.this, TaskChecklistActivity.class);
        startActivity(intent);
        finish();
    }
}