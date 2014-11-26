package com.riccardonoviello.simpleimapclient.utils;

import com.riccardonoviello.simpleimapclient.models.MailAddress;
import com.riccardonoviello.simpleimapclient.models.MessageDTO;
import com.riccardonoviello.simpleimapclient.models.EmailAttachment;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 *
 * @author novier
 */
public class EmailUtils {
    
    private final static Logger logger = Logger.getLogger(EmailUtils.class.getName());
    
    /**
     * 
     * @param s
     * @return 
     */
    public static MailAddress[] mapStringToMailAddressArray(String s, String name){
        List<MailAddress> simpleaddresses = new ArrayList<>();
        simpleaddresses.add(new MailAddress(s, name));
        return simpleaddresses.toArray(new MailAddress[simpleaddresses.size()]);
    }
    
    /**
     * Converts an array of Java Mail address to an array of simple MailAddress
     *
     * @param addresses
     * @return
     */
    public static MailAddress[] mapJavaMailAddressToAddress(Address[] addresses) {
        List<MailAddress> simpleaddresses = new ArrayList<>();
        if (addresses != null) {
            for (Address item : addresses) {
                InternetAddress ia = (InternetAddress) item;
                MailAddress mail = new MailAddress(ia.getAddress(), ia.getPersonal());
                simpleaddresses.add(mail);
            }
        }
        // convert to List
        return simpleaddresses.toArray(new MailAddress[simpleaddresses.size()]);
    }

    /**
     * Jsonify a given list
     *     
* @param collection
     * @return json
     */
    public static String jsonify(List<?> collection, ObjectMapper mapper) {
        String Json = "[ ]";
        try {
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, collection);
            Json = sw.toString();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error jsonifying pojo", ioe);
        }
        return Json;
    }

    /**
     * Converts a Java Mail Address to a MailAddress object
     *
     * @param item
     * @return
     */
    public static MailAddress mapJavaMailAddressToAddress(Address item) {
        InternetAddress ia = (InternetAddress) item;
        MailAddress mail = new MailAddress(ia.getAddress(), ia.getPersonal());
        return mail;
    }

    /**
     * Converts an array of MailAddress to an array of Java Mail Addresses
     *
     * @param addresses
     * @return
     */
    public static Address[] mapAddresstoJavaMailAddress(MailAddress[] addresses) {
        if (addresses != null) {
            List<InternetAddress> internetaddresses = new ArrayList<>();
            for (MailAddress item : addresses) {
                try {
                    InternetAddress ia = new InternetAddress(item.getAddress(), item.getPersonal());
                    internetaddresses.add(ia);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(EmailUtils.class.getName()).log(Level.SEVERE, "Error Mapping Email Addresss to JavaMail Address", ex);
                }
            }
            // convert to List
            return internetaddresses.toArray(new InternetAddress[internetaddresses.size()]);
        }
        return new Address[0];
    }

    /**
     * Converts one MailAddress to one Java Mail Address object
     *
     * @param address
     * @return
     */
    public static Address mapAddresstoJavaMailAddress(MailAddress address) {
        try {
            InternetAddress ia = new InternetAddress(address.getAddress(), address.getPersonal());
            return ia;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EmailUtils.class.getName()).log(Level.SEVERE, "Error Mapping Email Addresss to JavaMail Address", ex);
        }
        return null;
    }

    /**
     * Factory method to generate appropriate Flags on Message in order to
     * update the Message Status (Read, Unread, Deleted, etc)
     *
     * @param message
     * @param action
     * @return
     */
    public static MessageDTO flagsMessageFactory(MessageDTO message, String action) {
        switch (action.toUpperCase()) {

            case "UNREAD": {
                message.setSeen(false);
                break;
            }
            case "READ": {
                message.setSeen(true);
                break;
            }
            case "DELETED": {
                message.setDeleted(true);
                break;
            }
            case "ANSWERED": {
                message.setAnswered(true);
                break;
            }
        }
        return message;
    }

    /**
     * Filter the attachments. This will return us with a list of attachments
     * that have been selected by the user. Since the user can upload and then
     * delete an attachment we need to have this mechanism of comparing list of
     * names with in Memory attachments, in order to only send via SMTP the
     * attachments selected.
     *
     * @param attachments
     * @param filter
     * @return
     */
    public static List<EmailAttachment> filterAttachments(List<EmailAttachment> attachments, List<String> filter) {
        Set<EmailAttachment> set = new HashSet<EmailAttachment>();

        if (filter != null && attachments != null) {
            for (EmailAttachment item : attachments) {
                for (String f : filter) {

                    if (item.getFileName().equalsIgnoreCase(f)) {
                        set.add(item);
                        break;
                    }
                }
            }
        }
        List<EmailAttachment> list = Arrays.asList(set.toArray(new EmailAttachment[set.size()]));

        return list;
    }

    /**
     *
     * @param attachmentName
     * @param attachments
     * @return
     */
    public static EmailAttachment findAttachmentInList(String attachmentName, List<EmailAttachment> attachments) {
        for (EmailAttachment item : attachments) {
            if (item.getFileName().toLowerCase().contains(attachmentName.toLowerCase())) {
                return item;
            }
        }
        return null;
    }


    /**
     * Converts an array of MailAddress to CSV string
     *
     * @param ccs
     * @return
     */
    public static String convertMailAddressArrayToStringArray(MailAddress[] array) {
        StringBuffer buf = new StringBuffer();
        String csv = null;
        if (array.length > 0) {
            for (MailAddress address : array) {
                buf.append(address.getAddress() + ", ");
            }
            csv = buf.toString().substring(0, buf.length() - 2);
        }
        return csv;
    }
    
    
    /**
     * Search if a string is contained into an array. The search is case
     * insensitive.
     *
     * @param array of possible matches
     * @param search the string to search for
     * @return true if the string has been found in the given array
     */
    public static boolean isTextInArray(String[] array, String search) {
        boolean found = false;
        for (String item : array) {
            if (item.toLowerCase().contains(search.toLowerCase())) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Search if any string in the array is contained in a given text. The
     * search is case insensitive.
     *
     * @param array of possible matches
     * @param text
     * @return true if any of the array strings is found in the given text
     */
    public static boolean isAnyWordFromArrayInText(String[] array, String text) {
        boolean found = false;
        if (text != null) {
            for (String item : array) {
                if (text.toLowerCase().contains(item.toLowerCase())) {
                    found = true;
                }
            }
        }
        return found;
    }

    /**
     * Clones an InputStream
     *
     * @param input
     * @return
     */
    public static InputStream cloneInputStream(InputStream input) {
        InputStream is = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Fake code simulating the copy
            // You can generally do better with nio if you need...
            // And please, unlike me, do something about the Exceptions :D
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            // Open new InputStreams using the recorded bytes
            // Can be repeated as many times as you wish
            is = new ByteArrayInputStream(baos.toByteArray());

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error cloning Input Stream", ex);
        }
        return is;
    }
}
