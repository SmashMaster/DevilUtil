package com.samrj.devil.math;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class MatrixStack<TYPE extends Matrix>
{
    private int size = 0;
    private Node head = null;
    
    public MatrixStack(TYPE head)
    {
        push(head);
    }
    
    public MatrixStack() {}
    
    public void push(TYPE m)
    {
        head = new Node((TYPE)m.copy(), head);
        size++;
    }
    
    public TYPE pop()
    {
        if (isEmpty()) throw new ArrayIndexOutOfBoundsException("Stack underflow");
        
        TYPE out = head.value;
        head = head.prev;
        size--;
        return out;
    }
    
    public void push()
    {
        push(peek());
    }
    
    public void mult(TYPE m)
    {
        peek().mult(m);
    }
    
    public void pushMult(TYPE m)
    {
        push();
        mult(m);
    }
    
    public TYPE peek()
    {
        if (isEmpty()) return null;
        return head.value;
    }
    
    public int getSize()
    {
        return size;
    }
    
    public boolean isEmpty()
    {
        return head == null;
    }
    
    private class Node
    {
        private TYPE value;
        private Node prev;
        
        private Node(TYPE value, Node prev)
        {
            this.value = value;
            this.prev = prev;
        }
    }
}
