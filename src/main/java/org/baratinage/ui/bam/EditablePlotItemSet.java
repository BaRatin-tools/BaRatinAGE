package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.baratinage.ui.plot.EditablePlotItem;

public class EditablePlotItemSet {

    private final List<EditablePlotItemAndBamItem> plotItems = new ArrayList<>();

    private static class EditablePlotItemAndBamItem {
        public final String id;
        public final BamItem bamItem;
        public final EditablePlotItem plotItem;

        // public boolean synced;

        public EditablePlotItemAndBamItem(String id, BamItem bamItem, EditablePlotItem plotItem) {
            this.id = id;
            this.bamItem = bamItem;
            this.plotItem = plotItem;

            // synced = true;
            // bamItem.addChangeListener(l -> {
            // synced = false;
            // });
        }

    }

    public void addItem(String id, BamItem bamItem, EditablePlotItem item) {
        if (getItem(bamItem, id).isEmpty()) {
            plotItems.add(new EditablePlotItemAndBamItem(id, bamItem, item));
        } else {
            System.err.println("Cannot add already existing item!");
        }
    }

    // public void removeItems(BamItem bamItem) {

    // }

    private Optional<EditablePlotItemAndBamItem> getItem(BamItem bamItem, String id) {
        return plotItems.stream()
                .filter(i -> i.bamItem.ID.equals(bamItem.ID) & i.id.equals(id)).findFirst();
    }

    public EditablePlotItem getEditablePlotItem(BamItem bamItem, String id) {
        Optional<EditablePlotItemAndBamItem> result = getItem(bamItem, id);
        if (result.isPresent()) {
            return result.get().plotItem;
        }
        return null;
    }

    public List<EditablePlotItem> getEditablePlotItems(BamItem bamItem) {
        List<EditablePlotItem> items = new ArrayList<>();
        plotItems.stream().filter(i -> i.bamItem.ID.equals(bamItem.ID)).forEach(i -> items.add(i.plotItem));
        return items;
    }

    public List<EditablePlotItem> getEditablePlotItems() {
        List<EditablePlotItem> items = new ArrayList<>();
        plotItems.stream().forEach(i -> items.add(i.plotItem));
        return items;
    }
}
