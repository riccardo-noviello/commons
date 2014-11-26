package com.riccardonoviello.simpleimapclient.core;

import com.riccardonoviello.simpleimapclient.models.EmailAttachment;
import com.riccardonoviello.simpleimapclient.models.EmailFolderDto;
import com.riccardonoviello.simpleimapclient.models.MessageDTO;
import com.riccardonoviello.simpleimapclient.utils.EmailUtils;
import com.sun.mail.imap.IMAPFolder;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author novier
 */
//@Service
//@Scope("prototype") //this should not be a aingleton. Return a new bean instance each time when requested
public class ImapServiceImpl implements ImapService {

    private static final int BUFFER_SIZE = 1024;
    private final static Logger logger = Logger.getLogger(ImapService.class.getName());
    private final static int PARSE_FULL_ATTACHMENT = 1;
    private final static int PARSE_FILENAME_ONLY = 0;

    private static Map<String, String> settings = new HashMap<String, String>();

    private Store storeInstance;
    private Session imapsession;
    private Session smtpsession;

    /**
     *
     * @param imap
     * @param email
     * @param password
     */
    @Override
    public boolean setup(String imap, String email, String password, String smtp) {
        settings.put("imap", imap);
        settings.put("email", email);
        settings.put("password", password);
        settings.put("smtp", smtp);

        return ensureConnection();
    }

    /**
     *
     * @return
     */
    @Override
    public List<EmailFolderDto> getAvailableFolders() {
        List<EmailFolderDto> listoffolders = new ArrayList<EmailFolderDto>();
        String[] exclude = {"Gmail", "Starred", "Important", "All Mail", "Draft"};

        try {
            Folder[] folders = this.getStore().getDefaultFolder().list("*");
            for (Folder item : folders) {
                String foldername = item.getName();
                boolean filter = false;
                // filter folders that belongs to the 'exclude' list
                for (String ex : exclude) {
                    if (foldername.toLowerCase().contains(ex.toLowerCase())) {
                        filter = true;
                    }
                }
                // adding the folder to the array
                if (filter == false) {
                    EmailFolderDto folder = new EmailFolderDto();
                    folder.setName(foldername);
                    folder.setIcon(factoryIconForFolder(foldername));
                    folder.setUnreadCount(item.getUnreadMessageCount());
                    folder.setTotalCount(item.getMessageCount());
                    listoffolders.add(folder);
                }
            }

        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Could not get Available IMAP folders.", ex);
        }

        // finally order the list to be displayed to the user
        listoffolders = orderList(listoffolders);

        return listoffolders;
    }

    /**
     *
     * @param foldername
     * @return
     */
    @Override
    public List<MessageDTO> getEmails(String foldername, int maxnumber, long begin, int offset) {
        int emailcounter = 0;
        List<MessageDTO> messages = new ArrayList<MessageDTO>();
        try {
            Long timestart = System.currentTimeMillis();
            IMAPFolder folder = null;
            try {
                // try to open folder as it is
                folder = openReadOnlyFolder(foldername);
            } catch (MessagingException me) {
                // try again to see if this is a subfolder
                String subfoldername = this.getFolderFullName(foldername);
                folder = openReadOnlyFolder(subfoldername);
            }
            // Get the messages in the folder
            Message[] msgs = folder.getMessages();
            int number = (maxnumber > msgs.length) ? msgs.length : maxnumber;

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            //fp.add(FetchProfile.Item.CONTENT_INFO);
            //fp.add("X-mailer");
            folder.fetch(msgs, fp);

            Long timeend = System.currentTimeMillis();
            logger.info("Metrics ### Time Ellapsed Opening Folder " + (timeend - timestart));

            int start = msgs.length - 1 - offset;
            int end = start - number;
            // make sure there are enough emails in the folders to fetch
            end = (end < 0) ? 0 : end;

            timestart = System.currentTimeMillis();

            // build a list of MessageDTOs
            for (int i = start; i > end; i--) {
                Message item = msgs[i];

                Long uid = folder.getUID(item);
                if (uid > begin) {
                    MessageDTO message = mapMessageToDTO(item, uid, foldername, PARSE_FILENAME_ONLY);
                    messages.add(message);
                    emailcounter++;
                    System.out.println("### READ EMAILS: " + emailcounter);
                }
            }
            timeend = System.currentTimeMillis();
            logger.info("Metrics ### Time Ellapsed Building Messages List " + (timeend - timestart));

            // Close connection
            folder.close(false);

        } catch (NoSuchProviderException nspe) {
            logger.log(Level.SEVERE, "Invalid provider name", nspe);
        } catch (MessagingException me) {
            logger.log(Level.SEVERE, "Error getting Messages", me);
        } catch (IOException ex) {
            Logger.getLogger(ImapService.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        return messages;
    }

    /**
     * This says if the message exists in the specified folder. This method can
     * be used to verify messages have been deleted or moved from their original
     * folder.
     *
     * @param item
     * @return
     */
    public boolean isInvalidMessage(MessageDTO item) {
        boolean isinvalid = false;
        try {
            // open the folder            
            IMAPFolder folder = openReadOnlyFolder(item.getFolderName());

            Message msg = folder.getMessageByUID(item.getUid());

            isinvalid = (msg == null) ? true : false;

            folder.close(true);

        } catch (NoSuchProviderException nspe) {
            logger.log(Level.SEVERE, "invalid provider name", nspe);
        } catch (MessagingException me) {
            logger.log(Level.SEVERE, "Error validating Message", me);
        }

        return isinvalid;
    }

    /**
     *
     * @param message
     * @param destFolder
     */
    @Override
    public void moveFolder(final MessageDTO message, String destFolder) {
        try {
            if (message.getFolderName() != null) {
                IMAPFolder folder = openReadWriteFolder(message.getFolderName());
                IMAPFolder destinationfolder = openReadWriteFolder(destFolder);

                // Copy the message from source folder to destination folder
                Message foundmsg = folder.getMessageByUID(message.getUid());
                if (foundmsg != null) {
                    folder.copyMessages(new Message[]{foundmsg}, destinationfolder);

                    // Delete the message from source folder, once it has been copied
                    this.deleteMessage(message);

                    if (folder.isOpen()) {
                        folder.close(true);
                    }
                    if (destinationfolder.isOpen()) {
                        destinationfolder.close(true);
                    }
                }
            }

        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error moving Message to folder " + destFolder + " .", ex);
        }
    }

    /**
     *
     * @param message
     */
    @Override
    public void updateMessageLinkToCase(MessageDTO message) {
        try {
            if (message.getLinkCaseId() != null) {
                IMAPFolder folder = openReadWriteFolder(message.getFolderName());

                Message msg = folder.getMessageByUID(message.getUid());
                Flags processedFlag = new Flags("link-to-case#" + message.getLinkCaseId());
                folder.setFlags(new Message[]{msg}, processedFlag, true);

                folder.close(true);
            }
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error updating Message Flags", ex);
        }
    }

    /**
     *
     * @param message
     */
    @Override
    public void updateMessageFlag(MessageDTO message) {
        try {
            if (message.getFolderName() != null) {
                IMAPFolder folder = openReadWriteFolder(message.getFolderName());

                Message msg = folder.getMessageByUID(message.getUid());
                // build Flags Object
                Flags flags = new Flags();
                boolean flagtrue = true;
                if (message.isAnswered()) {
                    flags.add(Flag.ANSWERED);
                }
                if (message.isDeleted()) {
                    flags.add(Flag.DELETED);
                }
                if (message.isRecent()) {
                    flags.add(Flag.RECENT);
                }
                if (message.isSeen()) {
                    flags.add(Flag.SEEN);
                }
                if (message.isSeen() == false) {
                    flags.add(Flag.SEEN);
                    flagtrue = false;
                }

                folder.setFlags(new Message[]{msg}, flags, flagtrue);

                folder.close(true);
            }
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error updating Message Flags", ex);
        }
    }

    /**
     *
     * @param uuid
     * @param foldername
     * @return
     */
    @Override
    public MessageDTO findOne(Long uuid, String foldername) {
        MessageDTO message = new MessageDTO();
        try {
            IMAPFolder folder = openReadOnlyFolder(foldername);

            Message msg = folder.getMessageByUID(uuid);
            if (msg != null) {
                message = mapMessageToDTO(msg, uuid, foldername, PARSE_FULL_ATTACHMENT);
            }

            folder.close(false);

        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error finding a Message", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error mapping Message to MessageDTO", ex);
        }
        return message;
    }

    /**
     *
     * @param message
     */
    @Override
    public void moveToBin(MessageDTO message) {
        String binfolder = findBinFolder();
        moveFolder(message, binfolder);
    }

    /**
     *
     * @param message
     */
    @Override
    public void deleteMessage(MessageDTO message) {

        try {
            IMAPFolder folder = openReadWriteFolder(message.getFolderName());

            Message msg = folder.getMessageByUID(message.getUid());
            if (msg != null) {
                folder.setFlags(new Message[]{msg}, new Flags(Flag.DELETED), true);
            }

            folder.expunge();
            folder.close(true);

        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error deleting Message", ex);
        }
    }

    /**
     *
     * @param message
     */
    @Override
    public void sendMail(MessageDTO message) {

        // set some more fields before sending Message
        message = sentSentDate(message);
        message.setFolderName("INBOX");

        smtpSendMail(message);

    }
    
    @Override
    public Message prepareMessageToSend(MessageDTO message) {

        // set some more fields before sending Message
        message = sentSentDate(message);
        message.setFolderName("INBOX");

        Message mail = mapDTOToMessageHtml(message, smtpsession);
        return mail;
    }

    /**
     *
     * @param term
     * @param foldername
     * @return
     */
    @Override
    public MessageDTO searchOne(SearchTerm term, String foldername) {
        MessageDTO foundmessage = null;
        try {
            // creates a search criterion
            IMAPFolder folder = openReadOnlyFolder(foldername);

            Message[] returned = folder.search(term);
            if (returned != null) {
                Long uuid = folder.getUID(returned[0]);
                foundmessage = mapMessageToDTO(returned[0], uuid, foldername, PARSE_FULL_ATTACHMENT);
            }
            folder.close(false);
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error searching Message", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error mapping Message to DTO", ex);
        }
        return foundmessage;
    }

    /**
     *
     * @param term
     * @param foldername
     * @return
     */
    @Override
    public List<MessageDTO> search(SearchTerm term, String foldername) {
        List<MessageDTO> list = new ArrayList<MessageDTO>();

        try {
            // creates a search criterion
            IMAPFolder folder = openReadOnlyFolder(foldername);

            Message[] returned = folder.search(term);
            for (Message item : returned) {
                Long uuid = folder.getUID(item);
                MessageDTO foundmessage = mapMessageToDTO(item, uuid, foldername, PARSE_FILENAME_ONLY);
                list.add(foundmessage);
            }
            folder.close(false);
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error searching Messages", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error mapping Message to DTO", ex);
        }
        return list;
    }

    /**
     *
     * @return
     */
    public String findBinFolder() {
        String[] possiblefoldername = new String[]{"bin", "trash", "deleted"};
        String matchname = null;
        List<EmailFolderDto> folders = this.getAvailableFolders();

        for (EmailFolderDto folder : folders) {
            for (String s : possiblefoldername) {
                if (folder.getName().toLowerCase().contains(s)) {
                    matchname = folder.getName();
                }
            }
        }
        return matchname;
    }

    /**
     *
     * @param dto
     * @param session
     * @return
     */
    private Message mapDTOToMessagePlainText(MessageDTO dto, Session session) {
        try {
            Message message = new MimeMessage(session);

            message.setFrom(EmailUtils.mapAddresstoJavaMailAddress(dto.getFrom()[0]));
            message.addRecipients(Message.RecipientType.TO, EmailUtils.mapAddresstoJavaMailAddress(dto.getAllRecipients()));
            if (dto.getCcs().length > 0) {
                message.addRecipients(Message.RecipientType.CC, EmailUtils.mapAddresstoJavaMailAddress(dto.getCcs()));
            }
            message.setSubject(dto.getSubject());

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            if (dto.getBody() != null) {
                messageBodyPart.setText(dto.getBody());
            }
            // Create a multipar message
            Multipart multipart = new MimeMultipart("mixed");

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is the attachment, if any exists
            if (dto.getAttachments().size() > 0) {
                List<EmailAttachment> attachments = dto.getAttachments();
                for (EmailAttachment item : attachments) {
                    messageBodyPart = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource(item.getInputStream(), "application/octet-stream");
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(parseFileName(item.getFileName()));
                    multipart.addBodyPart(messageBodyPart);
                }
            }
            // Send the complete message parts
            message.setContent(multipart);

            return message;
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error Creating Message from DTO ", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error parsing attachment", ex);
        }
        return null;
    }

    /**
     *
     * @param dto
     * @param session
     * @return
     */
    private Message mapDTOToMessageHtml(MessageDTO dto, Session session) {
        try {
            Message message = new MimeMessage(session);

            message.setFrom(EmailUtils.mapAddresstoJavaMailAddress(dto.getFrom()[0]));
            message.addRecipients(Message.RecipientType.TO, EmailUtils.mapAddresstoJavaMailAddress(dto.getAllRecipients()));
            if (dto.getCcs().length > 0) {
                message.addRecipients(Message.RecipientType.CC, EmailUtils.mapAddresstoJavaMailAddress(dto.getCcs()));
            }
            message.setSubject(dto.getSubject());

            // Create the message part
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            if (dto.getBody() != null) {
                attachmentBodyPart.setContent(dto.getBody(), "text/html");
            }

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(attachmentBodyPart);

            // Part two is the attachment, if any exists
            if (dto.getAttachments() != null) {
                List<EmailAttachment> attachments = dto.getAttachments();
                for (EmailAttachment item : attachments) {
                    if (item.isInline()) {
                        // inline attachment
                 //       multipart.addBodyPart(createInlineImagePart(item));
                        attachmentBodyPart = new MimeBodyPart();
                        DataSource source = new ByteArrayDataSource(item.getInputStream(), "application/octet-stream");
                        attachmentBodyPart.setDataHandler(new DataHandler(source));
                        attachmentBodyPart.setFileName(parseFileName(item.getFileName()));
                        attachmentBodyPart.setDisposition(MimeBodyPart.INLINE);
                        attachmentBodyPart.setHeader("Content-ID", "<"+item.getInlineId()+">");
                        multipart.addBodyPart(attachmentBodyPart);
                    } else {
                        // normal attachment
                        attachmentBodyPart = new MimeBodyPart();
                        DataSource source = new ByteArrayDataSource(item.getInputStream(), "application/octet-stream");
                        attachmentBodyPart.setDataHandler(new DataHandler(source));
                        attachmentBodyPart.setFileName(parseFileName(item.getFileName()));
                    }
                }
            }
            // Send the complete message parts
            message.setContent(multipart);

            return message;

        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error Creating Message from DTO ", ex);
        } catch (IOException ex) {
            Logger.getLogger(ImapServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * 
     * @param base64EncodedImageContentByteArray
     * @return
     * @throws MessagingException 
     */
    private BodyPart createInlineImagePart(EmailAttachment attachment) throws MessagingException, IOException {
        
        byte[] base64EncodedImageContentByteArray = IOUtils.toByteArray(attachment.getInputStream());
        
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("Content-Type", "image/png");
        headers.addHeader("Content-Transfer-Encoding", "base64");
        MimeBodyPart imagePart = new MimeBodyPart(headers, base64EncodedImageContentByteArray);
        imagePart.setDisposition(MimeBodyPart.INLINE);
        imagePart.setContentID("&lt;image&gt;");
        imagePart.setFileName(attachment.getFileName());
        return imagePart;
    }

    /**
     *
     * @param item
     * @param uid
     * @param foldername
     * @param option
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    private MessageDTO mapMessageToDTO(Message item, Long uid, String foldername, int option) throws MessagingException, IOException {
        MessageDTO message = new MessageDTO();

        // set flags first
        message = setFlagsFromMessage(item.getFlags(), message);

        message.setUid(uid);
        message.setRecipients(EmailUtils.mapJavaMailAddressToAddress(item.getRecipients(Message.RecipientType.TO)));
        message.setCcs(EmailUtils.mapJavaMailAddressToAddress(item.getRecipients(Message.RecipientType.CC)));

        // Link Case Id
        for (String userflag : item.getFlags().getUserFlags()) {
            if (userflag.contains("link-to-case")) {
                String result = userflag.substring("link-to-case#".length());
                Long caseId = Long.parseLong(result);
                message.setLinkCaseId(caseId);
            }
        }

        Object content = item.getContent();

        message.setBody(parseMessageBody(content));

        // parse all attachment
        List<EmailAttachment> attachments = parseMessageAttachment(content, option);

        // add attachment and filenames
        List<String> filenames = new ArrayList<String>();
        for (EmailAttachment att : attachments) {
            filenames.add(att.getFileName());
        }

        message.setFileNames(filenames);
        message.setAttachments(attachments);
        message.setFrom(EmailUtils.mapJavaMailAddressToAddress(item.getFrom()));
        message.setSentDate(item.getSentDate());
        message.setSubject(item.getSubject());
        message.setReceivedDate(item.getReceivedDate());
        message.setFolderName(foldername);

        return message;
    }

    /**
     *
     * @param content
     * @return
     */
    private List<EmailAttachment> parseMessageAttachment(Object content, int option) {
        List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();

        try {
            Multipart multiPart = (Multipart) content;

            for (int i = 0; i < multiPart.getCount(); i++) {
                EmailAttachment attachment = new EmailAttachment();
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) | Part.INLINE.equalsIgnoreCase(part.getDisposition())) {
                    attachment.setFileName(part.getFileName());
                    attachment.setMimeType(parseMimeType(part.getContentType()));
                    attachment.setEncoding(part.getEncoding());
                    if (option == PARSE_FULL_ATTACHMENT) {
                        attachment.setInputStream(EmailUtils.cloneInputStream(part.getInputStream()));
                    }
                }
                if (attachment.getFileName() != null) {
                    attachments.add(attachment);
                }
            }
        } catch (MessagingException | IOException ex) {
            logger.log(Level.SEVERE, "Error parsing attachment", ex);
        } catch (ClassCastException e) {
            //nothing
        }
        return attachments;
    }

    /**
     *
     * @param content
     * @return
     */
    private String parseMimeType(String content) {
        String result[] = content.split(" ");
        return result[0].substring(0, result[0].length() - 1);
    }

    /**
     *
     * @param content
     * @return
     */
    private String parseMessageBody(Object msgcontent) {
        String result = null;
        try {
            if (msgcontent instanceof Multipart) {
                // a multi part message
                result = parseMultiPart((Multipart) msgcontent);

            } else if (msgcontent instanceof BodyPart) {
                // a body part message
                result = getText((Part) msgcontent);

            } else if (msgcontent instanceof String) {
                // a simple text message
                result = (String) msgcontent;
            }

        } catch (MessagingException | IOException e) {
            logger.log(Level.SEVERE, "Could not get Body from Message Content ", e);
        }
        return result;
    }

    /**
     * Return the primary text content of the message.
     */
    private String getText(Part part) throws
            MessagingException, IOException {
        if (part.isMimeType("text/*")) {
            String s = (String) part.getContent();
            // textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (part.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) part.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null) {
                        text = getText(bp);
                    }
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null) {
                    return s;
                }
            }
        }

        return null;
    }

    /**
     *
     * @param msgcontent
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    private String parseMultiPart(Multipart msgcontent) throws MessagingException, IOException {
        String result = null;
        Multipart content = (Multipart) msgcontent;
        int count = content.getCount();

        // find the parts of the message
        for (int i = 0; i < count; i++) {
            BodyPart part = content.getBodyPart(i);

            result = getText(part);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Builds and returns a connected java.mail.Store object
     *
     * @return
     * @throws NoSuchProviderException
     * @throws MessagingException
     */
    private Store buildStoreConnection() {
        Long timestart = System.currentTimeMillis();
        Store st = null;
        try {
            // Connect to the server
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            String provider = "imaps";
            imapsession = Session.getDefaultInstance(props, null);
            st = imapsession.getStore(provider);
            st.connect(settings.get("imap"), settings.get("email"), settings.get("password"));
        } catch (NoSuchProviderException ex) {
            logger.log(Level.SEVERE, "error conencting to IMAP Store", ex);
        } catch (AuthenticationFailedException ex) {
            // silent            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error conencting to IMAP Store", e);
        }
        Long timeend = System.currentTimeMillis();

        logger.info("Metrics ### Time Ellapsed Connecting " + (timeend - timestart));
        return st;
    }

    /**
     *
     * @param foldername
     * @return
     * @throws MessagingException
     */
    private IMAPFolder openReadOnlyFolder(String foldername) throws MessagingException {

        IMAPFolder folder = (IMAPFolder) getStore().getFolder(getSpecialFolderName(foldername));
        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }
        return folder;
    }

    /**
     *
     * @param foldername
     * @return
     * @throws MessagingException
     */
    private IMAPFolder openReadWriteFolder(String foldername) throws MessagingException {

        IMAPFolder folder = (IMAPFolder) getStore().getFolder(getSpecialFolderName(foldername));
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
        return folder;
    }

    /**
     *
     */
    private boolean ensureConnection() {
        if (storeInstance == null) {
            storeInstance = buildStoreConnection();
        } else if (!storeInstance.isConnected()) {
            storeInstance = buildStoreConnection();
        }
        if (smtpsession == null) {
            smtpsession = buildSmtpSession();
        }
        return storeInstance.isConnected();
    }

    /**
     *
     * @return
     */
    private Store getStore() {
        ensureConnection();
        return storeInstance;
    }

    /**
     *
     * @return
     */
    private Session buildSmtpSession() {

        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        final String username = this.settings.get("email");
        final String password = this.settings.get("password");

        // Get the default Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        return session;
    }

    /**
     *
     * @param message
     */
    private void smtpSendMail(MessageDTO message) {
        try {
            Message mail = mapDTOToMessageHtml(message, smtpsession);
            // Send message
            Transport.send(mail);

            System.out.println("Sent message successfully....");
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "Error sending Mail via Smtp protocol.", ex);
        }

    }

    /**
     *
     * @param message
     * @return
     */
    private MessageDTO sentSentDate(MessageDTO message) {
        try {
            //set the time
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:MM:SS");
            Date date = new Date();
            String sentDate = df.format(date);
            Date dd = (Date) df.parse(sentDate);
            message.setSentDate(date);
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, "Error parsing Sent Date", ex);
        }
        return message;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public String parseFileName(String fileName) {
        String[] array = fileName.split("/");
        int index = array.length - 1;
        return array[index];
    }

    /**
     *
     * @param foldername
     * @return
     */
    private String factoryIconForFolder(String foldername) {
        if (foldername.toLowerCase().contains("inbox")) {
            return "fa fa-inbox";
        }
        if (foldername.toLowerCase().contains("sent")) {
            return "fa fa-send";
        }
        if (foldername.toLowerCase().contains("spam")) {
            return "fa fa-bug";
        }
        if (foldername.toLowerCase().contains("bin") || foldername.toLowerCase().contains("trash")) {
            return "fa fa-trash-o";
        }
        return "fa fa-folder";
    }

    /**
     * Some IMAP Folders are accessible only under a special name like
     * "[Gmail]/Bin" for Gmail.
     *
     */
    private String getSpecialFolderName(String foldername) {

        // Gmail Special folders
        if (this.settings.get("imap").contains("gmail")) {
            if (EmailUtils.isAnyWordFromArrayInText(new String[]{"sent", "junk", "spam", "bin", "trash"}, foldername)) {
                return "[Gmail]/" + foldername;
            }
        }

        return foldername;
    }

    /**
     * Orders the list in a logical way: Inbox and Sent first, then all generic
     * folders and finally Junk and Bin.
     *
     * @param listoffolders
     * @return
     */
    private List<EmailFolderDto> orderList(List<EmailFolderDto> listoffolders) {
        List<EmailFolderDto> ordered = new ArrayList<EmailFolderDto>();
        int order = 0;

        ordered.add(findFolder("inbox", listoffolders));
        ordered.add(findFolder("sent", listoffolders));
        ordered.addAll(findFolderNotIn(new String[]{"inbox", "sent", "junk", "spam", "bin", "trash"}, listoffolders));
        ordered.add(findFolder("spam", listoffolders));
        ordered.add(findFolder("bin", listoffolders));

        return removeNullFolders(ordered);
    }

    /**
     * Find a particular folder which matches the given name
     *
     * @param name
     * @param listoffolders
     * @return
     */
    public EmailFolderDto findFolder(String name, List<EmailFolderDto> listoffolders) {
        for (EmailFolderDto item : listoffolders) {
            if (item.getName().toLowerCase().contains(name)) {
                return item;
            }
        }
        return EmailFolderDto.INVALID_FOLDER();
    }

    /**
     * Returns a list of Folders which do not match any of the terms in the
     * array
     *
     * @param exclude
     * @param listoffolders
     * @return
     */
    public List<EmailFolderDto> findFolderNotIn(String[] exclude, List<EmailFolderDto> listoffolders) {
        List<EmailFolderDto> filteredlist = new ArrayList<EmailFolderDto>();

        for (EmailFolderDto item : listoffolders) {
            String foldername = item.getName();
            boolean filter = false;
            // filter folders that belongs to the 'exclude' list
            for (String ex : exclude) {
                if (foldername.toLowerCase().contains(ex.toLowerCase())) {
                    filter = true;
                }
            }
            if (filter == false) {
                filteredlist.add(item);
            }
        }
        return filteredlist;
    }

    /**
     * Removes all invlid Folders from a list of EmailFolderDto
     *
     * @param ordered
     * @return
     */
    public List<EmailFolderDto> removeNullFolders(List<EmailFolderDto> ordered) {
        List<EmailFolderDto> newordered = new ArrayList<>();

        for (EmailFolderDto i : ordered) {
            if (!i.equals(EmailFolderDto.INVALID_FOLDER())) {
                newordered.add(i);
            }
        }
        return newordered;
    }

    /**
     *
     * @param uid
     * @param foldername
     * @return
     */
    @Override
    public List<EmailAttachment> downloadAttachments(Long uid, String foldername) {
        MessageDTO message = this.findOne(uid, foldername);
        return message.getAttachments();
    }

    /**
     *
     * @param foldername
     * @return
     */
    @Override
    public int getUnreadCount(String foldername) {
        int count = 0;

        try {
            IMAPFolder folder = openReadOnlyFolder(foldername);
            count = folder.getUnreadMessageCount();
        } catch (MessagingException ex) {
            String subfolder = this.getFolderFullName(foldername);
            if (foldername.equals(subfolder)) {
                logger.log(Level.SEVERE, "Error Getting Unread Mail count for folder " + foldername, ex);
            } else {
                //try again
                getUnreadCount(subfolder);

            }
        }
        return count;
    }

    /**
     *
     * @param ric
     * @param foldersnames
     * @return
     */
    public String getFolderFullName(String sourcefolder) {
        String fullname = sourcefolder;

        // get all available folders
        List<EmailFolderDto> folders = this.getAvailableFolders();

        // build array of strings
        List<String> foldersnames = new ArrayList<String>();
        for (EmailFolderDto f : folders) {
            foldersnames.add(f.getName());
        }

        // try to connect to the folder
        for (String parent : foldersnames) {
            IMAPFolder folder = null;
            try {
                folder = openReadOnlyFolder(parent + "/" + sourcefolder);
            } catch (MessagingException ex) {
                //do nothing
            }
            if (folder != null) {
                fullname = parent + "/" + sourcefolder;
                return fullname;
            }
        }

        // by default return the normal name
        return fullname;
    }

    /**
     *
     * @return
     */
    public List<String> getAvailableFolderNames() {
        List<String> foldersnames = new ArrayList<String>();
        //if the folder fails to open this might be a sub-folder
        List<EmailFolderDto> folders = this.getAvailableFolders();
        for (EmailFolderDto f : folders) {
            foldersnames.add(f.getName());
        }
        return foldersnames;
    }

    /**
     *
     * @param flags
     * @return
     */
    private List<Flag> convertFlagsToList(Flags flags) {
        List<Flag> list = new ArrayList<>();

        for (Flag item : flags.getSystemFlags()) {
            list.add(item);
        }
        return list;
    }

    /**
     *
     * @param item
     * @return
     */
    private MessageDTO setFlagsFromMessage(Flags flags, MessageDTO dto) {
        if (flags.contains(Flag.ANSWERED)) {
            dto.setAnswered(true);
        }
        if (flags.contains(Flag.DELETED)) {
            dto.setDeleted(true);
        }
        if (flags.contains(Flag.RECENT)) {
            dto.setRecent(true);
        }
        if (flags.contains(Flag.SEEN)) {
            dto.setSeen(true);
        }
        return dto;
    }

    /**
     *
     * @param foldername
     * @param messages
     * @param i
     * @return
     */
    @Override
    public List<MessageDTO> refreshEmails(String foldername, List<MessageDTO> messages, int offset) {

        // Step 0: Get the email with Highest UID, i.e. the most recent email in the list
        Collections.sort(messages, MessageDTO.sortDescByUid);
        long latestUid = messages.get(0).getUid();

        // Step 1: delete messages with invalid UID
        List<MessageDTO> removeList = new ArrayList<MessageDTO>();
        for (MessageDTO item : messages) {
            if (isInvalidMessage(item)) {
                removeList.add(item);
            }
        }
        messages.removeAll(removeList);

        // Step 2: get only the newest emails (after latest UID)
        List<MessageDTO> newmessages = this.getEmails(foldername, offset - messages.size(), latestUid, 0);
        messages.addAll(newmessages);

        return messages;
    }
}
