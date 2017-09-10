package com.hemendra.mdmgenerator.controller;

import com.hemendra.mdmgenerator.generator.GenerateDto;
import com.hemendra.mdmgenerator.generator.GenerateEntity;
import com.hemendra.mdmgenerator.generator.GenerateWebServiceRest;
import com.hemendra.mdmgenerator.model.JsonPayloadModel;
import com.squareup.javapoet.JavaFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hemendra
 */
@RestController
@RequestMapping("/generate")
public class IndexController {

    @Autowired
    GenerateDto generateDto;

    @Autowired
    GenerateEntity generateEntity;

    @Autowired
    GenerateWebServiceRest generateWebServiceRest;

    @PostMapping("/entity-dto")
    private ResponseEntity<byte []> generateEntityAndDto(@RequestBody JsonPayloadModel jsonPayloadModel) {
        JavaFile dtoFile = generateDto.generate(jsonPayloadModel);
        JavaFile entityFile = generateEntity.generate(jsonPayloadModel);
        JavaFile restInterfaceFile = generateWebServiceRest.generate(jsonPayloadModel);
        return null;
    }
}
