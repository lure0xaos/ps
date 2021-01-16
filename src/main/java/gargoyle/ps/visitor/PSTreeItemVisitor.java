package gargoyle.ps.visitor;

import javafx.scene.control.TreeItem;

import java.util.function.Consumer;

public final class PSTreeItemVisitor<T> {

    private final Consumer<TreeItem<T>> consumer;

    private PSTreeItemVisitor(Consumer<TreeItem<T>> consumer) {
        this.consumer = consumer;
    }

    public static <T> PSTreeItemVisitor<T> create(Consumer<TreeItem<T>> consumer) {
        return new PSTreeItemVisitor<>(consumer);
    }

    public TreeItem<T> visit(TreeItem<T> item) {
        consumer.accept(item);
        if (!item.isLeaf()) {
            item.getChildren().forEach(this::visit);
        }
        return item;
    }
}
