package com.yandex.app.service;

import java.util.ArrayList;

public class LinkedHashMapContainer<K, V> {
    Node<K, V>[] nodeTable;
    private int size;
    private int initialCapacity;
    private float loadFactor;
    private int threshold;
    private Node<K, V> startNode;
    private Node<K, V> endNode;

    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> beforeNode;
        Node<K, V> afterNode;
        Node<K, V> nextNode;

        Node(int hash, K key, V value, Node<K, V> nextNode) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.nextNode = nextNode;
        }
    }

    public LinkedHashMapContainer(int initialCapacity, float loadFactor) {
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.threshold = (int) (initialCapacity * loadFactor);
        this.nodeTable = (Node<K, V>[]) new Node[initialCapacity];
    }

    public LinkedHashMapContainer() {
        this(16, 0.75f);
    }

    private void resize() {
        int newInitialCapacity = initialCapacity * 2;
        Node<K, V>[] newNodeTable = (Node<K, V>[]) new Node[newInitialCapacity];
        for (int i = 0; i < initialCapacity; i++) {
            Node<K, V> oldNode = nodeTable[i];
            while (oldNode != null) {
                Node<K, V> oldNextNode = oldNode.nextNode;
                int newIndex = (newInitialCapacity - 1) & oldNode.hash;
                oldNode.nextNode = newNodeTable[newIndex];
                newNodeTable[newIndex] = oldNode;
                oldNode = oldNextNode;
            }
        }
        initialCapacity = newInitialCapacity;
        threshold = (int) (newInitialCapacity * loadFactor);
        nodeTable = newNodeTable;
    }

    private void linkLast(Node<K, V> newNode) {
        if (endNode == null) {
            startNode = endNode = newNode;
        } else {
            endNode.afterNode = newNode;
            newNode.beforeNode = endNode;
            endNode = newNode;
        }
    }

    private void unlink(Node<K, V> unlinkNode) {
        Node<K, V> beforeNode = unlinkNode.beforeNode;
        Node<K, V> afterNode = unlinkNode.afterNode;
        if (beforeNode == null) {
            startNode = afterNode;
        } else {
            beforeNode.afterNode = afterNode;
        }
        if (afterNode == null) {
            endNode = beforeNode;
        } else {
            afterNode.beforeNode = beforeNode;
        }
    }

    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        int hash = key.hashCode();
        int index = (initialCapacity - 1) & hash;
        for (Node<K, V> node = nodeTable[index]; node != null; node = node.nextNode) {
            if (node.hash == hash && node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        Node<K, V> newNode = new Node<>(hash, key, value, nodeTable[index]);
        nodeTable[index] = newNode;
        linkLast(newNode);
        size++;
        if (size > threshold) {
            resize();
        }
    }

    public V remove(K key) {
        if (key == null) {
            return null;
        }
        int hash = key.hashCode();
        int index = (initialCapacity - 1) & hash;
        Node<K, V> prevNode = null;
        Node<K, V> removeNode = nodeTable[index];
        while (removeNode != null) {
            if (removeNode.hash == hash && removeNode.key.equals(key)) {
                if (prevNode == null) {
                    nodeTable[index] = removeNode.nextNode;
                } else {
                    prevNode.nextNode = removeNode.nextNode;
                }
                unlink(removeNode);
                size--;
                return removeNode.value;
            }
            prevNode = removeNode;
            removeNode = prevNode.nextNode;
        }
        return null;
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        int hash = key.hashCode();
        int index = (initialCapacity - 1) & hash;
        for (Node<K, V> node = nodeTable[index]; node != null; node = node.nextNode) {
            if (node.hash == hash && node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    public ArrayList<V> values() {
        ArrayList<V> valueList = new ArrayList<>(size);
        Node<K, V> node = startNode;
        while (node != null) {
            valueList.add(node.value);
            node = node.afterNode;
        }
        return valueList;
    }

    public boolean containsKey(K key) {
        int hash = key.hashCode();
        int index = (initialCapacity - 1) & hash;
        Node<K, V> searchNode = nodeTable[index];
        while (searchNode != null) {
            if (searchNode.hash == hash && searchNode.key.equals(key)) {
                return true;
            }
            searchNode = searchNode.nextNode;
        }
        return false;
    }

    public V getFirst() {
        return startNode != null ? startNode.value : null;
    }

    public V getLast() {
        return endNode != null ? endNode.value : null;
    }

    public int getSize() {
        return size;
    }
}