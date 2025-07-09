import java.util.ArrayList;

public class EpicTask extends Task {
    private ArrayList<Integer> subtaskIdList;

    public EpicTask(String name, String description) {
        super(name, description);
        subtaskIdList = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIdList() {
        return subtaskIdList;
    }

    public void addSubTask(int subtaskId) {
        subtaskIdList.add(subtaskId);
    }

    public void removeSubTask(int subtaskId) {
        subtaskIdList.remove(subtaskId);
    }

    public void clearEpic() {
        subtaskIdList.clear();
    }

    @Override
    public String toString() {
        return "EpicTask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subTaskIdList=" + subtaskIdList +
                '}';
    }

}