package com.hemendra.mdmgenerator.util;

import com.hemendra.mdmgenerator.model.FieldModel;
import com.squareup.javapoet.*;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author Hemendra
 */

@Component
public class EntitySourceFileGenerator {
    private String className;
    private String tableName;
    private List<FieldSpec> fieldSpecs;
    private List<MethodSpec> methodSpecs;

    public EntitySourceFileGenerator() {
    }

    public EntitySourceFileGenerator(String className, String tableName, List<FieldSpec> fieldSpecs, List<MethodSpec> methodSpecs) {
        this.className = className;
        this.tableName = tableName;
        this.fieldSpecs = fieldSpecs;
        this.methodSpecs = methodSpecs;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FieldSpec> getFieldSpecs() {
        return fieldSpecs;
    }

    public void setFieldSpecs(List<FieldSpec> fieldSpecs) {
        this.fieldSpecs = fieldSpecs;
    }

    public List<MethodSpec> getMethodSpecs() {
        return methodSpecs;
    }

    public void setMethodSpecs(List<MethodSpec> methodSpecs) {
        this.methodSpecs = methodSpecs;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public JavaFile invoke(String packageName) {
        //Create class
        TypeSpec entity = TypeSpec.classBuilder(className)
                .addSuperinterface(Serializable.class)
                .addAnnotation(getEntityAnnotation())
                .addAnnotation(getTableAnnotation())
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        //create file
        JavaFile build = JavaFile.builder(packageName, entity).build();
        try {
            //Write to file
            build.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return build;
    }

    private AnnotationSpec getEntityAnnotation() {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Entity.class);
        return builder.build();
    }

    private AnnotationSpec getTableAnnotation() {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Table.class)
                .addMember("name", "$S", tableName);
        return builder.build();
    }

}
