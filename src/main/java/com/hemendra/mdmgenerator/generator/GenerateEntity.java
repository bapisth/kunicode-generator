package com.hemendra.mdmgenerator.generator;

import com.hemendra.mdmgenerator.model.FieldModel;
import com.hemendra.mdmgenerator.model.JsonPayloadModel;
import com.hemendra.mdmgenerator.util.EntityFieldGenerator;
import com.hemendra.mdmgenerator.util.EntitySourceFileGenerator;
import com.hemendra.mdmgenerator.util.GetterAndSetterGenerator;
import com.hemendra.mdmgenerator.util.DtoSourceFileGenerator;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hemendra
 */

@Component
public class GenerateEntity {

    @Autowired
    private GetterAndSetterGenerator getterAndSetterGenerator;

    @Autowired
    private EntityFieldGenerator entityFieldGenerator;

    @Autowired
    private EntitySourceFileGenerator entitySourceFileGenerator;

    public JavaFile generate(JsonPayloadModel jsonPayloadModel) {
        if (jsonPayloadModel == null) {
            return null;
        }

        String className = jsonPayloadModel.getClassName();
        String tableName = jsonPayloadModel.getTableName();
        List<FieldModel> fields = jsonPayloadModel.getFields();


        List<FieldSpec> fieldSpecs = entityFieldGenerator.generateFields(fields);
        List<MethodSpec> methodSpecs = getterAndSetterGenerator.generateGetterAndSettersForFields(fields);

        entitySourceFileGenerator.setClassName(className);
        entitySourceFileGenerator.setTableName(tableName);
        entitySourceFileGenerator.setFieldSpecs(fieldSpecs);
        entitySourceFileGenerator.setMethodSpecs(methodSpecs);

        JavaFile javaFile = entitySourceFileGenerator.invoke();


        return javaFile;
    }
}
