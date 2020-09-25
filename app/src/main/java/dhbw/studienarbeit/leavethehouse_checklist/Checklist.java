package dhbw.studienarbeit.leavethehouse_checklist;

import java.util.List;

class Checklist {
    private String id;
    private String title;
    private String uid;
    private List<String> tasks;

    public Checklist(){}

    public Checklist(String id, String title, String uid, List<String> tasks) {
        this.id = id;
        this.title = title;
        this.uid = uid;
        this.tasks = tasks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }
}
