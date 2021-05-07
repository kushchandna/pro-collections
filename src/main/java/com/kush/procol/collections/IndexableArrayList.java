package com.kush.procol.collections;

import java.util.ArrayList;

public class IndexableArrayList<T> extends AbstractIndexableCollection<T, ArrayList<T>> {

    public IndexableArrayList() {
        super(new ArrayList<>());
    }
}
