package model.huffman;

public class Node {

	private int frequency;
	private char character;
	private Node left;
	private Node right;

	public Node(char character, int frequency) {
		this.character = character;
		this.frequency = frequency;
		this.left = this.right = null;
	}

	public Node() {
		this.left = this.right = null;
	}

	public Node(char character) {
		this.character = character;
		this.left = this.right = null;
	}

	public Node(char character, int frequency, Node left, Node right) {
		this.character = character;
		this.frequency = frequency;
		this.left = left;
		this.right = right;
	}

	public Node(int frequency, Node left, Node right) {
		this.frequency = frequency;
		this.left = left;
		this.right = right;
	}

	public int getFrequency() {
		return frequency;
	}

	public char getCharacter() {
		return character;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

	public boolean isLeaf() {
		return left == null && right == null;
	}

}
