package it.ucdm.leisure.dinnerplan.dto;

public class ChatMessageDTO {
    private Long id;
    private String sender;
    private String content;
    private String time;

    public ChatMessageDTO(Long id, String sender, String content, String time) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
