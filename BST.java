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
    public TNode getParent(TNode root, TNode target) {
        // Base case: if tree is empty, target is null, or target is the root node
        if (root == null || target == null || root == target) {
            return null;
        }

        TNode parent = null;
        TNode current = root;

        // Traverse the tree using BST properties
        while (current != null && current.data != target.data) {
            parent = current;

            if (target.data.compareTo( current.data)<0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        // If the target node was found in the BST, return its parent
        if (current != null) {
            return parent;
        }

        // If the target node does not exist in the BST
        return null;
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
    public void preOrder(TNode node) {
        if (node == null) return;

        System.out.print(node.data + " ");
        preOrder(node.left);
        preOrder(node.right);
    }

    // Postorder: Left -> Right -> Root
    public void postOrder(TNode node) {
        if (node == null) return;

        postOrder(node.left);
        postOrder(node.right);
        System.out.print(node.data + " ");
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
    /*public static TreeNode convertToAVL(TreeNode root) {
        if (root == null) {
            return null;
        }

        // Dummy pseudo-root to simplify rotations at the top of the tree
        TreeNode grandParent = new TreeNode(0);
        grandParent.right = root;

        // Step 1: Flatten BST into a right-skewed backbone (vine)
        int nodeCount = createBackbone(grandParent);

        // Step 2: Rebalance the vine into a height-balanced tree
        createBalancedTree(grandParent, nodeCount);

        return grandParent.right;
    }

    // Flattens the BST into a linked chain going right using right-rotations
    private static int createBackbone(TreeNode grandParent) {
        int count = 0;
        TreeNode current = grandParent.right;

        while (current != null) {
            if (current.left != null) {
                // Right rotation to convert left child into root/right chain
                TreeNode leftChild = current.left;
                current.left = leftChild.right;
                leftChild.right = current;

                // Attach to grandparent/parent
                grandParent.right = leftChild;
                current = leftChild;
            } else {
                // Move down the right vine
                count++;
                grandParent = current;
                current = current.right;
            }
        }
        return count;
    }

    // Reconstructs a balanced tree from the right-skewed vine
    private static void createBalancedTree(TreeNode grandParent, int count) {
        // Find the height of the largest perfect binary sub-tree
        int m = (int) (Math.pow(2, Math.floor(Math.log(count + 1) / Math.log(2))) - 1);

        // Perform initial rotations to handle leftover bottom-level leaves
        compress(grandParent, count - m);

        // Compress iteratively to build subtrees level by level
        while (m > 1) {
            m /= 2;
            compress(grandParent, m);
        }
    }

    // Performs 'count' left-rotations down the right vine
    private static void compress(TreeNode grandParent, int count) {
        TreeNode current = grandParent.right;

        for (int i = 0; i < count; i++) {
            if (current == null || current.right == null) break;

            TreeNode child = current.right;
            current.right = child.left;
            child.left = current;

            grandParent.right = child;
            grandParent = child;
            current = child.right;
        }
    }*/

    /*public int getMinimumDifference(TreeNode root) {
        inOrder(root);
        return minDiff;
    }

    private void inOrder(TreeNode node) {
        if (node == null) {
            return;
        }

        // 1. Traverse left subtree
        inOrder(node.left);

        // 2. Process current node
        if (prev != null) {
            minDiff = Math.min(minDiff, node.val - prev);
        }
        prev = node.val; // Update previous node value

        // 3. Traverse right subtree
        inOrder(node.right);
    }*/


}










