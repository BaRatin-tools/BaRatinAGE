package org.baratinage.ui.container;

import java.util.ArrayList;
import java.util.List;

public class ChangingRowColPanel extends RowColPanel {

    public ChangingRowColPanel(AXIS axis) {
        super(axis);
    }

    public ChangingRowColPanel() {
        super(AXIS.ROW);
    }

    @FunctionalInterface
    public interface ToBeNotified {
        public void notify(ChangingRowColPanel object);
    }

    private final List<ToBeNotified> followers = new ArrayList<>();

    public void addFollower(ToBeNotified follower) {
        followers.add(follower);
    }

    public void removeFollower(ToBeNotified follower) {
        followers.remove(follower);
    }

    protected void notifyFollowers() {
        for (ToBeNotified follower : followers) {
            follower.notify(this);
        }
    }

}
