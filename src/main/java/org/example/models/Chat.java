package org.example.models;

public class Chat {
    private String id;
    private Integer chatter1;
    private Integer chatter2;
    private Boolean isRemoved;
    private Message lastMessage;
    private String chatterLocalNickname;
    private Integer notChecked;

    public Chat(String id, Integer chatter1, Integer chatter2, Boolean isRemoved, Integer notChecked) {
        this.id = id;
        this.chatter1 = chatter1;
        this.chatter2 = chatter2;
        this.isRemoved = isRemoved;
        this.notChecked = notChecked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getChatter1() {
        return chatter1;
    }

    public void setChatter1(Integer chatter1) {
        this.chatter1 = chatter1;
    }

    public Integer getChatter2() {
        return chatter2;
    }

    public void setChatter2(Integer chatter2) {
        this.chatter2 = chatter2;
    }

    public Boolean getIsRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(Boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChatterLocalNickname() {
        return chatterLocalNickname;
    }

    public void setChatterLocalNickname(String chatterLocalNickname) {
        this.chatterLocalNickname = chatterLocalNickname;
    }

    public Integer getNotChecked() {
        return notChecked;
    }

    public void setNotChecked(Integer notChecked) {
        this.notChecked = notChecked;
    }
}
