package io.mosip.proxy.abis.controller;

import io.mosip.proxy.abis.entity.ConfigureDto;
import io.mosip.proxy.abis.entity.Expectation;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;


@CrossOrigin
@RestController
@Api(value = "Abis", description = "Provides API's for configuring proxy Abis", tags = "Proxy Abis config API")
@RequestMapping("api/v0/proxyabisconfig/")
public class ProxyAbisConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyAbisConfigController.class);

    @Autowired
    ProxyAbisInsertService abisInsertService;

    @RequestMapping(value = "expectation", method = RequestMethod.POST)
    @ApiOperation(value = "Sets expectation")
    public ResponseEntity<String> setExpectation(@Valid @RequestBody Expectation expectation) throws Exception {
        logger.info("Setting expectation" + expectation.getId());
        try {
            abisInsertService.setExpectation(expectation);
            return new ResponseEntity<>("Successfully inserted expectation "+expectation.getId(), HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception while getting expectation: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "expectation", method = RequestMethod.GET)
    @ApiOperation(value = "Gets expectation")
    public ResponseEntity<Map<String, Expectation>> getExpectation() throws Exception {
        logger.info("Getting expectation");
        try {
            return new ResponseEntity<>(abisInsertService.getExpectations(), HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception while getting expectation: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "expectation/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete expectation")
    public ResponseEntity<String> deleteExpectation(@PathVariable String id) {
        logger.info("Delete expectation: "+id);
        try {
            abisInsertService.deleteExpectation(id);
            return new ResponseEntity<>("Successfully deleted expectation "+id, HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception while getting expectation: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "configure", method = RequestMethod.GET)
    @ApiOperation(value = "Configure Request")
    public ResponseEntity<ConfigureDto> checkConfiguration()
            throws Exception {
        logger.info("Configure Request");
        try {
            ConfigureDto configureDto = new ConfigureDto();
            configureDto.setFindDuplicate(abisInsertService.getDuplicate());
            return new ResponseEntity<>(configureDto, HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception while getting configuration: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "configure", method = RequestMethod.POST)
    @ApiOperation(value = "Configure Request")
    public ResponseEntity<String> configure(@Valid @RequestBody ConfigureDto ie, BindingResult bd)
            throws Exception {
        logger.info("Configure Request");
        try {
            abisInsertService.setDuplicate(ie.getFindDuplicate());
            logger.info("[Configuration updated] overrideFindDuplicate: "+abisInsertService.getDuplicate());
            return new ResponseEntity<>("Successfully updated the configuration", HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception in configure request: "+exp.getMessage());
            throw exp;
        }
    }
}