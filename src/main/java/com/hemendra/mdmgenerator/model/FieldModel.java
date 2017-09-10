package com.hemendra.mdmgenerator.model;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Comparator;

/**
 * @author Hemendra
 */
public class FieldModel implements Comparable {
    private String fieldName;
    private String fieldType;
    private String javaDoc;
    private ColumnIdentity columnIdentity;

    public FieldModel() {
    }

    public FieldModel(String fieldName, String fieldType, String javaDoc, ColumnIdentity columnIdentity) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.javaDoc = javaDoc;
        this.columnIdentity = columnIdentity;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getJavaDoc() {
        return javaDoc;
    }

    public void setJavaDoc(String javaDoc) {
        this.javaDoc = javaDoc;
    }

    public ColumnIdentity getColumnIdentity() {
        return columnIdentity;
    }

    public void setColumnIdentity(ColumnIdentity columnIdentity) {
        this.columnIdentity = columnIdentity;
    }

    @Override
    public int compareTo(Object obj) {
        FieldModel fieldModel = (FieldModel) obj;
        return this.getFieldName().compareTo(fieldModel.getFieldName());
    }
}


