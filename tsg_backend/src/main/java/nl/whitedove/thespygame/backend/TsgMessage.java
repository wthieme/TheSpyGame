package nl.whitedove.thespygame.backend;

import org.joda.time.DateTime;

public class TsgMessage {
    private String Title;
    private DateTime MessageDt;
    private String MessageTxt;

    public TsgMessage(String title, String txt) {
        this.setTitle(title);
        this.setMessageTxt(txt);
        this.setMessageDt(DateTime.now());
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public DateTime getMessageDt() {
        return MessageDt;
    }

    public void setMessageDt(DateTime messageDt) {
        MessageDt = messageDt;
    }

    public String getMessageTxt() {
        return MessageTxt;
    }

    public void setMessageTxt(String messageTxt) {
        MessageTxt = messageTxt;
    }
}
