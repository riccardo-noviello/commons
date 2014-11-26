package com.riccardonoviello.simpleimapclient.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author novier
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private long uid;
    private MailAddress[] allRecipients;
    private MailAddress[] ccs;
    private MailAddress[] from;
    private String body;
    private Date sentDate;
    private Date receivedDate;
    private String subject;
    private String fileName;
    private String folderName;
    private boolean hasattachment;
    private boolean seen = false;
    private boolean answered = false;
    private boolean deleted = false;
    private boolean recent = false;
    private Long linkCaseId;
        
    private boolean hide = false;
    private List<EmailAttachment> attachments;
    private List<String> fileNames;
    
    private Timestamp time;

    private void init() {
        attachments = new ArrayList<>();
        fileNames = new ArrayList<>();
        body = "";
        allRecipients = new MailAddress[0];
        from = new MailAddress[0];
        ccs = new MailAddress[0];
    }

    public MessageDTO() {
        init();
    }

    public MessageDTO(Long id, String folder) {
        this.uid = id;
        this.folderName = folder;
        init();
    }

    public static Comparator<MessageDTO> sortDescByUid = new Comparator<MessageDTO>() {
        public int compare(MessageDTO m1, MessageDTO m2) {
            return (m1.getUid() < m2.getUid()) ? 1 : -1;
        }
    };

    public String getBody() {
        return body;
    }

       public void setRecipients(MailAddress[] allRecipients) {
        this.allRecipients = allRecipients;
    }

    public void setFrom(MailAddress[] from) {
        this.from = from;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setFileNames(List<String> names) {
        this.fileNames = names;
        checkHasAttachment();
        composeFilename();
    }

    private void composeFilename() {
        StringBuffer buf = new StringBuffer();
        for (String name : fileNames) {
            if (name != null) {
                buf.append(name + " ");
            }
        }
        this.setFileName(buf.toString());
    }

    private void checkHasAttachment() {
        if (fileNames.size() > 0) {
            this.hasattachment = true;
        } else {
            this.hasattachment = false;
        }

    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public MailAddress[] getAllRecipients() {
        return allRecipients;
    }

    public MailAddress[] getFrom() {
        return from;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setFileName(String name) {
        fileName = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Date getReceivedDate() {
        return this.receivedDate;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setBody(String parsedMessageBody) {
        this.body = parsedMessageBody;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public boolean isHasattachment() {
        return hasattachment;
    }

    public void setHasattachment(boolean hasattachment) {
        this.hasattachment = hasattachment;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<EmailAttachment> list) {
        this.attachments = list;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean val) {
        this.answered = val;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean val) {
        this.deleted = val;
    }
    
     public boolean isRecent() {
        return recent;
    }

    public void setRecent(boolean val) {
        this.recent = val;
    }
    
      public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean value) {
        this.seen = value;
    }
    
    public MailAddress[] getCcs() {
        return ccs;
    }

    public void setCcs(MailAddress[] ccs) {
        this.ccs = ccs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (this.uid ^ (this.uid >>> 32));
        hash = 61 * hash + Objects.hashCode(this.body);
        hash = 61 * hash + Objects.hashCode(this.sentDate);
        hash = 61 * hash + Objects.hashCode(this.receivedDate);
        hash = 61 * hash + Objects.hashCode(this.subject);
        hash = 61 * hash + Objects.hashCode(this.fileName);
        hash = 61 * hash + Objects.hashCode(this.folderName);
        hash = 61 * hash + (this.hasattachment ? 1 : 0);
        hash = 61 * hash + (this.seen ? 1 : 0);
        hash = 61 * hash + (this.answered ? 1 : 0);
        hash = 61 * hash + (this.deleted ? 1 : 0);
        hash = 61 * hash + (this.hide ? 1 : 0);
        hash = 61 * hash + Objects.hashCode(this.fileNames);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessageDTO other = (MessageDTO) obj;
        if (this.uid != other.uid) {
            return false;
        }
        if (!Objects.equals(this.body, other.body)) {
            return false;
        }
        if (!Objects.equals(this.sentDate, other.sentDate)) {
            return false;
        }
        if (!Objects.equals(this.receivedDate, other.receivedDate)) {
            return false;
        }
        if (!Objects.equals(this.subject, other.subject)) {
            return false;
        }
        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        if (!Objects.equals(this.folderName, other.folderName)) {
            return false;
        }
        if (this.hasattachment != other.hasattachment) {
            return false;
        }
        if (this.seen != other.seen) {
            return false;
        }
        if (this.answered != other.answered) {
            return false;
        }
        if (this.deleted != other.deleted) {
            return false;
        }
        if (this.hide != other.hide) {
            return false;
        }
        if (!Objects.equals(this.fileNames, other.fileNames)) {
            return false;
        }
        return true;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Long getLinkCaseId() {
        return linkCaseId;
    }

    public void setLinkCaseId(Long linkCaseId) {
        this.linkCaseId = linkCaseId;
    }
    
    
}
