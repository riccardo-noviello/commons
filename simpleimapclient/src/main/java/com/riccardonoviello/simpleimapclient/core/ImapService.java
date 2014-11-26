package com.riccardonoviello.simpleimapclient.core;

import com.riccardonoviello.simpleimapclient.models.EmailFolderDto;
import com.riccardonoviello.simpleimapclient.models.MessageDTO;
import com.riccardonoviello.simpleimapclient.models.EmailAttachment;
import java.util.List;
import javax.mail.Message;
import javax.mail.search.SearchTerm;

/**
 *
 * @author novier
 */
public interface ImapService {

    
    /**
     * Set Up the connection
     *
     * @param imap
     * @param email
     * @param password
     * @return true if connected successfully
     */
    public boolean setup(String imap, String email, String password, String smtp);

    
    /**
     * List all available folders and subfolders
     *
     * @return
     */
    public List<EmailFolderDto> getAvailableFolders();

    
    /**
     * Refresh the current List of emails in a given folder for a given offset.
     * For example if we have a list of 10 emails and call "refresh" we will get
     * the latest emails and remove any invalid email (emails that have been
     * deleted or emails that have been moved to another folder).
     *
     * @param foldername
     * @param messages
     * @param offset
     * @return
     */
    public List<MessageDTO> refreshEmails(String foldername, List<MessageDTO> messages, int offset);

    
    /**
     * Get a number of emails. For example you can either get the latest 15
     * emails setting (number=15, begin=0 and offset=0).
     * Or you can also get the 15 previous emails (number=15 and begin=0, offset=15)
     *
     * @param foldername
     * @param number
     * @param begin
     * @param offset
     * @return
     */
    public List<MessageDTO> getEmails(String foldername, int number, long begin, int offset);

    
    /**
     * Move a message from its original folder to a given destination folder
     * 
     * @param message
     * @param destFolder 
     */
    public void moveFolder(MessageDTO message, String destFolder);

    
      /**
     * Move a message to the Bin folder. You don;t need to specify the name of the Bin folder, the application find it automatically.
     * 
     * @param message
     */
    public void moveToBin(MessageDTO message);
    
    
    /**
     * Updates the Message's Header (for the moment there is only one custom header which is "linkCaseId"
     *   
     * @param message 
     */
    
    public void updateMessageLinkToCase(MessageDTO message);
    
    
    /**
     * Updates the Message's Flags, like SEEN, ANSWERED, and so on...
     *   
     * @param message 
     */
    public void updateMessageFlag(MessageDTO message);

    
    /**
     * Delete a Message permanently
     * 
     * @param message 
     */
    public void deleteMessage(MessageDTO message);

    
    /**
     * Send a Mail via SMTP and place it into the Sent Folder. #### NOT IMPLEMENTED ###
     * 
     * @param message 
     */
    public void sendMail(MessageDTO message);
    
    
    /**
     * Finds a Message by UUID in a given folder.
     * 
     * @param uuid
     * @param foldername
     * @return 
     */
    public MessageDTO findOne(Long uuid, String foldername);

    
    /**
     * Search for one message given the Search Terms
     * 
     * @param term
     * @param folder
     * @return 
     */
    public MessageDTO searchOne(SearchTerm term, String folder);
    
    
    /**
     * Search for messages given the Search Terms
     * 
     * @param term
     * @param folder
     * @return 
     */
    public List<MessageDTO> search(SearchTerm term, String folder);
    
    /**
     * Download a message attachment
     * @param uid   the message unique id
     * @param foldername
     * @return 
     */
    public List<EmailAttachment> downloadAttachments(Long uid, String foldername);
    
    /**
     * 
     * @param foldername
     * @return 
     */
    public int getUnreadCount(String foldername);
    
    
    public Message prepareMessageToSend(MessageDTO message);
}
