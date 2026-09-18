package com.sameneed.model;

import java.time.LocalDateTime;

public class ChatMessage {

    private int messageId;
    private int requestId;
    private String senderAlias;
    private String content;
    private LocalDateTime sentAt;

    public ChatMessage() {}

    public ChatMessage(int requestId, String senderAlias, String content) {
        this.requestId = requestId;
        this.senderAlias = senderAlias;
        this.content = content;
    }

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public String getSenderAlias() { return senderAlias; }
    public void setSenderAlias(String senderAlias) { this.senderAlias = senderAlias; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
