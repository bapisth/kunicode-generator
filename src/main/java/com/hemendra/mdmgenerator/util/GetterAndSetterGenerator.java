package com.hemendra.mdmgenerator.util;

import com.hemendra.mdmgenerator.model.FieldModel;
import com.squareup.javapoet.MethodSpec;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Hemendra
 */

@Component
public class GetterAndSetterGenerator {
    private List<MethodSpec> methodSpecs;

    public List<MethodSpec> generateGetterAndSettersForFields(List<FieldModel> fields) {
        TreeSet<FieldModel> fieldModels = RemoveDuplicateField.getFieldModels(fields);
        return methodSpecs = fieldModels.stream().map(field -> {
            Class fieldType = TypeMatcher.getDataTypeMatcher(field.getFieldType());
            Map<String, List<MethodSpec>> fieldMethodMap = new HashMap<>();
            List<MethodSpec> methodSpecs = new ArrayList<>();
            //Getter generator
            MethodSpec getterMethod = getGetterMethodSpec(field, fieldType);
            //Setter generator
            MethodSpec setterMethod = getSetterMethodSpec(field, fieldType);
            methodSpecs = Arrays.asList(setterMethod, getterMethod);
            return methodSpecs;
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Generate setter methods for list of the fields
     * @param field
     * @param fieldType
     * @return
     */
    private MethodSpec getSetterMethodSpec(FieldModel field, Class fieldType) {
        return MethodSpec
                .methodBuilder("set" + getCapitalizeFieldName(field))
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(fieldType, field.getFieldName())
                .addStatement("this.$N = " + field.getFieldName(), field.getFieldName())
                .build();
    }

    /**
     * Generates the getter method-specs for the list of the fields
     * @param field
     * @param fieldType
     * @return
     */
    private MethodSpec getGetterMethodSpec(FieldModel field, Class fieldType) {
        return MethodSpec.methodBuilder("get" + getCapitalizeFieldName(field))
                .addModifiers(Modifier.PUBLIC)
                .returns(fieldType)
                .addStatement("return $N", field.getFieldName())
                .build();
    }

    /**
     * Return capitalize Strings ex: "fOO BAr" to become "Foo Bar"
     * @param field
     * @return
     */
    private String getCapitalizeFieldName(FieldModel field) {
        return WordUtils.capitalize(field.getFieldName());
    }
}
