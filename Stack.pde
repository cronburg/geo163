
public class Stack<T> {
  
  private class Node {
    T data;
    Node next;
    Node(T data) {
      this.data = data;
      this.next = null;
    }
  }

  Node head;

  Stack() {
    this.head = null;
  }

  public void push(T data) {
    Node tmp = new Node(data);
    tmp.next = head;
    head = tmp;
  }

  public T pop() {
    if (null != head) {
      T data = head.data;
      head = head.next;
      return data;
    }
    return null;
  }

  public T peek() {
    if (null != head) return head.data;
    return null;
  }

  public boolean empty() { return head == null; }

}

