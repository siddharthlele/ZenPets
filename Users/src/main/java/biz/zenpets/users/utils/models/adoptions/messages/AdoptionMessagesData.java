package biz.zenpets.users.utils.models.adoptions.messages;

public class AdoptionMessagesData {

    private String messageID = null;
    private String adoptionID = null;
    private String userID = null;
    private boolean userPoster = false;
    private String messageText = null;
    private String messageTimeStamp = null;

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getAdoptionID() {
        return adoptionID;
    }

    public void setAdoptionID(String adoptionID) {
        this.adoptionID = adoptionID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isUserPoster() {
        return userPoster;
    }

    public void setUserPoster(boolean userPoster) {
        this.userPoster = userPoster;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageTimeStamp() {
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(String messageTimeStamp) {
        this.messageTimeStamp = messageTimeStamp;
    }
}