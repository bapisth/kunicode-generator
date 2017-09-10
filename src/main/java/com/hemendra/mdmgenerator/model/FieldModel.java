package com.hemendra.mdmgenerator.model;

/**
 * @author Hemendra
 */
public class FieldModel implements Comparable {
    private String fieldName;
    private String fieldType;
    private String javaDoc;
    private String entityClassPackage;
    private String dtoClassPackage;
    private String restInterfacePackage;
    private String restImplPackage;
    private ColumnIdentity columnIdentity;

    public FieldModel() {
    }

    public FieldModel(String fieldName, String fieldType,
                      String javaDoc, ColumnIdentity columnIdentity,
                      String entityClassPackage, String dtoClassPackage,
                      String restInterfacePackage, String restImplPackage) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.javaDoc = javaDoc;
        this.columnIdentity = columnIdentity;
        this.entityClassPackage = entityClassPackage;
        this.dtoClassPackage = dtoClassPackage;
        this.restImplPackage = restImplPackage;
        this.restInterfacePackage = restInterfacePackage;
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

    public String getEntityClassPackage() {
        return entityClassPackage;
    }

    public void setEntityClassPackage(String entityClassPackage) {
        this.entityClassPackage = entityClassPackage;
    }

    public String getDtoClassPackage() {
        return dtoClassPackage;
    }

    public void setDtoClassPackage(String dtoClassPackage) {
        this.dtoClassPackage = dtoClassPackage;
    }

    public String getRestInterfacePackage() {
        return restInterfacePackage;
    }

    public void setRestInterfacePackage(String restInterfacePackage) {
        this.restInterfacePackage = restInterfacePackage;
    }

    public String getRestImplPackage() {
        return restImplPackage;
    }

    public void setRestImplPackage(String restImplPackage) {
        this.restImplPackage = restImplPackage;
    }

    @Override
    public int compareTo(Object obj) {
        FieldModel fieldModel = (FieldModel) obj;
        return this.getFieldName().compareTo(fieldModel.getFieldName());
    }
}


