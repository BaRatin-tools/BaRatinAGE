package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.lg.Lg;
import org.baratinage.utils.Misc;

public class BamItemList extends ArrayList<BamItem> {

    public BamItemList() {
        super();
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
        // notifyAllBamItems();
        fireChangeListeners();
        return res;
    }

    @Override
    public boolean remove(Object item) {
        boolean res = super.remove(item);
        // notifyAllBamItems();
        fireChangeListeners();
        return res;
    }

    @Override
    public BamItem get(int index) {
        if (index < 0 || index > size()) {
            return null;
        }
        return super.get(index);
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

    public static String[] getBamItemNames(BamItemList bamItemList) {
        int n = bamItemList.size();
        String[] names = new String[n];
        for (int k = 0; k < n; k++) {
            names[k] = bamItemList.get(k).bamItemNameField.getText();
        }
        return names;
    }

    public String getDefaultName(BamItemType type) {
        String shortName = Lg.text(type.id + "_short");
        BamItemList filteredList = filterByType(type);
        String[] filteredNames = getBamItemNames(filteredList);
        return Misc.getNextName(shortName, filteredNames);

    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

}
