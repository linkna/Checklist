package dhbw.studienarbeit.leavethehouse_checklist;


import java.util.List;

class Checklist {
    private String id;
    private String title;
    private String userid;
    private List<String> tasks;



    public Checklist(){}

    public Checklist(String id, String title, String userid, List<String> tasks) {
        this.id = id;
        this.title = title;
        this.userid = userid;
        this.tasks = tasks;
    }

    public static class ChecklistBuilder{
        private String id;
        private String title;
        private String userid;
        private List<String> tasks;

        public ChecklistBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public ChecklistBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public ChecklistBuilder setUserid(String userid) {
            this.userid = userid;
            return this;
        }

        public ChecklistBuilder setTasks(List<String> tasks) {
            this.tasks = tasks;
            return this;
        }

        public Checklist build(){
            return new Checklist(id, title, userid, tasks);
        }
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

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }
}
