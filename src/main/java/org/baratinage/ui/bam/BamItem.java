package org.baratinage.ui.bam;

import java.util.ArrayList;
// import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;
// import java.util.concurrent.SubmissionPublisher;
// import java.util.concurrent.Flow.Publisher;
// import java.util.concurrent.Flow.Subscriber;
// import java.util.concurrent.Flow.Subscription;

// import org.baratinage.jbam.BaM;
import org.baratinage.ui.container.FlexPanel;

abstract public class BamItem extends FlexPanel {

    private String uuid;
    // private List<BamItem> parents;
    private BamItem[] parents;
    // private List<BamItem> children;

    @FunctionalInterface
    public interface BamItemChangeListener extends EventListener {
        public void onUpdate(BamItem item);
    }

    private List<BamItemChangeListener> bamItemChangeListeners;
    // private Publisher<BamItem> publisher;
    // private List<Subscription> subscriptions;
    // // private Publisher<BamItem> publisher;

    public BamItem(BamItem... parents) {
        super(FlexPanel.AXIS.COL);
        this.setGap(5);
        // this.children = new ArrayList<>();
        // this.parents = new ArrayList<>(Arrays.asList(bamItems));

        this.uuid = UUID.randomUUID().toString();
        this.bamItemChangeListeners = new ArrayList<>();

        this.parents = parents;
        System.out.println(this.parents);
        for (BamItem parent : parents) {
            parent.addChangeListener((p) -> {
                System.out.println("PARENT_HAS_CHANGED >>> " + p);
            });
        }

        // this.subscriptions = new ArrayList<>();
        // this.publisher = new Publisher<>() {

        // private List<Subscriber<BamItem>> subscribers;
        // @Override
        // public void subscribe(Subscriber<BamItem> subscriber) {
        // this.subscribers.add(subscriber));
        // }

        // }
    }

    public String getUUID() {
        return this.uuid;
    }

    // // FIXME: when/how are children removed?
    // public void addChild(BamItem child) {
    // children.add(child);
    // // subscriptions.add(new Subscription() {

    // // @Override
    // // public void request(long n) {

    // // System.out.println("ON_REQUEST");
    // // }

    // // @Override
    // // public void cancel() {
    // // System.out.println("ON_CANCEL");
    // // }

    // // });

    // // child.sub
    // }

    // public void addParent(BamItem parent) {
    // parents.add(parent);
    // }

    public void hasChanged() {
        fireChangeListeners();
        // for (BamItem child : this.children) {
        // child.parentHasChanged(this);
        // }
    }

    public void addChangeListener(BamItemChangeListener updateListener) {
        this.bamItemChangeListeners.add(updateListener);
    }

    public void removeChangeListener(BamItemChangeListener updateListener) {
        this.bamItemChangeListeners.remove(updateListener);
    }

    public void fireChangeListeners() {
        for (BamItemChangeListener listener : this.bamItemChangeListeners) {
            listener.onUpdate(this);
        }
    }

    // @Override
    // public void subscribe(Subscriber<BamItem> subscriber) {

    // }

    // @Override
    // public void subscribe<T>(Subscriber<T extends BamItem> sub) {
    // System.out.println("ON_COMPLETE");
    // }

    // private void subscribe(BamItem item) {
    // // called when a child wants to be kept informed of any change

    // }

    // these four methods are used to follow other BamItem this bamItem depends on

    // @Override
    // public void onComplete() {
    // System.out.println("ON_COMPLETE");
    // }

    // @Override
    // public void onError(Throwable throwable) {
    // System.out.println("ON_ERROR");
    // throwable.printStackTrace();
    // }

    // @Override
    // public void onNext(BamItem item) {
    // System.out.println("ON_NEXT");
    // }

    // @Override
    // public void onSubscribe(Subscription subscription) {
    // System.out.println("ON_SUBSCRIBE");
    // this.subscriptions.add(subscription);
    // }

    public abstract String getName();

    @Deprecated
    public abstract void parentHasChanged(BamItem parent);

    public abstract String toJsonString();

    public abstract void fromJsonString(String jsonString);
}
