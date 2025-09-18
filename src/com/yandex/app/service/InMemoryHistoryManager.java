package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node startNode;
    private Node endNode;
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    private static class Node {
        Task value;
        Node prevNode;
        Node nextNode;

        Node(Task value, Node prevNode, Node nextNode) {
            this.value = value;
            this.prevNode = prevNode;
            this.nextNode = nextNode;
        }
    }

    @Override
    public void add(int id, Task task) {
        remove(id);
        linkLast(task);
        nodeMap.put(id, endNode);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            unlink(node);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        ArrayList<Task> historyList = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            historyList.add(current.value);
            current = current.prevNode;
        }
        return historyList;
    }

    private void linkLast(Task value) {
        Node newNode = new Node(value, endNode, null);
        if (endNode == null) {
            startNode = newNode;
        } else {
            endNode.nextNode = newNode;
        }
        endNode = newNode;
    }

    private void unlink(Node node) {
        Node prevNode = node.prevNode;
        Node nextNode = node.nextNode;

        if (prevNode == null) {
            startNode = nextNode;
        } else {
            prevNode.nextNode = nextNode;
        }

        if (nextNode == null) {
            endNode = prevNode;
        } else {
            nextNode.prevNode = prevNode;
        }
    }
}
