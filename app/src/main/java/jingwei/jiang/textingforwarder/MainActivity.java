package jingwei.jiang.textingforwarder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SMSHandler {
    private static final String CHANNEL_ID = "Background";
    private final String EMPTY_STRING = "";
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    // TextViews
    private TextView saveButton;
    private TextView emailText;
    private TextView protocolText;
    private TextView hostText;
    private TextView portText;
    private TextView passwordText;
    private TextView recipientText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        createNotification();
        requestSmsPermission();
        registerSmsListener();
    }

    @Override
    public void handleSMS(String sender, String message) {
        String protocol = preferences.getString(getString(R.string.email_sender_protocol), EMPTY_STRING);
        String host = preferences.getString(getString(R.string.email_sender_host), EMPTY_STRING);
        String port = preferences.getString(getString(R.string.email_sender_port), EMPTY_STRING);
        String userName = preferences.getString(getString(R.string.email_sender_userName), EMPTY_STRING);
        String password = preferences.getString(getString(R.string.email_sender_password), EMPTY_STRING);
        String recipient = preferences.getString(getString(R.string.recipient_email), EMPTY_STRING);

        EmailSender emailSender = new EmailSender(protocol, host, port, userName, password);
        emailSender.send(userName, recipient, sender, message);
    }

    private void registerSmsListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        /* filter.setPriority(999); This is optional. */
        SMSReceiver receiver = new SMSReceiver(this);
        registerReceiver(receiver, filter);
    }

    private void requestSmsPermission() {
        String permission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if ( grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }

    private void init() {
        saveButton = findViewById(R.id.save_button);
        emailText = findViewById(R.id.email_text);
        protocolText = findViewById(R.id.protocol_text);
        hostText = findViewById(R.id.host_text);
        portText = findViewById(R.id.port_text);
        passwordText = findViewById(R.id.password_text);
        recipientText = findViewById(R.id.recipient_text);

        preferences = getSharedPreferences(getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
        preferencesEditor = preferences.edit();

        emailText.setText(preferences.getString(getString(R.string.email_sender_userName), EMPTY_STRING));
        protocolText.setText(preferences.getString(getString(R.string.email_sender_protocol), EMPTY_STRING));
        hostText.setText(preferences.getString(getString(R.string.email_sender_host), EMPTY_STRING));
        portText.setText(preferences.getString(getString(R.string.email_sender_port), EMPTY_STRING));
        passwordText.setText(preferences.getString(getString(R.string.email_sender_password), EMPTY_STRING));
        recipientText.setText(preferences.getString(getString(R.string.recipient_email), EMPTY_STRING));

        saveButton.setOnClickListener(view -> {
            String newEmail = emailText.getText().toString();
            String newProtocol = protocolText.getText().toString();
            String newHost = hostText.getText().toString();
            String newPort = portText.getText().toString();
            String newPassword = passwordText.getText().toString();
            String newRecipient = recipientText.getText().toString();

            preferencesEditor.putString(getString(R.string.email_sender_userName), newEmail);
            preferencesEditor.putString(getString(R.string.email_sender_protocol), newProtocol);
            preferencesEditor.putString(getString(R.string.email_sender_host), newHost);
            preferencesEditor.putString(getString(R.string.email_sender_port), newPort);
            preferencesEditor.putString(getString(R.string.email_sender_password), newPassword);
            preferencesEditor.putString(getString(R.string.recipient_email), newRecipient);
            preferencesEditor.commit();
        });
    }

    private void createNotification() {
        String title = getString(R.string.notification_title);
        String content = getString(R.string.notification_content);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1, builder.build());
        }
    }
}