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
import java.util.List;

public class EditListActivity extends BaseActivity {

    private TextView title;

    private Button saveEditTextPopupBtn;
    private Button cancelEditTextBtnPopup;
    private Button deleteBtnPopup;
    private Button cancelDeleteBtnPopup;

    private ListView taskListView;

    PopupWindow editTextPopupWindow;
    private TextView inputType, editTextPopup;
    private PopupWindow deletePopupWindow;
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

        getSupportActionBar().setTitle(R.string.editList);

        taskListView = findViewById(R.id.taskOverviewListView);
        title = findViewById(R.id.accessTextView);
        Button editTitleBtn = findViewById(R.id.editTitleButton);
        Button newTaskBtn = findViewById(R.id.newTaskButton);
        Button deleteTaskBtn = findViewById(R.id.deleteButton);
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
                    editTextPopup.setError(getString(R.string.errorEmptyTextfield));
                } else {
                    // check if list title exists for current user - No equal titles are allowed.
                    allListsOfUser.forEach(checklist -> checklistTitleList.add(checklist.getTitle()));
                    if (checklistTitleList.stream().anyMatch(titleToMatch -> titleToMatch.equalsIgnoreCase(newTitle))) {
                        editTextPopup.setError(getString(R.string.errorTitleExists));
                    } else {
                        // write to database, if successfull change repository
                        DocumentReference ref = mDatabase.collection("Checklist").document(listId);

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
                }
            });
            cancelEditTextBtnPopup.setOnClickListener(v1 -> {
                editTextPopup.setText("");
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
                if (newTask.isEmpty()) {
                    editTextPopup.setError(getString(R.string.errorEmptyTextfield));
                } else {
                    editTextPopup.setText("");
                    taskList.add(newTask);
                    DocumentReference ref = mDatabase.collection("Checklist").document(listId);
                    ref.update("tasks", taskList)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Update db Title", "db updated with title ");
                                selectedList.setTasks(taskList);
                                repository.setSelectedList(selectedList);
                                ArrayAdapter<String> myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, taskList);
                                taskListView.setAdapter(myAdapter);
                                editTextPopupWindow.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Update database", "Error writing document", e);
                                Toast.makeText(EditListActivity.this, "Fehler beim speichern.", Toast.LENGTH_SHORT).show();
                            });
                }
            });

            cancelEditTextBtnPopup.setOnClickListener(v1 -> {
                editTextPopup.setText("");
//                Toast.makeText(EditListActivity.this, "clicked on cancel", Toast.LENGTH_SHORT).show();
                editTextPopupWindow.dismiss();
            });
        });


        // inflate the layout of the delete popup window
        View deletePopupView = inflater.inflate(R.layout.delete_popup_window, null);
        // create the popup window
        deletePopupWindow = new PopupWindow(deletePopupView, width, height, true);

        cancelDeleteBtnPopup = deletePopupWindow.getContentView().findViewById(R.id.cancelButton);
        deleteBtnPopup = deletePopupWindow.getContentView().findViewById(R.id.deleteButton);
        taskListPopup = deletePopupWindow.getContentView().findViewById(R.id.taskOverviewListView);
        TextView titlePopup = deletePopupWindow.getContentView().findViewById(R.id.titleToDeleteTextView);


        deleteListBtn.setOnClickListener(v -> {
                    titlePopup.setText(selectedList.getTitle());
                    deletePopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);


                    deleteBtnPopup.setOnClickListener(v1 -> {
                        DocumentReference ref = mDatabase.collection("Checklist").document(listId);
                        ref.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("delete List in db ", "list deleted ");
                                    titlePopup.setText("");
                                    deletePopupWindow.dismiss();
                                    Intent intent = new Intent(EditListActivity.this, ChecklistOverviewActivity.class);
                                    startActivity(intent);

                                })
                                .addOnFailureListener(e -> {
                                    titlePopup.setText("");
                                    Log.w("Update database", "Error writing document", e);
                                    Toast.makeText(EditListActivity.this, "Fehler beim löschen.", Toast.LENGTH_SHORT).show();
                                });
                    });
                    cancelDeleteBtnPopup.setOnClickListener(v1 -> {
                        titlePopup.setText("");
                        deletePopupWindow.dismiss();
                    });
                });


            deleteTaskBtn.setOnClickListener(v -> {

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

                        ref.update("tasks", taskList)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Update db Title", "db updated ");
                                    selectedList.setTasks(taskList);
                                    repository.setSelectedList(selectedList);
                                    myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, taskList);
                                    taskListView.setAdapter(myAdapter);

                                    deletePopupWindow.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("Update database", "Error writing document", e);
                                    Toast.makeText(EditListActivity.this, "Fehler beim löschen.", Toast.LENGTH_SHORT).show();
                                });
                        List<String> list = new ArrayList<String>();
                        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, (String[]) list.toArray(new String[0]));
                        taskListPopup.setAdapter(myAdapter);
//                    }
//                        else {
//                        Toast.makeText(EditListActivity.this, R.string.noEmptyTasks, Toast.LENGTH_SHORT).show();
//                    }
                });
                cancelDeleteBtnPopup.setOnClickListener(v1 ->{
                    List<String> list = new ArrayList<String>();
                    myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, (String[]) list.toArray(new String[0]));
                    taskListPopup.setAdapter(myAdapter);
                    deletePopupWindow.dismiss();
                });
            });


        }

        public void onBackPressed () {
            Intent intent = new Intent(EditListActivity.this, TaskChecklistActivity.class);
            startActivity(intent);
            finish();
        }
    }