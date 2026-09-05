package com.example.carprojds2;

public class QueueCAraay<T extends Comparable<T>> {
    private T [] queue ;
    private int front;
    private int rear;
    private int size;
    public QueueCAraay() {
        this(10);
    }
    public QueueCAraay(int Capacity) {
        queue =  (T[]) new Comparable[Capacity];
        front = 0;
        rear = Capacity-1;
        this.size = 0;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public int getSize() {
        return size;
    }
    public boolean isFull() {
        return size == queue.length;
    }
    public boolean enqueue(T element) {
        if (isFull()) return false;
        rear = (rear + 1) % queue.length;
        queue[rear] = element;
        size++;
        return true;
    }
    public T dequeue() {
        if (isEmpty()) return null;
        T element = queue[front];
        queue[front] = null;
        front = (front + 1) % queue.length;
        size--;
        return element;
    }
    public T peek() {
        return queue[front];
    }
    public void clear(){
        front = 0;
        rear=queue.length-1;
        size=0;
    }
    public void print() {
        QueueCAraay<T> temp = new QueueCAraay<T>(getSize());
        while(!isEmpty()) {
            System.out.println(this.peek().toString());
            temp.enqueue(this.dequeue());
        }
        while(!temp.isEmpty()) {
            this.enqueue(temp.dequeue());
        }
    }
}
