package io.mosip.proxy.abis.controller;

import io.mosip.proxy.abis.entity.BiometricData;
import io.mosip.proxy.abis.dto.ConfigureDto;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
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
import java.util.List;
import java.util.Map;


@CrossOrigin
@RestController
@Api(value = "Abis", description = "Provides API's for configuring proxy Abis", tags = "Proxy Abis config API")
@RequestMapping("config/")
public class ProxyAbisConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyAbisConfigController.class);

    @Autowired
    ProxyAbisConfigService proxyAbisConfigService;

    @RequestMapping(value = "expectation", method = RequestMethod.POST)
    @ApiOperation(value = "Sets expectation")
    public ResponseEntity<String> setExpectation(@Valid @RequestBody Expectation expectation) throws Exception {
        logger.info("Setting expectation" + expectation.getId());
        try {
            proxyAbisConfigService.setExpectation(expectation);
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
            return new ResponseEntity<>(proxyAbisConfigService.getExpectations(), HttpStatus.OK);
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
            proxyAbisConfigService.deleteExpectation(id);
            return new ResponseEntity<>("Successfully deleted expectation "+id, HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception while deleting expectation: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "expectation", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete expectations")
    public ResponseEntity<String> deleteAllExpectations() {
        logger.info("Delete all expectations");
        try {
            proxyAbisConfigService.deleteExpectations();
            return new ResponseEntity<>("Successfully deleted expectations ", HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception while deleting expectations: "+exp.getMessage());
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
            configureDto.setFindDuplicate(proxyAbisConfigService.getDuplicate());
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
            proxyAbisConfigService.setDuplicate(ie.getFindDuplicate());
            logger.info("[Configuration updated] overrideFindDuplicate: "+proxyAbisConfigService.getDuplicate());
            return new ResponseEntity<>("Successfully updated the configuration", HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception in configure request: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "cache", method = RequestMethod.GET)
    @ApiOperation(value = "Get cached biometrics")
    public ResponseEntity<List<String>> getCache()
            throws Exception {
        logger.info("Get cached biometrics Request");
        try {
            return new ResponseEntity<>(proxyAbisConfigService.getCachedBiometrics(), HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception in cache request: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "cache/{hash}", method = RequestMethod.GET)
    @ApiOperation(value = "Get cached biometrics by hash")
    public ResponseEntity<List<String>> getCacheByHash(@PathVariable String hash)
            throws Exception {
        logger.info("Get cached biometrics by hash: "+hash);
        try {
            return new ResponseEntity<>(proxyAbisConfigService.getCachedBiometric(hash), HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception in cache request: "+exp.getMessage());
            throw exp;
        }
    }

    @RequestMapping(value = "cache", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete cached biometrics")
    public ResponseEntity<String> deleteCache()
            throws Exception {
        logger.info("Delete cached biometrics Request");
        try {
            proxyAbisConfigService.deleteAllCachedBiometrics();
            logger.info("[Configuration updated] overrideFindDuplicate: "+proxyAbisConfigService.getDuplicate());
            return new ResponseEntity<>("Successfully deleted cached biometrics", HttpStatus.OK);
        } catch (RuntimeException exp) {
            logger.error("Exception in cache request: "+exp.getMessage());
            throw exp;
        }
    }
}