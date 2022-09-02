package jingwei.jiang.textingforwarder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SMSHandler {
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

    private void init() {
        saveButton = (TextView) findViewById(R.id.save_button);
        emailText = (TextView) findViewById(R.id.email_text);
        protocolText = (TextView) findViewById(R.id.protocol_text);
        hostText = (TextView) findViewById(R.id.host_text);
        portText = (TextView) findViewById(R.id.port_text);
        passwordText = (TextView) findViewById(R.id.password_text);
        recipientText = (TextView) findViewById(R.id.recipient_text);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
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
}