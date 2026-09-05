package CarProjDS2;


    public class AVL<T extends Comparable<T>> extends BST<T> {


        public AVL() {
            root = null;

        }

        public void insert(T key) {
            root = insert(root, key);
        }

        private TNode<T> insert(TNode<T> root, T key) {
            if (root == null) {
                return new TNode<T>(key);
            }
            if (key.compareTo(root.data) < 0) {
                root.setLeft(insert(root.getLeft(), key));
            } else {
                root.setRight(insert(root.getRight(), key));
            }
            return balance(root);
        }

      /*  public TNode<T> delete(T key) {
            TNode<T> ret = super.delete(key);
            root = balance(root);
            return ret;
        }*/

        private TNode<T> balance(TNode<T> root) {
            if (root == null) {
                return root;
            }
            int balance = getBalance(root);
            if (balance > 1) {
                if (getBalance(root.getLeft()) > 0) {
                    root = rotateRight(root);
                } else {
                    root = rotateLeftRight(root);
                }
            } else if (balance < -1) {
                if (getBalance(root.getRight()) < 0) {
                    root = rotateLeft(root);
                } else {
                    root = rotateRightLeft(root);
                }
            }
            return root;
        }

        private TNode<T> rotateRightLeft(TNode<T> root) {
            /*TNode<T> temp = root.getRight();
            root.setRight(rotateRight(temp));
            return rotateLeft(root);*/
            root.setRight(rotateRight(root.getRight()));
            return rotateLeft(root);
        }

        private TNode<T> rotateLeft(TNode<T> root) {
            TNode<T> temp = root.getRight();
            root.setRight(temp.getLeft());
            temp.setLeft(root);
            return temp;
        }

        private TNode<T> rotateLeftRight(TNode<T> root) {
            /*TNode<T> temp = root.getLeft();
            root.setLeft(rotateLeft(temp));
            return rotateRight(root);*/
            root.setLeft(rotateLeft(root.getLeft()));
            return rotateRight(root);
        }

        private TNode<T> rotateRight(TNode<T> root) {
            TNode<T> temp = root.getLeft();
            root.setLeft(temp.getRight());
            temp.setRight(root);
            return temp;
        }

        private int getBalance(TNode<T> root) {
            if (root == null) {
                return 0;
            }
            return getHeight(root.getLeft()) - getHeight(root.getRight());
        }

        private int getHeight(TNode<T> curr) {
            if (curr == null)
                return 0;
            if (curr.isLeaf())
                return 1;
            else
                return 1+ Math.max(getHeight(curr.getLeft()),getHeight(curr.getRight()));
        }

        public void print() {
            print(root);
        }

        private void print(TNode<T> root) {
            if (root == null) {
                return;
            }
            print(root.getLeft());
            System.out.print(root.data + " ");
            print(root.getRight());
        }

        public void traverseLevel() {
            int h = getHeight(root);
            int i;
            for (i = 0; i < h; i++) {
                System.out.println(printLevel(root, i, 0));
                System.out.println();
            }
        }

        private String printLevel(TNode<T> root, int i, int j) {

            if (root != null) {
                if (i == j)
                    return root.data + " ";
                if (j > i)
                    return "NULL";

                return printLevel(root.getLeft(), i, j + 1) + " " + printLevel(root.getRight(), i, j + 1);
            } else
                return "NULL";

        }
        public TNode<T> delete(T key) {
            root = delete(root, key);
            return root;
        }

        private TNode<T> delete(TNode<T> node, T key) {
            if (node == null) return null;

            int cmp = key.compareTo(node.data);
            if (cmp < 0) {
                node.setLeft(delete(node.getLeft(), key));
            } else if (cmp > 0) {
                node.setRight(delete(node.getRight(), key));
            } else {
                if (node.getLeft() == null || node.getRight() == null) {
                    TNode<T> temp = (node.getLeft() != null) ? node.getLeft() : node.getRight();
                    if (temp == null) {
                        node = null;
                    } else {
                        node = temp;
                    }
                } else {
                    TNode<T> minNode = getMinValueNode(node.getRight());
                    node.data = minNode.data;
                    node.setRight(delete(node.getRight(), minNode.data));
                }
            }

            if (node == null) return null;

            return balance(node);
        }

        private TNode<T> getMinValueNode(TNode<T> node) {
            TNode<T> current = node;
            while (current.getLeft() != null) {
                current = current.getLeft();
            }
            return current;
        }

    }

