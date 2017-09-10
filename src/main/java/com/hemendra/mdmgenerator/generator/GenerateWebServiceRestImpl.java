package com.hemendra.mdmgenerator.generator;

import au.com.bytecode.opencsv.CSVReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemendra.mdmgenerator.model.FieldModel;
import com.hemendra.mdmgenerator.model.JsonPayloadModel;
import com.squareup.javapoet.*;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Hemendra
 */
@Component
public class GenerateWebServiceRestImpl {
    public JavaFile generate(JsonPayloadModel jsonPayloadModel) {
        String className = jsonPayloadModel.getClassName();
        String tableName = jsonPayloadModel.getTableName();
        String dtoClassPackage = jsonPayloadModel.getDtoClassPackage();
        String entityClassPackage = jsonPayloadModel.getEntityClassPackage();
        List<FieldModel> fields = jsonPayloadModel.getFields();
        String restInterfacePackage = jsonPayloadModel.getRestInterfacePackage();
        String restImplPackage = jsonPayloadModel.getRestImplPackage();

        createRestImplFileFor(className,restInterfacePackage, restImplPackage,  dtoClassPackage, entityClassPackage);

        return null;
    }

    private JavaFile createRestImplFileFor(String className, String restInterfacePackage, String restImplPackage, String dtoClassPackage, String entityClassPackage) {

        ClassName dtoClazz = ClassName.get(dtoClassPackage, className+"Dto");
        ClassName entityClazz = ClassName.get(entityClassPackage, className+"Entity");
        ClassName restServiceInterfaceClazz = ClassName.get(entityClassPackage, className+"RestService");

        Iterable<FieldSpec> fieldSpecs = getFieldSpecs(className, dtoClazz, entityClazz, restServiceInterfaceClazz);
        Iterable<MethodSpec> methodSpecs = getMethodSpecs(className, dtoClazz, entityClazz, restServiceInterfaceClazz);
        TypeSpec serviceImplBuilder = TypeSpec.classBuilder(className + "RestServiceImpl")
                .superclass(AbstractRestProcessEngineAware.class)
                .addSuperinterface(restServiceInterfaceClazz)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        //create file
        JavaFile build = JavaFile.builder(restImplPackage, serviceImplBuilder).build();
        try {
            //Write to file
            build.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return build;
    }

    private Iterable<MethodSpec> getMethodSpecs(String className, ClassName dtoClazz, ClassName entityClazz,
                                                ClassName restServiceInterfaceClazz) {
        ClassName list = ClassName.get("java.util", "List");
        ClassName customSessionFactoryClazz = ClassName.get("com.bipros.ims.service", "CustomSessionFactory");
        TypeName listOfDtoClazz = ParameterizedTypeName.get(list, dtoClazz);
        TypeName listOfEntityClazz = ParameterizedTypeName.get(list, entityClazz);


        //constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "engineName")
                .addParameter(ObjectMapper.class, "objectMapper")
                .addStatement("super($N, $N)", "engineName", "objectMapper")
                .addStatement("this.$N = $N", "engineName", "engineName")
                .addStatement("this.$N = $N", "objectMapper", "objectMapper")
                .addStatement("this.$N = new $T()", "modelMapper", ModelMapper.class)
                .build();

        //getALl()
        MethodSpec getAllImpl = getAllImplMethodBuild(dtoClazz, entityClazz, customSessionFactoryClazz, listOfDtoClazz, listOfEntityClazz);

        //lazy-load
        MethodSpec lazyLoadMethodImpl = getLazyLoadMethodBuild(className, dtoClazz, entityClazz, customSessionFactoryClazz, listOfDtoClazz, listOfEntityClazz);

        //upload
        MethodSpec setBinaryVariableMethodBuild = getSetBinaryVariableMethodBuild(className, dtoClazz, entityClazz,
                customSessionFactoryClazz, listOfDtoClazz, listOfEntityClazz);


        return Arrays.asList(constructor, getAllImpl, lazyLoadMethodImpl, setBinaryVariableMethodBuild);
    }

    private MethodSpec getSetBinaryVariableMethodBuild(String className, ClassName dtoClazz, ClassName entityClazz,
                                                               ClassName customSessionFactoryClazz, TypeName listOfDtoClazz, TypeName listOfEntityClazz) {
        return MethodSpec.methodBuilder("setBinaryVariable")
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(MultipartFormData.class, "payload")
                .addStatement("$T transaction = null", Transaction.class)
                .addStatement("$T br = null", BufferedReader.class)
                .addStatement("$T result = new $T()", JSONObject.class, JSONObject.class)
                .addStatement("$T status = \"success\"", String.class)
                .addStatement("$T dataPart = $N.getNamedPart(\"data\")", MultipartFormData.FormPart.class, "payload")
                .addStatement("$T objectTypePart = $N.getNamedPart(\"type\")", MultipartFormData.FormPart.class, "payload")
                .addStatement("$T valueTypePart = $N.getNamedPart(\"valueType\")", MultipartFormData.FormPart.class, "payload")

                .addStatement("if ($N != null) {\n" + //objectTypePart
                        "\t\t\t$T object = null;\n" + //Object.class
                        "\t\t\tif ($N.getContentType() != null\n" + //dataPart
                        "\t\t\t\t\t&& $N.getContentType().toLowerCase().contains($T.APPLICATION_JSON)) {\n" + //dataPart, MediaType.class
                        "\n" +
                        "\t\t\t\tobject = deserializeJsonObject($N.getTextContent(), $N.getBinaryContent());\n" +  //objectTypePart, dataPart
                        "\t\t\t} else {\n" +
                        "\t\t\t\tthrow new $T($T.BAD_REQUEST,\n" + //InvalidRequestException.class, Response.Status.class
                        "\t\t\t\t\t\t\"Unrecognized content type for serialized java type: \" + $N.getContentType());\n" + //dataPart
                        "\t\t\t}\n" +
                        "\t\t\tif (object != null) {\n" +
                        "\t\t\t\t// Do Nothing\n" +
                        "\t\t\t}\n" +
                        "\t\t} else {\n" +
                        "\t\t\ttry {\n" +
                        "\t\t\t\t$N = $T.getSessionFactory();\n" + //sessionFactory, customSessionFactoryClazz
                        "\t\t\t\t$N = $T.getSession($N);\n" + //session, customSessionFactoryClazz, sessionFactory
                        "\t\t\t\t$N = $N.beginTransaction();\n" + //transaction, session
                        "\n" +
                        "\t\t\t\t$T valueTypeName = DEFAULT_BINARY_VALUE_TYPE;\n" +//String.class
                        "\t\t\t\tif ($N != null) {\n" + //valueTypePart
                        "\t\t\t\t\tif ($N.getTextContent() == null) {\n" + //valueTypePart
                        "\t\t\t\t\t\tthrow new $T($T.BAD_REQUEST,\n" + //InvalidRequestException.class, Response.Status.class
                        "\t\t\t\t\t\t\t\t\"Form part with name 'valueType' must have a text/plain value\");\n" +
                        "\t\t\t\t\t}\n" +
                        "\t\t\t\t\t$N = $N.getTextContent();\n" + //valueTypeName, valueTypePart
                        "\t\t\t\t}\n" +
                        "\n" +
                        "\t\t\t\t$T[] bytesData = $N.getBinaryContent();\n" + //byte.class, dataPart
                        "\t\t\t\t$T file = new $T(\"TempFile.csv\");\n" + //File.class, File.class
                        "\t\t\t\t$T fos = new $T(file.getPath());\n" + //FileOutputStream.class, FileOutputStream.class
                        "\t\t\t\tfos.write(bytesData);\n" +
                        "\t\t\t\tfos.close();\n" +
                        "\n" +
                        "\t\t\t\t$T reader = new $T(new $T(file.getAbsolutePath()));\n" + //CSVReader.class, CSVReader.class, FileReader.class
                        "\t\t\t\treader.readNext();// Skip First Line\n" +
                        "\t\t\t\t$T mdmEmployee = null;\n" + //entityClazz
                        "\n" +
                        "\t\t\t\t$T<$T> records = reader.readAll();\n" + //List.class, String[].class
                        "\t\t\t\t$T<$T> iterator = records.iterator();\n" + //Iterator.class, String[].class
                        "\t\t\t\t$T<$T> fileIterator = records.iterator();\n" + //Iterator.class, String[].class
                        "\t\t\t\t\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\t$T createCriteria = $N.createCriteria($T.class);\n" + //Criteria.class, session, entityClazz
                        "\t\t\t\t$T mdmEmployeeList = ($T) createCriteria.list();\n" + //listOfEntityClazz, listOfEntityClazz
                        "\t\t\t\t$T mdmEmployeeDto = null;\n" + //listOfDtoClazz
                        "\t\t\t\t$T recordList = null;\n" + //listOfEntityClazz
                        "\t\t\t\t$T employeeRecord = null;\n" + //entityClazz
                        "\t\t\t\tif ($T.ofNullable(mdmEmployeeList).isPresent()) {\n" + //Optional.class
                        "\t\t\t\t\tmdmEmployeeDto = mdmEmployeeList.stream().map(new Function<MdmEmployee, MdmEmployeeDto>() {\n" + //Function.class, entityClazz, dtoClazz
                        "\t\t\t\t\t\t@Override\n" +
                        "\t\t\t\t\t\tpublic $T apply($T mdmEmployee) {\n" + //dtoClazz, entityClazz
                        "\t\t\t\t\t\t\treturn modelMapper.map(mdmEmployee, $T.class);\n" + //dtoClazz
                        "\t\t\t\t\t\t}\n" +
                        "\t\t\t\t\t}).collect($T.toList());\n" + //Collectors.class
                        "\t\t\t\t}\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\trecordList = new $T<$T>();\n" + //ArrayList.class, entityClazz
                        "\t\t\t\tint index=0;\n" +
                        "\t\t\t\twhile (iterator.hasNext()) {\n" +
                        "\t\t\t\t\tString[] record = iterator.next();\n" + // String[].class
                        "\n" +
                        "\t\t\t\t\t //replace this code as per your code..... \n" +
                        "\t\t\t\t\tString employeeId = record[0];\n" +
                        "\t\t\t\t\tString employeeName = record[1];\n" +
                        "\t\t\t\t\tString agency = record[2];\n" +
                        "\t\t\t\t\tString tenantId = record[3];\n" +

                        "\n" +
                        "\t\t\t\t\temployeeRecord = new MdmEmployee();\n" +
                        "\t\t\t\t\temployeeRecord.setEmployeeId(employeeId);\n" +
                        "\t\t\t\t\temployeeRecord.setEmployeeName(employeeName);\n" +
                        "\t\t\t\t\temployeeRecord.setAgency(agency);\n" +
                        "\t\t\t\t\temployeeRecord.setTenantId(tenantId);\n" +
                        "\t\t\t\t\temployeeRecord.setStatus(\"ACTIVE\");\n" +
                        "\t\t\t\t\trecordList.add(employeeRecord);\n" +
                        "\t\t\t\t\tindex++;\n" +
                        "\t\t\t\t\t // Replace this code end. " +
                        "\t\t\t\t}\n" +
                        "\n" +
                        "\t\t\t\tmdmEmployeeList.removeAll(recordList);\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\tList<MdmEmployee> entityList = mdmEmployeeList.stream().map(obj -> {\n" +
                        "\t\t\t\t\tobj.setStatus(\"INACTIVE\");\n" +
                        "\t\t\t\t\treturn obj;\n" +
                        "\t\t\t\t}).collect(Collectors.toList());\n" +
                        "\n" +
                        "\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\twhile (fileIterator.hasNext()) {\n" +
                        "\t\t\t\t\tString[] record = fileIterator.next();\n" +
                        "\t\t\t\t\tString employeeId = record[0];\n" +
                        "\t\t\t\t\tString employeeName = record[1];\n" +
                        "\t\t\t\t\tString agency = record[2];\n" +
                        "\t\t\t\t\tString tenantId = record[3];\n" +
                        "\t\t\t\t\t/**\n" +
                        "\t\t\t\t\t * Currently the session object are in persistent state\n" +
                        "\t\t\t\t\t * To avoid the below exception\n" +
                        "\t\t\t\t\t * \"Hibernate Error: org.hibernate.NonUniqueObjectException: a different object with the same identifier value was already associated with the session\"\n" +
                        "\t\t\t\t\t * \n" +
                        "\t\t\t\t\t * first check if the perticualr record is present in the database or not accordingly execute the if-else statement below.\n" +
                        "\t\t\t\t\t */\n" +
                        "\t\t\t\t\t\n" +
                        "\t\t\t\t\tmdmEmployee = (MdmEmployee) session.get(MdmEmployee.class , employeeId);\n" +
                        "\t\t\t\t\t\n" +
                        "\t\t\t\t\tif (mdmEmployee == null) {\n" +
                        "\t\t\t\t\t\tmdmEmployee = new MdmEmployee();\n" +
                        "\t\t\t\t\t\tmdmEmployee.setEmployeeName(employeeName);\n" +
                        "\t\t\t\t\t\tmdmEmployee.setAgency(agency);\n" +
                        "\t\t\t\t\t\tmdmEmployee.setEmployeeId(employeeId);\n" +
                        "\t\t\t\t\t\tmdmEmployee.setTenantId(tenantId);\n" +
                        "\t\t\t\t\t\tmdmEmployee.setStatus(\"ACTIVE\");\n" +
                        "\t\t\t\t\t\t\n" +
                        "\t\t\t\t\t\tsession.save(mdmEmployee);\n" +
                        "\t\t\t\t\t} else {\n" +
                        "\t\t\t\t\t\tmdmEmployee.setEmployeeName(employeeName);\n" +
                        "\t\t\t\t\t\tmdmEmployee.setAgency(agency);\n" +
                        "\t\t\t\t\t\tmdmEmployee.setTenantId(tenantId);\n" +
                        "\t\t\t\t\t\tmdmEmployee.setStatus(\"ACTIVE\");\n" +
                        "\t\t\t\t\t\t\n" +
                        "\t\t\t\t\t\tsession.update(mdmEmployee);\n" +
                        "\t\t\t\t\t}\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\t// Then update those records which are not present in the file\n" +
                        "\t\t\t\tentityList.stream().forEach(mdmEmpEntity -> {\n" +
                        "\t\t\t\t\tsession.update(mdmEmpEntity);\n" +
                        "\t\t\t\t});\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\ttransaction.commit();\n" +
                        "\n" +
                        "\t\t\t} catch (AuthorizationException e) {\n" +
                        "\t\t\t\tif (transaction != null && transaction.isActive()) {\n" +
                        "\t\t\t\t\ttransaction.rollback();\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\te.printStackTrace();\n" +
                        "\t\t\t\tstatus = \"fail\";\n" +
                        "\t\t\t\tmessage = e.getMessage();\n" +
                        "\t\t\t} catch (ProcessEngineException e) {\n" +
                        "\t\t\t\tif (transaction != null && transaction.isActive()) {\n" +
                        "\t\t\t\t\ttransaction.rollback();\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\tthrow new RestException(Response.Status.INTERNAL_SERVER_ERROR, e, \"\");\n" +
                        "\t\t\t} catch (FileNotFoundException e) {\n" +
                        "\t\t\t\tif (transaction != null && transaction.isActive()) {\n" +
                        "\t\t\t\t\ttransaction.rollback();\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\te.printStackTrace();\n" +
                        "\t\t\t\tstatus = \"fail\";\n" +
                        "\t\t\t\tmessage = e.getMessage();\n" +
                        "\t\t\t} catch (IOException e) {\n" +
                        "\t\t\t\tif (transaction != null && transaction.isActive()) {\n" +
                        "\t\t\t\t\ttransaction.rollback();\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\te.printStackTrace();\n" +
                        "\t\t\t\tstatus = \"fail\";\n" +
                        "\t\t\t\tmessage = e.getMessage();\n" +
                        "\t\t\t} catch (Exception e) {\n" +
                        "\t\t\t\tif (transaction != null && transaction.isActive()) {\n" +
                        "\t\t\t\t\ttransaction.rollback();\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\te.printStackTrace();\n" +
                        "\t\t\t\tstatus = \"fail\";\n" +
                        "\t\t\t\tmessage = e.getMessage();\n" +
                        "\t\t\t} finally {\n" +
                        "\t\t\t\tif (session != null && session.isOpen()) {\n" +
                        "\t\t\t\t\tsession.close();\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t}\n" +
                        "\t\t}",
                        "objectTypePart",
                        Object.class,
                        "dataPart",
                        "dataPart", MediaType.class,
                        "objectTypePart", "dataPart",
                        InvalidRequestException.class, Response.Status.class,
                        "dataPart",
                        "sessionFactory", customSessionFactoryClazz,
                        "session", customSessionFactoryClazz, "sessionFactory",
                        "transaction", "session",
                        String.class,
                        "valueTypePart",
                        "valueTypePart",
                        InvalidRequestException.class, Response.Status.class,
                        "valueTypeName", "valueTypePart",
                        byte.class, "dataPart",
                        File.class, File.class,
                        FileOutputStream.class, FileOutputStream.class,
                        CSVReader.class, CSVReader.class, FileReader.class,
                        entityClazz,
                        List.class, String[].class,
                        Iterator.class, String[].class,
                        Iterator.class, String[].class,
                        Criteria.class, "session", entityClazz,
                        listOfEntityClazz, listOfEntityClazz,
                        listOfDtoClazz,
                        listOfEntityClazz,
                        entityClazz,
                        Optional.class,
                        Function.class, entityClazz, dtoClazz,
                        dtoClazz, entityClazz,
                        dtoClazz
                        //,
                        /*Collectors.class,
                        ArrayList.class, entityClazz*/
                )

                .addStatement("$N.put(\"status\", $N)", "result", "status")
                .addStatement("$N.put(\"message\", $N)", "result", "message")
                .addStatement("return $N.toString()", "result")
                .build();
    }

    private MethodSpec getLazyLoadMethodBuild(String className, ClassName dtoClazz, ClassName entityClazz,
                                              ClassName customSessionFactoryClazz, TypeName listOfDtoClazz, TypeName listOfEntityClazz) {
        return MethodSpec.methodBuilder("lazyLoad" + className)
                .returns(listOfDtoClazz)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(int.class, "itemsPerPage")
                .addParameter(int.class, "pageNo")
                .addStatement("$T transaction = null", Transaction.class)
                .addStatement("$T collectEntityDtos = null", listOfDtoClazz) //listOfDtoClazz
                .addStatement("try {\n" +
                        "\t$N = $T.getSessionFactory();\n" + //sessionFactory, customSessionFactoryClazz
                        "\t$N = $T.getSession($N);\n" + //session, customSessionFactoryClazz, sessionFactory
                        "\t$N = $N.beginTransaction();\n" + //transaction, session
                        "\n" +
                        "\t$T criteria = $N.createCriteria($T.class);\n" + //Criteria.class, session, entityClazz
                        "\tcriteria.setFirstResult(($N - 1) * $N);\n" + //pageNo, itemsPerPage
                        "\tcriteria.setMaxResults($N);\n" + //itemsPerPage
                        "\n" +
                        "\t$T mdmEntityList = ($T) criteria.list();\n" + // listOfEntityClazz, listOfEntityClazz
                        "\tif ($T.ofNullable(mdmEntityList).isPresent()) {\n" + //Optional.class
                        "\tcollectEntityDtos = mdmEntityList.stream().map(data -> {\n" +
                        "\treturn modelMapper.map(data, $T.class);\n" + //dtoClazz
                        "}).collect($T.toList());\n" + // Collectors.class
                        "}\n" +
                        "\t$N.commit();\n" + //transaction
                        "\treturn collectEntityDtos;\n" +
                        "} catch ($T e) {\n" + //IOException
                        "\tif ($N != null && $N.isActive()) {\n" + //transaction, transaction
                        "\t$N.rollback();\n" + //transaction
                        "}\n" +
                        "\te.printStackTrace();\n" +
                        "} finally {\n" +
                        "\tif ($N != null && $N.isOpen()) {\n" + //session, session
                        "\t$N.close();\n" + //session
                        "}\n" +
                        "}",
                        "sessionFactory", customSessionFactoryClazz,
                        "session", customSessionFactoryClazz, "sessionFactory",
                        "transaction", "session",
                        Criteria.class, "session", entityClazz,
                        "pageNo", "itemsPerPage",
                        "itemsPerPage",
                        listOfEntityClazz, listOfEntityClazz,
                        Optional.class,
                        dtoClazz,
                        Collectors.class,
                        "transaction",
                        IOException.class,
                        "transaction", "transaction",
                        "transaction",
                        "session", "session",
                        "session")
                .addStatement("return null")
                .build();
    }

    private MethodSpec getAllImplMethodBuild(ClassName dtoClazz, ClassName entityClazz, ClassName customSessionFactoryClazz,
                                             TypeName listOfDtoClazz, TypeName listOfEntityClazz) {
        return MethodSpec.methodBuilder("getAll")
                .addAnnotation(Override.class)
                .returns(listOfDtoClazz)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T transaction = null", Transaction.class)
                .addStatement("try {\n" +
                        "$N = $T.getSessionFactory();\n" +//sessionFactory, customSessionFactoryClazz
                        "$N = $T.getSession($N);\n" + //session, customSessionFactoryClazz, sessionFactory
                        "$N = $N.beginTransaction();\n" + //transaction, session
                        "$T createCriteria = $N.createCriteria($T.class);\n" + //Criteria.class, session, entityClazz
                        "\n" +
                        //"\tcreateCriteria.addOrder(Order.asc(\"employeeName\"));\n" +
                        "$T mdmEntities = ($T) createCriteria.list();\n" + // listOfEntityClazz, listOfEntityClazz
                        "$T mdmEntityDtos = null;\n" + //listOfEntityClazz
                        "if ($T.ofNullable(mdmEntities).isPresent()) {\n" + // Optional.class
                        "mdmEntityDtos = mdmEntities.stream().map(data-> {\n" +
                        "return modelMapper.map(data, $T.class);\n" + //dtoClazz
                        "}).collect($T.toList());\n" + //Collectors.class
                         "}\n" +
                        "\n" +
                        "$N.commit();\n" + //transaction
                        "\n" +
                        "return mdmEntityDtos;\n" +
                        "\n" +
                        "} catch ($T e) {\n" + //IOException.class
                        "\tif ($N != null && $N.isActive()) {\n" + //transaction, transaction
                        "\t$N.rollback();\n" + // transaction
                        "}\n" +
                        "\te.printStackTrace();\n" +
                        "} finally {\n" +
                        "\tif ($N != null && $N.isOpen()) {\n" + // session, session
                        "\t$N.close();\n" + // session
                        "}\n" +
                        "}", "sessionFactory", customSessionFactoryClazz,
                        "session", customSessionFactoryClazz, "sessionFactory",
                        "transaction", "session",
                        Criteria.class, "session", entityClazz,
                        listOfEntityClazz, listOfEntityClazz,
                        listOfEntityClazz,
                        Optional.class,
                        dtoClazz,
                        Collectors.class,
                        "transaction",
                        IOException.class,
                        "transaction", "transaction",
                        "transaction",
                        "session", "session",
                        "session")
                .addStatement("return null")
                .build();
    }

    private Iterable<FieldSpec> getFieldSpecs(String className, ClassName dtoClazz, ClassName entityClazz,
                                              ClassName restServiceInterfaceClazz) {
        FieldSpec fieldSession = FieldSpec.builder(Session.class, "session").addModifiers(Modifier.PRIVATE).build();
        FieldSpec fieldSessionFactory = FieldSpec.builder(SessionFactory.class, "sessionFactory").addModifiers(Modifier.PRIVATE).build();
        FieldSpec fieldEngineName = FieldSpec.builder(String.class, "engineName").addModifiers(Modifier.PRIVATE).build();
        FieldSpec fieldObjectMapper = FieldSpec.builder(ObjectMapper.class, "objectMapper").addModifiers(Modifier.PRIVATE).build();
        FieldSpec fieldParentObjectMapper = FieldSpec.builder(ObjectMapper.class, "parentObjectMapper").addModifiers(Modifier.PRIVATE).build();
        FieldSpec fieldEngineNameParent = FieldSpec.builder(String.class, "engineNameParent").addModifiers(Modifier.PRIVATE).build();
        FieldSpec fieldBinaryType = FieldSpec.builder(String.class, "DEFAULT_BINARY_VALUE_TYPE").addModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", "Bytes").build();
        FieldSpec fieldModelMapper = FieldSpec.builder(ModelMapper.class, "modelMapper").addModifiers(Modifier.PRIVATE).build();

        List<FieldSpec> fieldSpecs = Arrays.asList(fieldSession, fieldSessionFactory, fieldEngineName,
                fieldObjectMapper, fieldParentObjectMapper, fieldEngineNameParent, fieldBinaryType, fieldModelMapper);

        return fieldSpecs;
    }
}
