package uk.co.computicake.angela.thesis;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Thread safe linked buffer without random access support.
 * Only tail or head can be accessed. 
 * @param <T>
 */
public class LinkedBuffer<T> implements Iterable<T> {

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private Container<T> curr = new Container<T>(null, LinkedBuffer.this.head, null);

			@Override
			public boolean hasNext() {
				synchronized (LinkedBuffer.this) {
					return curr.next != null;
				}
			}

			@Override
			public T next() {
				synchronized (LinkedBuffer.this) {
					if (hasNext()) {
						curr = curr.next;
						return curr.data;
					}
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported");
			}

		};
	}

	private class Container<T> {
		T data;
		Container<T> next;
		Container<T> prev;

		Container(T data, Container<T> next, Container<T> prev)
		{
			this.data = data;
			this.next = next;
			this.prev = prev;
		}
	}

	private Container<T> head = null;
	private Container<T> tail = null;
	private int count = 0;

	public LinkedBuffer(){
	}

	public synchronized void add(T data)
	{
		final Container<T> c = new Container<T>(data, null, tail);
		if (tail != null) tail.next = c;
		tail = c;
		if (head == null) head = c;
		++count;
	}

	public synchronized int size() {
		return count;
	}

	public synchronized T peek() {
		return head.data;
	}

	public synchronized T pop()
	{
		T data = null;
		if (head != null)
		{
			data = head.data;
			head = head.next;
			--count;
		}
		return data;
	}
	
	/**
	 * Returns a comma separated string of values enclosed in "[]".
	 */
	public synchronized String toString(){	
		StringBuilder builder = new StringBuilder(count*Utils.APRX_JSON_LENGTH).append("[");
		Iterator<T> it = this.iterator();
		// to avoid ending with a comma
		if(it.hasNext()){
			builder.append(it.next().toString());
		}
		while(it.hasNext()){
			builder.append(", "+ it.next().toString());
		}
		builder.append("]"); 
		return builder.toString();
	}
}

