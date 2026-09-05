package CarProjDS2;

class UndoableOperation implements Comparable<UndoableOperation> {
    private static int nextOrder = 1;

    private final int order;
    private final String name;
    private final Runnable undoAction;
    private final Runnable redoAction;

    UndoableOperation(String name, Runnable undoAction, Runnable redoAction) {
        this.order = nextOrder++;
        this.name = name;
        this.undoAction = undoAction;
        this.redoAction = redoAction;
    }

    String getName() {
        return name;
    }

    void undo() {
        undoAction.run();
    }

    void redo() {
        redoAction.run();
    }

    @Override
    public int compareTo(UndoableOperation other) {
        return Integer.compare(order, other.order);
    }
}
