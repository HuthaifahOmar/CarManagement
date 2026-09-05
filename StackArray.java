package com.example.carprojds2;

public class StackArray<T extends Comparable<T>> {
    private T []stack;
    private int top;
    private int size;

    public StackArray(int size) {
        stack = (T[]) new Comparable[size];
        top = -1;
    }
    public StackArray() {
        this(10);
    }
    public void push(T element) {
        if (isfull()) throw new StackOverflowError();
        stack[++top] = element;
        size++;
    }
    public T pop() {
        if (isEmpty()) return null;
        T element = stack[top];
        stack[top] = null;
        top--;
        size--;
        return element;
    }

    public int getSize() {
        return size;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public T[] getStack() {
        return stack;
    }

    public void setStack(T[] stack) {
        this.stack = stack;
    }

    public boolean isEmpty() {
        return top == -1;
    }
    public boolean isfull(){
        return top==stack.length-1;
    }
    public T peek() {
        return stack[top];
    }
    public void printStack() {
        StackArray<T> stackArray2 = new StackArray<T>(stack.length);
        while (!isEmpty()) {
            stackArray2.push(this.pop());
            System.out.println(stackArray2.peek());
        }
        while (!stackArray2.isEmpty()) {
            this.push(stackArray2.pop());
        }
    }
    public boolean remove(T element) {
        if (isEmpty()) return false;
        boolean result = false;
        StackArray<T> stackArray2 = new StackArray<T>(this.getSize());
        while (!isEmpty()) {
            T element2 = pop();
            if (element2.equals(element)) {
                stackArray2.push(element2);
                size--;
                result = true;
            }
        }
        while (!stackArray2.isEmpty())
            this.push(stackArray2.pop());
        return result;
    }
}
