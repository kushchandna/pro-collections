package com.kush.procol.collections;

import java.util.LinkedList;

public class IndexableLinkedList<T> extends AbstractIndexableCollection<T, LinkedList<T>> {

    public IndexableLinkedList() {
        super(new LinkedList<>());
    }
}
