package edu.smu.smusql.table;

import java.util.*;

public class BPlusTree {
    private Node root;
    private Node firstLeaf;
    private static final int ORDER = 128;

    private static class Node {
        List<String> keys;
        int keyCount;
        Node parent;
        
        Node() {
            this.keys = new ArrayList<>();
            this.keyCount = 0;
            this.parent = null;
        }
    }

    private static class InternalNode extends Node {
        List<Node> children;
        
        InternalNode() {
            super();
            this.children = new ArrayList<>();
        }
    }

    private static class LeafNode extends Node {
        List<RID> values;
        LeafNode next;
        
        LeafNode() {
            super();
            this.values = new ArrayList<>();
            this.next = null;
        }
    }

    public BPlusTree() {
        root = new LeafNode();
        firstLeaf = (LeafNode) root;
    }

    public void insert(String key, RID rid) {
        if (key == null || rid == null) return;
        
        LeafNode leaf = findLeafNode(key);
        int insertionPoint = 0;
        
        while (insertionPoint < leaf.keyCount && 
               leaf.keys.get(insertionPoint).compareTo(key) < 0) {
            insertionPoint++;
        }
        
        leaf.keys.add(insertionPoint, key);
        leaf.values.add(insertionPoint, rid);
        leaf.keyCount++;

        if (leaf.keyCount >= ORDER) {
            splitLeaf(leaf);
        }
    }

    private LeafNode findLeafNode(String key) {
        Node current = root;
        while (current instanceof InternalNode) {
            InternalNode internal = (InternalNode) current;
            int i = 0;
            while (i < internal.keyCount && 
                   internal.keys.get(i).compareTo(key) <= 0) {
                i++;
            }
            current = internal.children.get(i);
        }
        return (LeafNode) current;
    }

    public RID search(String key) {
        if (key == null) return null;
        
        LeafNode leaf = findLeafNode(key);
        for (int i = 0; i < leaf.keyCount; i++) {
            if (leaf.keys.get(i).equals(key)) {
                return leaf.values.get(i);
            }
        }
        return null;
    }

    public void delete(String key) {
        if (key == null) return;
        
        LeafNode leaf = findLeafNode(key);
        for (int i = 0; i < leaf.keyCount; i++) {
            if (leaf.keys.get(i).equals(key)) {
                leaf.keys.remove(i);
                leaf.values.remove(i);
                leaf.keyCount--;
                return;
            }
        }
    }

    private void splitLeaf(LeafNode leaf) {
        LeafNode newLeaf = new LeafNode();
        int midpoint = (ORDER + 1) / 2;
        
        for (int i = midpoint; i < leaf.keyCount; i++) {
            newLeaf.keys.add(leaf.keys.get(i));
            newLeaf.values.add(leaf.values.get(i));
            newLeaf.keyCount++;
        }
        
        for (int i = midpoint; i < leaf.keyCount; i++) {
            leaf.keys.remove(midpoint);
            leaf.values.remove(midpoint);
        }
        leaf.keyCount = midpoint;
        
        newLeaf.next = leaf.next;
        leaf.next = newLeaf;
        
        if (leaf == root) {
            InternalNode newRoot = new InternalNode();
            newRoot.keys.add(newLeaf.keys.get(0));
            newRoot.children.add(leaf);
            newRoot.children.add(newLeaf);
            newRoot.keyCount = 1;
            root = newRoot;
            leaf.parent = newRoot;
            newLeaf.parent = newRoot;
        } else {
            insertIntoParent(leaf, newLeaf.keys.get(0), newLeaf);
        }
    }

    private void insertIntoParent(Node child, String key, Node newChild) {
        InternalNode parent = (InternalNode) child.parent;
        int insertionPoint = 0;
        
        while (insertionPoint < parent.keyCount && 
               parent.keys.get(insertionPoint).compareTo(key) < 0) {
            insertionPoint++;
        }
        
        parent.keys.add(insertionPoint, key);
        parent.children.add(insertionPoint + 1, newChild);
        parent.keyCount++;
        newChild.parent = parent;
    }

    public void clear() {
        root = new LeafNode();
        firstLeaf = (LeafNode) root;
    }
}
