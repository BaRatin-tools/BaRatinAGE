package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.baratinage.ui.bam.BamItem.ITEM_TYPE;

public class BamItemList extends ArrayList<BamItem> {

    private ArrayList<BamItemListChangeListener> bamItemListChangeListeners;

    @FunctionalInterface
    public interface BamItemListChangeListener extends EventListener {
        public void onBamItemListChange(BamItemList bamItemList);
    }

    public BamItemList() {
        super();
        bamItemListChangeListeners = new ArrayList<>();
    }

    public BamItemList(List<BamItem> bamItems) {
        this();
        for (BamItem item : bamItems) {
            add(item);
        }

    }

    @Override
    public boolean add(BamItem item) {
        boolean res = super.add(item);
        fireChangeListeners();
        return res;
    }

    @Override
    public boolean remove(Object item) {
        boolean res = super.remove(item);
        fireChangeListeners();
        return res;
    }

    public void addChangeListener(BamItemListChangeListener updateListener) {
        bamItemListChangeListeners.add(updateListener);
    }

    public void removeChangeListener(BamItemListChangeListener updateListener) {
        bamItemListChangeListeners.remove(updateListener);
    }

    public void fireChangeListeners() {
        for (BamItemListChangeListener listener : bamItemListChangeListeners) {
            listener.onBamItemListChange(this);
        }
    }

    public BamItemList filterByType(ITEM_TYPE type) {
        BamItemList filteredList = new BamItemList(
                this
                        .stream()
                        .filter(item -> item.TYPE == type)
                        .toList());
        return filteredList;
    }

}
