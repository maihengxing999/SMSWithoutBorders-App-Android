package com.example.sw0b_001;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.example.sw0b_001.Helpers.CustomHelpers;
import com.example.sw0b_001.Helpers.Datastore;
import com.example.sw0b_001.Helpers.SecurityLayer;
import com.example.sw0b_001.Providers.Emails.EmailMessage;
import com.example.sw0b_001.Providers.Emails.EmailMessageDao;
import com.example.sw0b_001.Providers.Emails.EmailThreads;
import com.example.sw0b_001.Providers.Emails.EmailThreadsDao;
import com.example.sw0b_001.Providers.Platforms.PlatformDao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class EmailComposeActivity extends AppCompatActivity {

    String SMS_SENT = "SENT";
    String SMS_DELIVERED = "DELIVERED";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    SecurityLayer securityLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_compose);

        Toolbar composeToolbar = (Toolbar) findViewById(R.id.compose_toolbar);
        setSupportActionBar(composeToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        try {
            securityLayer = new SecurityLayer();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView emailTo = findViewById(R.id.email_to);
        TextView emailSubject = findViewById(R.id.email_subject);
        if(getIntent().hasExtra("recipient") ) {
            emailTo.setText(getIntent().getStringExtra("recipient"));
        }
        if(getIntent().hasExtra("subject") ) {
            emailSubject.setText(getIntent().getStringExtra("subject"));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.email_compose_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        EditText to = findViewById(R.id.email_to);
        EditText subject = findViewById(R.id.email_subject);
        EditText body = findViewById(R.id.email_body);
        switch (item.getItemId()) {
            case R.id.discard:
                startActivity(new Intent(this, EmailThreadsActivity.class));
                to.setText("");
                subject.setText("");
                body.setText("");
                finish();
                return true;

            case R.id.action_send:
                if(to.getText().toString().isEmpty()) {
                    to.setError("Recipient cannot be empty!");
                    return false;
                }
                if(subject.getText().toString().isEmpty()) {
                    subject.setError("Subject should not be empty!");
                    return false;
                }
                if(body.getText().toString().isEmpty()) {
                    body.setError("Body should not be empty!");
                    return false;
                }

                finish();
                String default_phonenumber = "12345";
                final long[] threadId = {getIntent().getLongExtra("thread_id", -1)};
                if(threadId[0] == -1) {
                    Thread storeEmailThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            EmailThreads emailThread = new EmailThreads()
                                    .setRecipient(to.getText().toString())
                                    .setSubject(subject.getText().toString())
                                    .setPlatformId(getIntent().getLongExtra("platform_id", -1));
                            Datastore emailStoreDb = Room.databaseBuilder(getApplicationContext(),
                                    Datastore.class, Datastore.DBName).build();

                            EmailThreadsDao platformsDao = emailStoreDb.emailThreadDao();
                            threadId[0] = platformsDao.insert(emailThread);
                        }
                    });

                    storeEmailThread.start();
                    try {
                        storeEmailThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Thread storeEmailMessage = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        EmailMessage emailMessage = new EmailMessage()
                                .setBody(body.getText().toString())
                                .setDatetime(CustomHelpers.getDateTime())
                                .setThreadId(threadId[0])
                                .setRecipient(to.getText().toString())
                                .setSubject(subject.getText().toString())
                                .setStatus("pending");
                        Datastore emailStoreDb = Room.databaseBuilder(getApplicationContext(),
                                Datastore.class, Datastore.DBName).build();

                        EmailMessageDao platformsDao = emailStoreDb.emailDao();
                        platformsDao.insertAll(emailMessage);
                    }
                });
                storeEmailMessage.start();
                try {
                    storeEmailMessage.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    private void sendMessage(String phonenumber, String text) {
        Toast.makeText(getBaseContext(), "SMS sending...", Toast.LENGTH_LONG).show();
        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("SENT"), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("DELIVERED"), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_DELIVERED));


        if(text.isEmpty()) {
            Toast.makeText(this, "Text Cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // TODO: work out how the IV gets encrypted before sending
            if(checkPermission(Manifest.permission.SEND_SMS)) {
                String IV = new String(securityLayer.getIV(), "UTF-8");
                String transmissionText = IV + ":" + text;
                byte[] encryptedText = securityLayer.encrypt_AES(transmissionText);

                System.out.println("Transmission String: " + transmissionText);
                System.out.println("[+] Decrypted: " + new String(securityLayer.decrypt_AES(encryptedText), "UTF-8"));


                String strEncryptedText = Base64.encodeToString(encryptedText, Base64.URL_SAFE);
                System.out.println("Transmission message: " + strEncryptedText);

                //TODO: Research what to do in case of a double sim phone
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phonenumber, null, strEncryptedText, sentPendingIntent, deliveredPendingIntent);
                Toast.makeText(this, "Sending SMS....", Toast.LENGTH_LONG).show();
            }
            else {
                requestSMSpermission();
            }
        } catch (BadPaddingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Internal Error while encrypting...", Toast.LENGTH_LONG).show();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            Toast.makeText(this, "Internal Error while encrypting...", Toast.LENGTH_LONG).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

    public void requestSMSpermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0) {
                } else {
                }
            }
        }

    }

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);

        return (check == PackageManager.PERMISSION_GRANTED);
    }

}
