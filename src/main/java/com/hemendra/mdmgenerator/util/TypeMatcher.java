package com.hemendra.mdmgenerator.util;

import java.util.Date;

/**
 * @author Hemendra
 */
public class TypeMatcher {

    public static Class getDataTypeMatcher (String fieldType) {
        Class typeName = null;
        String fieldInLowerCase = fieldType.toLowerCase();
        switch (fieldInLowerCase) {
            case "char" :
            case "character" :
                typeName = Character.class;
                break;
            case "int" :
            case "integer" :
                typeName = Integer.class;
                break;
            case "float" :
                typeName = Float.class;
                break;
            case "long" :
                typeName = Long.class;
                break;
            case "double" :
                typeName = Double.class;
                break;
            case "date" :
                typeName = Date.class;
                break;
            case "string" :
                typeName = String.class;
                break;
            case "byte" :
                typeName = Byte.class;
                break;

        }

        return typeName;
    }
}
