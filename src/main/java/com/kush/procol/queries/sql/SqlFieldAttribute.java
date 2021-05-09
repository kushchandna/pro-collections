package com.kush.procol.queries.sql;

import com.kush.procol.Attribute;

public class SqlFieldAttribute implements Attribute {

    private final String name;

    public SqlFieldAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
