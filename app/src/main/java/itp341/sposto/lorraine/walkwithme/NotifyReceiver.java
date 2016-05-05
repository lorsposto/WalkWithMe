package itp341.sposto.lorraine.walkwithme;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by LorraineSposto on 5/3/16.
 */
public class NotifyReceiver extends BroadcastReceiver {

    public static final String TAG = NotifyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive sending sms");
        String myPhoneNumber = intent.getStringExtra(Keys.KEY_MY_NUMBER);
        ArrayList<String> numbers = intent.getStringArrayListExtra(Keys.KEY_PHONE_NUMBERS);

        sendSMS(myPhoneNumber);
        for (String number : numbers) {
            sendSMS(number);
        }
    }

    private void sendSMS(String phoneNumber) {
        try {
            phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
            Log.d(TAG, "Sending sms to " + phoneNumber);
            SmsManager smsManager = SmsManager.getDefault();
            // TODO
            smsManager.sendTextMessage(phoneNumber, null, "Test message to notify", null, null);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
