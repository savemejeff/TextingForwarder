package jingwei.jiang.textingforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.HashMap;
import java.util.Map;

public class SMSReceiver extends BroadcastReceiver {
    private final SMSHandler handler;

    public SMSReceiver(SMSHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        /* Retrieve the sms message chunks from the intent */
        SmsMessage[] rawSmsChunks;
        try {
            rawSmsChunks = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        } catch (NullPointerException ignored) { return; }

        /* Gather all sms chunks for each sender separately */
        Map<String, StringBuilder> sendersMap = new HashMap<>();
        for (SmsMessage rawSmsChunk : rawSmsChunks) {
            if (rawSmsChunk != null) {
                String sender = rawSmsChunk.getDisplayOriginatingAddress();
                String smsChunk = rawSmsChunk.getDisplayMessageBody();
                StringBuilder smsBuilder;
                if ( ! sendersMap.containsKey(sender) ) {
                    /* For each new sender create a separate StringBuilder */
                    smsBuilder = new StringBuilder();
                    sendersMap.put(sender, smsBuilder);
                } else {
                    /* Sender already in map. Retrieve the StringBuilder */
                    smsBuilder = sendersMap.get(sender);
                }
                /* Add the sms chunk to the string builder */
                smsBuilder.append(smsChunk);
            }
        }

        /* Loop over every sms thread and concatenate the sms chunks to one piece */
        for ( Map.Entry<String, StringBuilder> smsThread : sendersMap.entrySet() ) {
            String sender  = smsThread.getKey();
            StringBuilder smsBuilder = smsThread.getValue();
            String message = smsBuilder.toString();
            handler.handleSMS(sender, message);
        }
    }
}
