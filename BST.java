package com.example.carprojds2;



public class BST <T extends Comparable<T>> {
    class TNode<T extends Comparable<T>> {
        T data;
        TNode left;
        TNode right;

        public TNode(T data) {
            this.data = data;
        }

        boolean isLeaf() {
            return this.left == null && this.right == null;
        }

        boolean hasLeft() {
            return this.left != null;
        }

        boolean hasRight() {
            return this.right != null;
        }
        public TNode getLeft() {
            return left;
        }

        public void setLeft(TNode left) {
            this.left = left;
        }

        public TNode getRight() {
            return right;
        }

        public void setRight(TNode right) {
            this.right = right;
        }

        public String toString() {
            return "[" + String.valueOf(this.data) + "]";
        }
    }

    TNode<T> root;
    public boolean isEmpty() {
        return root == null;
    }
    public TNode<T> getRoot() {
        return root;
    }

    public void setRoot(TNode<T> root) {
        this.root = root;
    }
    public TNode<T> getParent(TNode<T> node) {
        TNode<T> parent = root;
        if(node.data.compareTo(root.data) < 0) {
            while(parent.left != node && parent.right != node) {
                parent = parent.left;
            }
        }
        return parent;
    }

    public void insert(T data){
        if (root == null){
            root = new TNode<T>(data);
        }
        else
            insert(data , root);

    }
    private void insert(T data, TNode<T> curr){
        if (curr != null) {
            if (curr.data.compareTo(data) > 0) {
                if (curr.left == null) {
                    curr.left = new TNode<T>(data);
                } else {
                    insert(data, curr.left);
                }
            }
            else if (curr.data.compareTo(data) < 0) {
                if (curr.right == null) {
                    curr.right = new TNode(data);
                }
                else
                    insert(data, curr.right);
            }
        }
    }
    void inOrderTraversal() {

        inOrderTraversal(root);

        System.out.println();

    }
    void inOrderTraversal(TNode curr) {

        if (curr != null) {

            inOrderTraversal(curr.left);

            System.out.print(" " + curr +":");//

            inOrderTraversal(curr.right);

        }

    }
    int size() {

        return size(root);

    }
    int size(TNode curr) {

        if (curr == null)

            return 0;

        return 1 + size(curr.left) + size(curr.right);

    }
    int height() {

        return height(root) - 1;

    }
    int height(TNode curr) {

        if (curr == null)

            return 0;

        return 1 + Math.max(height(curr.left), height(curr.right));

    }
    boolean find(T data) {

        TNode curr = root;

        while (curr != null) {

            int c = curr.data.compareTo(data);

            if (c == 0)

                return true;

            if (c < 0)

                curr = curr.right;

            else

                curr = curr.left;

        }

        return false;

    }
    int hightDiff(TNode curr) {

        if(curr == null)

            return 0;

        return Math.abs(height(curr.right) - height(curr.left));

    }


    public TNode<T> delete(T data) {
        TNode<T> current = root;
        TNode<T> parent = root;
        boolean isLeftChild = false;
        if (isEmpty())
            return null;
        while (current != null && !current.data.equals(data)) {
            parent = current;
            if (data.compareTo(current.data) < 0) {
                current = current.getLeft();
                isLeftChild = true;
            } else {
                current = current.getRight();
                isLeftChild = false;
            }
        }
        if (current == null)
            return null;
        if (!current.hasLeft() && !current.hasRight()) {
            if (current == root)
                root = null;
            else {
                if (isLeftChild)
                    parent.setLeft(null);
                else
                    parent.setRight(null);
            }
        } else if (current.hasLeft() && !current.hasRight()) {
            if (current == root) {
                root = current.getLeft();
            } else if (isLeftChild) {
                parent.setLeft(current.getLeft());
            } else {
                parent.setRight(current.getLeft());
            }
        } else if (current.hasRight() && !current.hasLeft()) {
            if (current == root) {
                root = current.getRight();
            } else if (isLeftChild) {
                parent.setLeft(current.getRight());
            } else {
                parent.setRight(current.getRight());
            }
        } else {
            TNode<T> successor = getSuccessor(current);
            if (current == root)
                root = successor;
            else if (isLeftChild) {
                parent.setLeft(successor);
            } else {
                parent.setRight(successor);
            }
            successor.setLeft(current.getLeft());
        }
        return current;
    }
    private TNode<T> getSuccessor(TNode<T> node) {
        TNode<T> parentOfSuccessor = node;
        TNode<T> successor = node;
        TNode<T> current = node.getRight();
        while (current != null) {
            parentOfSuccessor = successor;
            successor = current;
            current = current.getLeft();
        }
        if (successor.data.compareTo((T) node.getRight().data) != 0) { // fix successor connections
            parentOfSuccessor.setLeft(successor.getRight());
            successor.setRight(node.getRight());
        }
        return successor;
    }
    public boolean isAVL(TNode root) {
        return isBalancedAndGetHight(root)!=-1;
    }
    private  int isBalancedAndGetHight(TNode root) {
        if (root == null)return 0;
        int leftHight = isBalancedAndGetHight(root.left);
        if (leftHight == -1) return -1;
        int rightHight = isBalancedAndGetHight(root.right);
        if (rightHight == -1) return -1;
        if (Math.abs(leftHight - rightHight) > 1) {
            return -1;
        }
        return Math.max(leftHight, rightHight)+1;

    }



}










