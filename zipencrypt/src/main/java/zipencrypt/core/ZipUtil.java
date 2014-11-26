package zipencrypt.core;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;

/**
 * This class provide Util methods to zip/unzip encrypted files. The seed
 * password is stored in a configuration file for ease of maintenance.
 *
 * @author novier
 *
 */
public class ZipUtil {

   private static final Logger logger = Logger.getLogger(ZipUtil.class.getName());
   
   private String password;
   
   public ZipUtil(String key){
       this.password = key;
   }
   
   private String getPassword(){
       return password;
   }
   
    
    /**
     * Extracts a given zip file.
     *
     * @param source The source file as String
     * @param destination The target directory as String
     * @return a boolean to determine if successful (true) or not (false)
     */
    public boolean unzip(String source, String destination) {

        String password = getPassword();

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            logger.log(Level.SEVERE, "Error Unzipping the file " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Encrypts an existing zip archive.
     *
     * @param existingzip as File object
     * @return a boolean to determine if successful (true) or not (false)
     */
    public boolean encryptZipFile(File source) {
        
        //extracts the content to a temporary directory
    	String parentfolder = source.getParent();
        String tempfolder = parentfolder+"/tmpzip";
        String filename = source.getName();
        boolean successunzip = unzip(source.getAbsolutePath(), tempfolder);

        //deletes the old file
        if (successunzip) {
            try {
                source.delete();
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "error deleting file " + source.getName());
                return false;
            }
        } else {
            return false;
        }
        
        //creates an encrypted zip
        boolean successzip = zip(new File(tempfolder), new File(parentfolder+"/"+filename));
        
        //deletes the old folder
        if (successunzip) {
            try {
                FileUtils.deleteDirectory(new File(tempfolder));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "error deleting folder, please delete manually " + tempfolder);
            }
        } 
        
        logger.log(Level.FINE, "Zip File successful encrypted");
        return true;        
    }

    /**
     * This method creates an encrypted zip file (AES 256). It takes a Stack of
     * files from different locations and zip them into the specified output zip
     * file.
     *
     * @param stack of files
     * @param outputzipfile
     * @return a boolean to determine if successful (true) or not (false)
     */
    public boolean zip(Stack<File> queue, File outputzipfile) {

        try {
            // deletes file in case zip file already exists
            outputzipfile.delete();

            // Initiate ZipFile object with the path/name of the zip file.
            ZipFile zipFile = new ZipFile(outputzipfile);

            ZipParameters parameters = initZipParams();

            // Archive Files
            while (!queue.isEmpty()) {
                File fileinqueue = queue.pop();
                if (fileinqueue.isDirectory()) {
                    for (File item : fileinqueue.listFiles()) {
                        if (item.isDirectory()) {
                            queue.push(item);
                            parameters.setRootFolderInZip(item.getPath());
                        } else {
                            zipFile.addFile(item, parameters);
                        }
                    }
                } else {
                    zipFile.addFile(fileinqueue, parameters);
                }
            }
        } catch (ZipException e) {
            logger.log(Level.SEVERE, "Error Zipping the file " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * This method creates an encrypted zip file (AES 256). It takes a Directory
     * containing files and zip them into the specified output zip file.
     *
     * @param directory The source directory containing the files to zip
     * @param outputzipfile The output zip file
     * @return a boolean to determine if successful (true) or not (false)
     */
    public boolean zip(File directory, File outputzipfile) {
        if (validFilePath(directory)) {
            try {

                // deletes file in case zip file already exists
                outputzipfile.delete();

                // Initiate ZipFile object with the path/name of the zip file.
                ZipFile zipFile = new ZipFile(outputzipfile);

                ZipParameters parameters = initZipParams();

                // Archive Files
                Stack<File> queue = new Stack<File>();
                queue.push(directory);

                while (!queue.isEmpty()) {
                    directory = queue.pop();
                    for (File item : directory.listFiles()) {
                        if (item.isDirectory()) {
                            queue.push(item);
                            parameters.setRootFolderInZip(item.getPath());
                        } else {
                            zipFile.addFile(item, parameters);
                        }
                    }
                }
            } catch (ZipException e) {
                logger.log(Level.SEVERE, "Error Zipping the file " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param outputzipfile
     * @return
     */
    private boolean validFilePath(File file) {
        if (file.getPath().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Initiate Zip Parameters which define various properties such as
     * compression method, etc.
     *
     * @param directory
     * @return
     */
    private ZipParameters initZipParams() {

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

        // AES_STRENGTH_256 - For both encryption and decryption
        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

        parameters.setPassword(getPassword());
        parameters.setRootFolderInZip("conveylaw/");

        return parameters;
    }

}
