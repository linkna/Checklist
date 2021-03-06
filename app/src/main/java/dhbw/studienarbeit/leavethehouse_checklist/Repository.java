package dhbw.studienarbeit.leavethehouse_checklist;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Repository {
    FirebaseUser currentUser;
    String uid;
    DocumentSnapshot userDocumentSnapshot;
    Checklist selectedList;
    List<Checklist> allListsOfUser;
    List<String> sharedLists;

    Map<Integer, String> checkedItems;

    //Singleton-Pattern

    private static final Repository instance = new Repository();

    public static Repository getInstance() {
        return instance;
    }

    private Repository() {
    }

    public void setSelectedList(Checklist selectedList) {
        this.selectedList = selectedList;
    }

    public Checklist getSelectedList() {
        return selectedList;
    }

    public List<Checklist> getAllListsOfUser() {
        return allListsOfUser;
    }

    public void setAllListsOfUser(List<Checklist> allListsOfUser) {
        this.allListsOfUser = allListsOfUser;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(FirebaseUser currentUser) {
        this.currentUser = currentUser;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public DocumentSnapshot getUserDocumentSnapshot() {
        return userDocumentSnapshot;
    }

    public void setUserDocumentSnapshot(DocumentSnapshot userDocumentSnapshot) {
        this.userDocumentSnapshot = userDocumentSnapshot;
    }

    public Map<Integer, String> getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(Map<Integer, String> checkedItems) {
        this.checkedItems = checkedItems;
    }

    public void setSharedLists(List<String> sharedLists) {
        this.sharedLists =sharedLists;
    }

    public List<String> getSharedLists() {
        return sharedLists;
    }


}
