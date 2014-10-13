package fr.simple.edm.crawler.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.simple.edm.common.EdmNodeType;
import fr.simple.edm.common.dto.EdmDocumentFileDto;
import fr.simple.edm.crawler.bridge.EdmConnector;


public class FilesystemCrawler {
	
    private static final Logger logger = LoggerFactory.getLogger(FilesystemCrawler.class);
    
	private static final EdmConnector edmConnector = new EdmConnector();
	
	/**
	 * 
	 * @param filePath             The path of the directory to crawl
	 *                             For example : /media/raid/documents
	 * @param edmServerHttpAddress The address of the EDM webapp HTTP address
	 *                             For example : 127.0.0.1:8053
	 * @param sourceName           A unique name for this source of documents
	 *                             For example : 
	 * @throws IOException 
	 */
	public static void importFilesInDir(String filePath, final String edmServerHttpAddress, final String sourceName, final String categoryName) throws IOException {
		// create parents
		String categoryId = edmConnector.getIdFromCategoryByCategoryName(edmServerHttpAddress, categoryName);
		String sourceId = edmConnector.getIdFromSourceBySourceName(edmServerHttpAddress, sourceName, categoryId);
		
		// index
		logger.debug("The source ID is {}", sourceId);
		edmConnector.notifyStartCrawling(edmServerHttpAddress, sourceName);
		importFilesInDir(filePath, edmServerHttpAddress, sourceId);
		edmConnector.notifyEndOfCrawling(edmServerHttpAddress, sourceName);
	}
	
	
	private static void importFilesInDir(String filePath, final String edmServerHttpAddress, final String sourceId) throws ClientProtocolException {
		
		logger.info("Embedded crawler looks for : " + filePath);
		
		//try {
		//	Thread.sleep(500);
		//} catch (Exception e) {
		//}
		
		File file = new File(filePath);
				
		// recursive crawling
		if (file != null && file.isDirectory()) {
		    logger.debug("... is a directory !");
			for (File subFile : file.listFiles()) {
			    importFilesInDir(filePath + "/" + subFile.getName(), edmServerHttpAddress, sourceId);
			}
			
			// release memory
			file = null;
		}
				
		// add files
		if (file != null && file.isFile()) {
		    logger.debug("... is a file !");
			
			double bytes = file.length();
			double kilobytes = (bytes / 1024);
			double megabytes = (kilobytes / 1024);
			
			if (megabytes > 100) {
				logger.warn("Skipping too big file ({})", filePath);
			}
			else {
				// upload the file
				String fileToken = edmConnector.uploadFile(edmServerHttpAddress, file);
				
				// construct DTO
				EdmDocumentFileDto document = new EdmDocumentFileDto();
				document.setDate(new Date(file.lastModified()));
				document.setNodePath(filePath.replaceAll("\\\\", "/"));
				document.setEdmNodeType(EdmNodeType.DOCUMENT);
				document.setName(file.getName().replaceFirst("[.][^.]+$", "")); // without extension (http://stackoverflow.com/questions/924394/how-to-get-file-name-without-the-extension)
				document.setParentId(sourceId);
				document.setTemporaryFileToken(fileToken);

				// save DTO
				edmConnector.saveEdmDocument(edmServerHttpAddress, document);
			}
			
			// release memory
			file = null;
		}
		
		// other type
		if (file != null) {
			logger.debug("... is nothing !");
			
			// release memory
			file = null;
		}
	}
}
