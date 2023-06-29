package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

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

    public BamItemList filterByType(BamItemType type) {
        BamItemList filteredList = new BamItemList(
                this
                        .stream()
                        .filter(item -> item.TYPE == type)
                        .toList());
        return filteredList;
    }

    public BamItem getBamItemWithId(String id) {
        int n = size();
        for (int k = 0; k < n; k++) {
            BamItem item = get(k);
            if (item.ID.equals(id)) {
                return item;
            }
        }
        return null;
    }

}
