package common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tree<T> implements Iterable<Tree.Node<T>> {

	public static class Node<T> {

		private T value;
		private Node<T> parent;
		private ArrayList<Node<T>> childs;

		public Node() {
			this(null);
		}

		public Node(T value) {
			this.value = value;

			parent = null;
			childs = new ArrayList<>();
		}

		public void addChild(Node<T> node) {
			if (node != null) {
				node.setParent(null);
				node.parent = this;
			}

			childs.add(node);
		}

		public Node<T> addChild(T value) {
			Node<T> result = new Node<>(value);
			result.parent = this;
			childs.add(result);

			return result;
		}

		public void clearChilds() {
			childs.clear();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Node))
				return false;

			Node<?> other = (Node<?>) obj;
			if (childs == null) {
				if (other.childs != null)
					return false;
			} else if (!childs.equals(other.childs))
				return false;

			if (parent == other.parent)
				return false;

			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;

			return true;
		}

		public Node<T> getChild(int index) {
			return childs.get(index);
		}

		public Node<T> getChild(T value) {
			int index = indexOfChild(value);
			if (index == -1)
				return null;

			return childs.get(index);
		}

		public int getChildCount() {
			return childs.size();
		}

		public int getMaxDepth() {
			if (childs.size() == 0)
				return 0;

			int result = 0;
			for (int i = 0; i < childs.size(); i++) {
				Node<T> node = childs.get(i);
				int depth = 1 + node.getMaxDepth();
				if (depth > result)
					result = depth;
			}

			return result;
		}

		public Node<T> getParent() {
			return parent;
		}

		public List<T> getPathFromRoot() {
			ArrayList<T> positionsList = new ArrayList<>();
			Node<T> node = this;
			while (node != null) {
				positionsList.add(node.getValue());
				node = node.getParent();
			}

			ArrayList<T> result = new ArrayList<>();
			for (int i = 0; i < positionsList.size(); i++)
				result.add(positionsList.get(positionsList.size() - i - 1));

			return result;
		}

		public T getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (childs == null ? 0 : childs.hashCode());
			result = prime * result + (value == null ? 0 : value.hashCode());
			return result;
		}

		public int indexOfChild(Node<T> child) {
			return childs.indexOf(child);
		}

		public int indexOfChild(T value) {
			if (value != null)
				for (int i = 0; i < childs.size(); i++) {
					Node<T> node = childs.get(i);
					if (node != null && value.equals(node.getValue()))
						return i;
				}
			else
				for (int i = 0; i < childs.size(); i++) {
					Node<T> node = childs.get(i);
					if (node != null && node.getValue() == null)
						return i;
				}

			return -1;
		}

		public void pruneAllDepthsLessThan(int depth) {
			for (int i = 0; i < childs.size(); i++) {
				Node<T> child = childs.get(i);

				int d = child.getMaxDepth() + 1;
				if (d < depth) {
					childs.remove(i);
					i--;
				} else
					child.pruneAllDepthsLessThan(depth - 1);
			}
		}

		public Node<T> removeChild(int index) {
			return childs.remove(index);
		}

		public boolean removeChild(Node<T> node) {
			return childs.remove(node);
		}

		public Node<T> removeChild(T value) {
			int index = indexOfChild(value);
			if (index == -1)
				return null;
			return childs.remove(index);
		}

		public void setParent(Node<T> parent) {
			if (this.parent != null)
				this.parent.removeChild(this);

			this.parent = parent;
			if (this.parent != null)
				this.parent.childs.add(this);
		}

		public void setValue(T value) {
			this.value = value;
		}

	}

	private class TreeIterator implements Iterator<Node<T>> {

		Node<T> currentNode = null;
		boolean gotNext = false;
		boolean finished = false;

		private TreeIterator() {

		}

		void getNext() {
			try {
				if (currentNode == null) {
					currentNode = root;
					if (currentNode == null)
						throw new NullPointerException("Root is null.");

					while (currentNode.getChildCount() > 0) {
						currentNode = currentNode.getChild(0);
						if (currentNode == null)
							throw new NullPointerException("Node is null.");
					}
				} else
					while (true) {
						Node<T> parentNode = currentNode.parent;
						if (parentNode == null) {
							currentNode = null;
							finished = true;
							return;
						}

						int index = parentNode.indexOfChild(currentNode);
						index++;
						if (index < parentNode.getChildCount()) {
							currentNode = parentNode.getChild(index);
							if (currentNode == null)
								throw new NullPointerException("Root is null.");

							while (currentNode.getChildCount() > 0) {
								currentNode = currentNode.getChild(0);
								if (currentNode == null)
									throw new NullPointerException("Node is null.");
							}

							break;
						}

						currentNode = parentNode;
					}
			} finally {
				gotNext = true;
			}
		}

		@Override
		public boolean hasNext() {
			if (finished)
				return false;
			if (!gotNext)
				getNext();

			return currentNode != null;
		}

		@Override
		public Node<T> next() {
			if (finished)
				return null;
			if (!gotNext)
				getNext();

			gotNext = false;

			return currentNode;
		}

		@Override
		public void remove() {

		}

	}

	// test iterator
	public static void main(String... args) {
		Tree<String> tree = new Tree<>("root");
		Node<String> child = tree.addChild("e1");
		child.addChild("e11");
		Node<String> child1 = child.addChild("e12");
		child1.addChild("e121");
		child1.addChild("e122");
		child1.addChild("e123");
		child1.addChild("e124");
		child1.addChild("e125");
		child.addChild("e12");
		child = tree.addChild("e2");
		child.addChild("e21");
		child.addChild("e22");
		tree.addChild("e3");
		child = tree.addChild("e4");
		child.addChild("e41");
		child1 = child.addChild("e42");
		child1.addChild("e421");
		Node<String> child2 = child1.addChild("e422");
		child2.addChild("e4221");
		child2.addChild("e4222");
		child2 = child1.addChild("e423");
		child2.addChild("e4231");
		child1 = child.addChild("e43");

		for (Node<String> node : tree)
			System.out.println(node.getValue());
	}

	private Node<T> root;

	public Tree() {
		this(new Node<T>(null));
	}

	public Tree(List<Node<T>> nodes) {
		this();

		for (Node<T> node : nodes)
			root.addChild(node);
	}

	public Tree(Node<T> root) {
		this.root = root;
	}

	public Tree(T rootValue) {
		this(new Node<>(rootValue));
	}

	public void addChild(Node<T> node) {
		root.addChild(node);
	}

	public Node<T> addChild(T value) {
		return root.addChild(value);
	}

	public void clearChilds() {
		root.clearChilds();
	}

	@Override
	public boolean equals(Object obj) {
		return root.equals(obj);
	}

	public Node<T> getChild(int index) {
		return root.getChild(index);
	}

	public Node<T> getChild(T value) {
		return root.getChild(value);
	}

	public int getChildCount() {
		return root.getChildCount();
	}

	public int getMaxDepth() {
		return root.getMaxDepth();
	}

	public Node<T> getRoot() {
		return root;
	}

	public T getValue() {
		return root.getValue();
	}

	@Override
	public int hashCode() {
		return root.hashCode();
	}

	public int indexOfChild(Node<T> child) {
		return root.indexOfChild(child);
	}

	public int indexOfChild(T value) {
		return root.indexOfChild(value);
	}

	@Override
	public Iterator<Node<T>> iterator() {
		return new TreeIterator();
	}

	public void pruneAllDepthsLessThan(int depth) {
		root.pruneAllDepthsLessThan(depth);
	}

	public Node<T> removeChild(int index) {
		return root.removeChild(index);
	}

	public boolean removeChild(Node<T> node) {
		return root.removeChild(node);
	}

	public Node<T> removeChild(T value) {
		return root.removeChild(value);
	}

	public void setValue(T value) {
		root.setValue(value);
	}

	@Override
	public String toString() {
		return root.toString();
	}

}