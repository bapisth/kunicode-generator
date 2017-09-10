package com.hemendra.mdmgenerator.util;

import com.hemendra.mdmgenerator.model.FieldModel;

import java.util.List;
import java.util.TreeSet;

/**
 * @author Hemendra
 */
public class RemoveDuplicateField {
    /**
     * This method returns the distinct fieldName objects
     * @param fields
     * @return
     */
    public static TreeSet<FieldModel> getFieldModels(List<FieldModel> fields) {
        return new TreeSet<>(fields);
    }
}
