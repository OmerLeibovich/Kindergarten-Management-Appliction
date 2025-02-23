package Objects;

public class ChildStatus {
    private Child child;
    private boolean isApproved;

    public ChildStatus(Child child, boolean isApproved) {
        this.child = child;
        this.isApproved = isApproved;
    }
    public ChildStatus() {
    }

    public Child getChild() {
        return child;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}
