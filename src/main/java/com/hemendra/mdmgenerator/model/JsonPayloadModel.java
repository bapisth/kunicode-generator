package com.hemendra.mdmgenerator.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Hemendra
 */
public class JsonPayloadModel implements Serializable {
    private String className;
    private String tableName;
    private List<FieldModel> fields;
    private Boolean generateAllArgConstructor = false;
    private Boolean generateGetterAndSetter;
    private Boolean generateToStringMethod;
    private Boolean isEntityClass = true;

    public JsonPayloadModel() {
    }

    public JsonPayloadModel(String className, String tableName, List<FieldModel> fields, Boolean generateAllArgConstructor, Boolean generateGetterAndSetter, Boolean generateToStringMethod, Boolean isEntityClass) {
        this.className = className;
        this.tableName = tableName;
        this.fields = fields;
        this.generateAllArgConstructor = generateAllArgConstructor;
        this.generateGetterAndSetter = generateGetterAndSetter;
        this.generateToStringMethod = generateToStringMethod;
        this.isEntityClass = isEntityClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FieldModel> getFields() {
        return fields;
    }

    public void setFields(List<FieldModel> fields) {
        this.fields = fields;
    }

    public Boolean getGenerateAllArgConstructor() {
        return generateAllArgConstructor;
    }

    public void setGenerateAllArgConstructor(Boolean generateAllArgConstructor) {
        this.generateAllArgConstructor = generateAllArgConstructor;
    }

    public Boolean getGenerateGetterAndSetter() {
        return generateGetterAndSetter;
    }

    public void setGenerateGetterAndSetter(Boolean generateGetterAndSetter) {
        this.generateGetterAndSetter = generateGetterAndSetter;
    }

    public Boolean getGenerateToStringMethod() {
        return generateToStringMethod;
    }

    public void setGenerateToStringMethod(Boolean generateToStringMethod) {
        this.generateToStringMethod = generateToStringMethod;
    }

    public Boolean getEntityClass() {
        return isEntityClass;
    }

    public void setEntityClass(Boolean entityClass) {
        isEntityClass = entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
