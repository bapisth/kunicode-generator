package com.hemendra.mdmgenerator.generator;

import com.hemendra.mdmgenerator.model.FieldModel;
import com.hemendra.mdmgenerator.model.JsonPayloadModel;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.text.WordUtils;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hemendra
 */
@Component
public class GenerateWebServiceRest {
    public JavaFile generate(JsonPayloadModel jsonPayloadModel) {
        String className = jsonPayloadModel.getClassName();
        String tableName = jsonPayloadModel.getTableName();
        String dtoClassPackage = jsonPayloadModel.getDtoClassPackage();
        String entityClassPackage = jsonPayloadModel.getEntityClassPackage();
        List<FieldModel> fields = jsonPayloadModel.getFields();
        String restInterfacePackage = jsonPayloadModel.getRestInterfacePackage();
        String restImplPackage = jsonPayloadModel.getRestImplPackage();

        createRestServiceFor(className,restInterfacePackage,  dtoClassPackage, entityClassPackage);
        return null;
    }

    private JavaFile createRestServiceFor(String className, String restInterfacePackage, String dtoClassPackage, String entityClassPackage) {
        ClassName dtoClazz = ClassName.get(dtoClassPackage, className+"Dto");


        Iterable<MethodSpec> methodSpecs = getMethodSpecs(className, dtoClazz);
        Iterable<FieldSpec> fieldSpecs = getFieldSpecs(className, dtoClazz);

        TypeSpec restInterfaceBuilder = TypeSpec.interfaceBuilder(className + "RestService")
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodSpecs)
                .addFields(fieldSpecs)
                .build();
        //create file
        JavaFile build = JavaFile.builder(restInterfacePackage, restInterfaceBuilder).build();
        try {
            //Write to file
            build.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return build;
    }

    private Iterable<FieldSpec> getFieldSpecs(String className, ClassName dtoClazz) {
        FieldSpec fieldSpec = FieldSpec.builder(String.class, "PATH")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", "/"+className.toLowerCase())
                .build();
        List<FieldSpec> fieldSpecs = Arrays.asList(fieldSpec);
        return fieldSpecs;
    }

    private Iterable<MethodSpec> getMethodSpecs(String className, ClassName dtoClazz) {

        ClassName list = ClassName.get("java.util", "List");
        TypeName listOfClazz = ParameterizedTypeName.get(list, dtoClazz);

        MethodSpec getAllMethod = MethodSpec.methodBuilder("getAll")
                .returns(listOfClazz)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(GET.class)
                .addAnnotation(getProducesAnnotation(MediaType.APPLICATION_JSON))
                .build();

        String lazyLoadDtoParameter1 = "itemsPerPage";
        String lazyLoadDtoParameter2 = "pageNo";
        MethodSpec lazyLaodMethod = MethodSpec.methodBuilder("lazyLoad" + className)
                .returns(listOfClazz)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(addParameterSpecs(lazyLoadDtoParameter1, int.class))
                .addParameter(addParameterSpecs(lazyLoadDtoParameter2, int.class))
                .addAnnotation(getProducesAnnotation(MediaType.APPLICATION_JSON))
                .addAnnotation(getPathAnnotation("/perSelection/{itemsPerPage}/{pageNo}"))
                .addAnnotation(GET.class)
                .build();

        MethodSpec uploadMethod = MethodSpec.methodBuilder("setBinaryVariable")
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(POST.class)
                .addAnnotation(getPathAnnotation("/upload"))
                .addAnnotation(getCreateConsumeAnnotation(MediaType.MULTIPART_FORM_DATA))
                .addParameter(MultipartFormData.class, "multipartFormData")
                .build();

        MethodSpec createMethod = MethodSpec.methodBuilder("create" + WordUtils.capitalizeFully(className))
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(POST.class)
                .addAnnotation(getPathAnnotation("/create"))
                .addAnnotation(getCreateConsumeAnnotation(MediaType.APPLICATION_JSON))
                .addParameter(dtoClazz, WordUtils.capitalizeFully(dtoClazz.simpleName()))
                .addJavadoc("Create " + className + " data in the database.")
                .build();

        MethodSpec updateMethod = MethodSpec.methodBuilder("update" + WordUtils.capitalizeFully(className))
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(PUT.class)
                .addAnnotation(getPathAnnotation("/update"))
                .addAnnotation(getProducesAnnotation(MediaType.APPLICATION_JSON))
                .addParameter(dtoClazz, WordUtils.capitalize(dtoClazz.simpleName()))
                .addJavadoc("Update " + className + " data in the database.")
                .build();

        MethodSpec deleteMethod = MethodSpec.methodBuilder("delete" + className)
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(DELETE.class)
                .addAnnotation(getPathAnnotation("/delete/{id}"))
                .addParameter(addParameterSpecs("id", String.class))
                .build();

        List<MethodSpec> methodSpecs = Arrays.asList(deleteMethod, updateMethod, createMethod, uploadMethod, lazyLaodMethod, getAllMethod);

        return methodSpecs;
    }

    private ParameterSpec addParameterSpecs(String parameterName, Class clazzType) {

        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(PathParam.class)
                .addMember("value","$S", parameterName);

        ParameterSpec.Builder parameterSpecBuilder
                = ParameterSpec.builder(clazzType, parameterName)
                .addAnnotation(annotationBuilder.build());
        return parameterSpecBuilder.build();
    }

    private AnnotationSpec getProducesAnnotation(String mediaType) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Produces.class)
                .addMember("value", "$S", mediaType);

        return builder.build();
    }

    private AnnotationSpec getPathAnnotation(String pathName) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Path.class)
                .addMember("value","$S", pathName);
        return builder.build();
    }

    private AnnotationSpec getCreateConsumeAnnotation(String mediaType) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Consumes.class)
                .addMember("value","$S", mediaType);
        return builder.build();
    }
}
