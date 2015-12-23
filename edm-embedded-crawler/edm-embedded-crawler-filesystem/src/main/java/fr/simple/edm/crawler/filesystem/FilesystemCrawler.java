package fr.simple.edm.crawler.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.ClientProtocolException;

import fr.simple.edm.crawler.bridge.EdmConnector;
import fr.simple.edm.domain.EdmDocumentFile;
import fr.simple.edm.domain.EdmNodeType;

@Slf4j
public class FilesystemCrawler {

    private static final EdmConnector edmConnector = new EdmConnector();

    /**
     * 
     * @param filePath
     *            The path of the directory to crawl For example :
     *            /media/raid/documents
     * @param edmServerHttpAddress
     *            The address of the EDM webapp HTTP address For example :
     *            127.0.0.1:8053
     * @param sourceName
     *            A unique name for this source of documents For example :
     * @param exclusionRegex
     *            Documents names which match with this regex will be ignored
     * 
     * @throws IOException
     */
    public static void importFilesInDir(String filePath,
            final String edmServerHttpAddress, final String sourceName,
            final String categoryName, final String exclusionRegex)
            throws IOException {
        // create parents
        String categoryId = edmConnector.getIdFromCategoryByCategoryName(
                edmServerHttpAddress, categoryName);
        String sourceId = edmConnector.getIdFromSourceBySourceName(
                edmServerHttpAddress, sourceName, categoryId);

        // index
        log.debug("The source ID is {}", sourceId);
        edmConnector.notifyStartCrawling(edmServerHttpAddress, sourceName);
        importFilesInDir(filePath, edmServerHttpAddress, sourceId,
                exclusionRegex);
        edmConnector.notifyEndOfCrawling(edmServerHttpAddress, sourceName);
    }

    public static boolean isExcluded(String filePath, String exclusionPattern) {
        boolean toExclude = !exclusionPattern.isEmpty()
                && Pattern.compile(exclusionPattern).matcher(filePath).find();
        log.debug("Check if '{}' match with '{}' : {}", filePath,
                exclusionPattern, toExclude);
        return toExclude;
    }

    private static void importFilesInDir(String filePath,
            final String edmServerHttpAddress, final String sourceId,
            final String exclusionRegex) throws ClientProtocolException {

        log.info("Embedded crawler looks for : " + filePath);

        // exclusion pattern
        if (isExcluded(filePath, exclusionRegex)) {
            log.info("File excluded because it matches with exclusion regex");
            return;
        }

        File file = new File(filePath);

        // recursive crawling
        if (file != null && file.isDirectory()) {
            log.debug("... is a directory !");
            for (File subFile : file.listFiles()) {
                importFilesInDir(filePath + "/" + subFile.getName(),
                        edmServerHttpAddress, sourceId, exclusionRegex);
            }

            // release memory
            file = null;
        }

        // add files
        if (file != null && file.isFile()) {
            log.debug("... is a file !");

            double bytes = file.length();
            double kilobytes = bytes / 1024;
            double megabytes = kilobytes / 1024;

            if (megabytes > 100) {
                log.warn("Skipping too big file ({})", filePath);
            } else {
                // upload the file
                String fileToken = edmConnector.uploadFile(
                        edmServerHttpAddress, file);

                // construct DTO
                EdmDocumentFile document = new EdmDocumentFile();
                document.setDate(new Date(file.lastModified()));
                document.setNodePath(filePath.replaceAll("\\\\", "/"));
                document.setEdmNodeType(EdmNodeType.DOCUMENT);
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
            log.debug("... is nothing !");

            // release memory
            file = null;
        }
    }
}
