package jingwei.jiang.textingforwarder;

public interface SMSHandler {
    void handleSMS(String sender, String message);
}
