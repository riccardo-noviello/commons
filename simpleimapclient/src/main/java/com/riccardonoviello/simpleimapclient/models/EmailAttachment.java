package com.riccardonoviello.simpleimapclient.models;

import java.io.InputStream;

/**
 *
 * @author novier
 */
public class EmailAttachment {

    private String mimeType;
    private String fileName;
    private String encoding;
    private InputStream inputStream;
    private boolean inline = false;
    private String inlineId;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public String getInlineId() {
        return inlineId;
    }

    public void setInlineId(String inlineId) {
        this.inlineId = inlineId;
    }

}
