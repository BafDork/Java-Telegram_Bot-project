public class User {

    private String command;
    private String part;
    private String topic;
    private String theme;
    private Integer task;
    private boolean solution;
    private boolean taskNumber;

    User() {
        setCommand("part");
    }

    public void newParameters() {
        deleteAll();
        setCommand("part");
    }

    public void deleteAll() {
        setCommand(null);
        setPart(null);
        setTopic(null);
        setTheme(null);
        setTask(null);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Integer getTask() {
        return task;
    }

    public void setTask(Integer task) {
        this.task = task;
    }

    public boolean isSolution() {
        return solution;
    }

    public void setSolution(boolean solution) {
        this.solution = solution;
    }

    public boolean isTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(boolean taskNumber) {
        this.taskNumber = taskNumber;
    }
}