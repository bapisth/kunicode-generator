package com.hemendra.mdmgenerator.util;

import com.hemendra.mdmgenerator.model.FieldModel;
import com.squareup.javapoet.FieldSpec;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Hemendra
 */

@Component
public class FieldGenerator {
    private List<FieldSpec> classFields;

    /**
     * Generates Field declarations for a pojo
     * @param fields
     * @return
     */
    public List<FieldSpec> generateFields(List<FieldModel> fields) {
        //remove duplicate fields names
        TreeSet<FieldModel> fieldModels = RemoveDuplicateField.getFieldModels(fields);
        classFields = fieldModels.stream().map(field -> {
            Class fieldType = TypeMatcher.getDataTypeMatcher(field.getFieldType());
            return FieldSpec.builder(fieldType, field.getFieldName(), Modifier.PRIVATE).build();
        }).collect(Collectors.toList());

        return classFields;
    }
}
