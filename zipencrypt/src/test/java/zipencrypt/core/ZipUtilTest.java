package zipencrypt.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipUtilTest {

    private static final Logger logger = Logger.getLogger(ZipUtilTest.class.getName());

    private static final String ZIPTEST = "/home/novier/ziptests";
    private static final String EXTRACTED = "/home/novier/ziptests/extracted";
    private static final String TARGET_ZIP = "/home/novier/ziptests/zip001.zip";
    private static final String SOURCE_FOLDER = "/home/novier/temp";
    private static final String SOURCE_FOLDER_2 = "/home/novier/temp2";

    ZipUtil zipUtil = new ZipUtil("test123");
    
    @Before
    public void createFolders() {
        createDirectory(ZIPTEST);
        createDirectory(SOURCE_FOLDER);
        createDirectory(SOURCE_FOLDER_2);
    }

    @After
    public void deleteAllFilesAndFolders() {
        deleteDirectory(new File(ZIPTEST));
        deleteFile(new File(TARGET_ZIP));
        deleteDirectory(new File(SOURCE_FOLDER));
        deleteDirectory(new File(SOURCE_FOLDER_2));
    }

    @Test
    public void encryptExistingZip_validZip(){
        //zip a File
        createTempTextFile(SOURCE_FOLDER + "/filename1.txt");

        File directory = new File(SOURCE_FOLDER);
        File target = new File(TARGET_ZIP);

        zipUtil.zip(directory, target);
        
        //encrypt the zipped file
        zipUtil.encryptZipFile(target);
        
        assertTrue(fileExists(target.getPath()));
        
        ZipFile zip;
		try {
			zip = new ZipFile(target);
			assertTrue(zip.isEncrypted());
		} catch (ZipException e) {
			logger.log(Level.SEVERE, "Error testing Zip file encryption");
		}
        
    }
    
    @Test
    public void zipFile_valid() {
        createTempTextFile(SOURCE_FOLDER + "/filename1.txt");

        File directory = new File(SOURCE_FOLDER);
        File target = new File(TARGET_ZIP);

        zipUtil.zip(directory, target);

        assertTrue(fileExists(target.getPath()));

    }

    @Test
    public void zipFile_differentLocations() {
        createTempTextFile(SOURCE_FOLDER + "/filename1.txt");
        createTempTextFile(SOURCE_FOLDER_2 + "/filename2.txt");

        Stack<File> queue = new Stack<File>();
        queue.push(new File(SOURCE_FOLDER + "/filename1.txt"));
        queue.push(new File(SOURCE_FOLDER_2 + "/filename2.txt"));

        File target = new File(TARGET_ZIP);
        zipUtil.zip(queue, target);
        assertTrue(fileExists(target.getPath()));

    }

    private void createTempTextFile(String filename) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), "utf-8"));
            writer.write("Something");
        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }

    }

    @Test
    public void unzipFile_valid() {
        // create zip
        createTempTextFile(SOURCE_FOLDER + "/filename1.txt");
        File directory = new File(SOURCE_FOLDER);
        File target = new File(TARGET_ZIP);
        zipUtil.zip(directory, target);

        // unzip
        String source = TARGET_ZIP;
        String destination = EXTRACTED;

        zipUtil.unzip(source, destination);

        assertTrue(directoryExists(destination));
    }

    @Test
    public void zipFile_invalid() {
        File directory = new File("");
        File target = new File("");

        zipUtil.zip(directory, target);

        assertFalse(fileExists(target.getPath()));

    }

    @Test
    public void unzipFile_invalid() {
        String source = "/home/novier/ziptests/abc.zip";
        String destination = "/home/novier/ziptests/extracted";

        zipUtil.unzip(source, destination);

        assertFalse(directoryExists(destination));
    }

    /**
     * Checks if a directory exists at a given path
     *
     * @param source
     * @return
     */
    private boolean directoryExists(String source) {
        File f = new File(source);
        if (f.exists() && f.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the file exists in the give in path
     *
     * @param source
     * @return
     */
    private boolean fileExists(String source) {
        File f = new File(source);
        if (f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * Deletes a file at a given path
     *
     * @param file
     */
    private void deleteFile(File file) {
        try {
            file.delete();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error deleting file " + file.getName());

        }
    }

    /**
     * Deletes a given directory and its content
     *
     * @param file
     */
    private void deleteDirectory(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     *
     * @param foldername
     */
    private void createDirectory(String foldername) {
        if (!directoryExists(foldername)) {
            boolean success = (new File(foldername)).mkdirs();
            if (!success) {
                assertFalse(true);
                logger.log(Level.SEVERE, "Error creating Directory " + foldername);
            }
        }
    }

}
