package com.hemendra.mdmgenerator.generator;

import com.hemendra.mdmgenerator.model.FieldModel;
import com.hemendra.mdmgenerator.model.JsonPayloadModel;
import com.hemendra.mdmgenerator.util.DtoSourceFileGenerator;
import com.hemendra.mdmgenerator.util.FieldGenerator;
import com.hemendra.mdmgenerator.util.GetterAndSetterGenerator;
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
public class GenerateDto {

    @Autowired
    private GetterAndSetterGenerator getterAndSetterGenerator;

    @Autowired
    private FieldGenerator fieldGenerator;

    @Autowired
    private DtoSourceFileGenerator dtoSourceFileGenerator;

    private List<FieldSpec> classFields;

    public JavaFile generate(JsonPayloadModel jsonPayloadModel) {
        if (jsonPayloadModel == null) {
            return null;
        }

        //Retrieve class name and fields from json data
        String className = jsonPayloadModel.getClassName();
        String dtoClassPackage = jsonPayloadModel.getDtoClassPackage();
        List<FieldModel> fields = jsonPayloadModel.getFields();

        //convert to java member variable and methods from json data
        List<FieldSpec> fieldSpecs = fieldGenerator.generateFields(fields);
        List<MethodSpec> methodSpecs = getterAndSetterGenerator.generateGetterAndSettersForFields(fields);

        dtoSourceFileGenerator.setClassName(className);
        dtoSourceFileGenerator.setFieldSpecs(fieldSpecs);
        dtoSourceFileGenerator.setMethodSpecs(methodSpecs);
        JavaFile build = dtoSourceFileGenerator.invoke(dtoClassPackage);

        return build;
    }

}
