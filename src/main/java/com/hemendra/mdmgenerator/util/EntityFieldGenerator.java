package com.hemendra.mdmgenerator.util;

import com.hemendra.mdmgenerator.model.FieldModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Hemendra
 */
@Component
public class EntityFieldGenerator {
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
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, field.getFieldName(), Modifier.PRIVATE);
            AnnotationSpec columnAnnotation = getColumnAnnotation(field);

            fieldSpecBuilder.addAnnotation(columnAnnotation);

            //Check if the field is primary key
            boolean columnIdentity = Objects.isNull(field.getColumnIdentity());
            if (!columnIdentity && field.getColumnIdentity().isPrimaryKey()) {
                fieldSpecBuilder.addAnnotation(Id.class);
                fieldSpecBuilder.addAnnotation(Basic.class);
            }
            // Check if the
            if (fieldType == Date.class) {
                fieldSpecBuilder.addAnnotation(getTemporalAnnotation(field));
            }

            FieldSpec fieldSpec = fieldSpecBuilder.build();

            return fieldSpec;
        }).collect(Collectors.toList());

        return classFields;
    }

    private AnnotationSpec getColumnAnnotation(FieldModel field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Column.class)
                .addMember("name", "$S",field.getFieldName());
        return builder.build();
    }

    private AnnotationSpec getTemporalAnnotation(FieldModel field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Temporal.class)
                .addMember("value", "$T.TIME", TemporalType.class);
        return builder.build();
    }
}
