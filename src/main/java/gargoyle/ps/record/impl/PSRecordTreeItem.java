package gargoyle.ps.record.impl;

import gargoyle.ps.record.IPSRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.List;

public final class PSRecordTreeItem extends TreeItem<IPSRecord> {

    private boolean isFirstTimeChildren = true;

    private PSRecordTreeItem(IPSRecord record) {
        super(record);
    }

    public static TreeItem<IPSRecord> create(IPSRecord record) {
        return new PSRecordTreeItem(record);
    }

    @Override
    public ObservableList<TreeItem<IPSRecord>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            ObservableList<TreeItem<IPSRecord>> result = FXCollections.emptyObservableList();
            IPSRecord rec = getValue();
            if (rec != null && rec.hasChildren()) {
                List<IPSRecord> records = rec.getChildren();
                if (records != null) {
                    ObservableList<TreeItem<IPSRecord>> children = FXCollections.observableArrayList();
                    for (IPSRecord childFile : records) {
                        children.add(create(childFile));
                    }
                    result = children;
                }
            }
            super.getChildren().setAll(result);
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        IPSRecord record = getValue();
        return record != null && record.isLeaf();
    }
}
