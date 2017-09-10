package com.hemendra.mdmgenerator.util;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author Hemendra
 */

@Component
public class DtoSourceFileGenerator {
    private String className;
    private List<FieldSpec> fieldSpecs;
    private List<MethodSpec> methodSpecs;

    public DtoSourceFileGenerator() {
    }

    public DtoSourceFileGenerator(String className, List<FieldSpec> fieldSpecs, List<MethodSpec> methodSpecs) {
        this.className = className;
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

    public JavaFile invoke(String packageName) {
        //Create class
        TypeSpec dtoClass = TypeSpec.classBuilder(className)
                .addSuperinterface(Serializable.class)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        //create file
        JavaFile build = JavaFile.builder(packageName, dtoClass).build();
        try {
            //Write to file
            build.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return build;
    }

}
