package fr.simple.edm.controller;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.simple.edm.crawler.filesystem.FilesystemCrawler;
import fr.simple.edm.service.EdmDocumentService;

@Controller
public class EdmCrawlingController {

    private final Logger logger = LoggerFactory.getLogger(EdmCrawlingController.class);
    
    @Inject
    private EdmDocumentService edmDocumentService;
    
    @RequestMapping(value = "/crawl/start", method = RequestMethod.GET, params = {"source"})
    @ResponseStatus(value=HttpStatus.OK)
    public void startCrawling(@RequestParam(value = "source") String source) {
    	logger.info("Begin crawling for source : {}", source);
    	edmDocumentService.snapshotCurrentDocumentsForSource(source);
    }
    @RequestMapping(value = "/crawl/stop", method = RequestMethod.GET, params = {"source"})
    @ResponseStatus(value=HttpStatus.OK)
    public void stopCrawling(@RequestParam(value = "source") String source) {
    	logger.info("End of crawling for source : {}", source);
    	edmDocumentService.deleteUnusedDocumentsBeforeSnapshotForSource(source);
    }
    
    
    @RequestMapping(value = "/crawl/filesystem", method = RequestMethod.GET, params = {"path"})
    @ResponseStatus(value=HttpStatus.OK)
    @ResponseBody
    public String crawlFilesystem(   
            @RequestParam(value = "path") String path, 
            @RequestParam(value = "edmServerHttpAddress", defaultValue = "127.0.0.1:8053") String edmServerHttpAddress,
            @RequestParam(value = "sourceName", defaultValue = "unmanned source") String sourceName
       ) {
        logger.info("Starting crawling on path : '{}'", path);
        try {
            FilesystemCrawler.importFilesInDir(path, edmServerHttpAddress, sourceName);
        } catch (Exception e) {
            logger.error("Failed to crawl '{}' with embedded crawler", path, e);
        }
        return "OK" ;
    }
    
}
