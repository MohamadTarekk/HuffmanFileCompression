package model.utility;

import java.util.Comparator;

import model.huffman.Node;

public class NodeComparator implements Comparator<Node> {

	@Override
	public int compare(Node firstNode, Node secondNode) {
		return firstNode.getFrequency() - secondNode.getFrequency();
	}

}
