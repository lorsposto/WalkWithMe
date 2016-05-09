package itp341.sposto.lorraine.walkwithme;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by LorraineSposto on 5/3/16.
 */
public class NotifyReceiver extends BroadcastReceiver {

    public static final String TAG = NotifyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        int code = intent.getIntExtra(Keys.INTENT_CODE, -1);
        String message = intent.getStringExtra(Keys.KEY_MESSAGE);

        Log.d(TAG, "onReceive broadcast receiver code: " + code);
        if (code == Keys.NOTIF_CODE) {
            doNotification(context, message);
        } else if (code == Keys.SMS_CODE) {
            ArrayList<String> numbers = intent.getStringArrayListExtra(Keys.KEY_PHONE_NUMBERS);

            if (numbers != null) {
                for (String number : numbers) {
                    sendSMS(number, message);
                }
            }
        }
    }

    private void doNotification(Context context, String message) {
        Log.d(TAG, "Launching notification, message: " + message);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        Intent i = new Intent(context, MapPaneActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i, 0);
        Notification.Action a = new Notification.Action.Builder(R.mipmap.ic_launcher, "Call", pi).build();

        Notification n = new Notification.Builder(context).
                setContentTitle(context.getString(R.string.notification_title)).
                setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .addAction(a).build();
        notificationManagerCompat.notify(394, n);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
            Log.d(TAG, "Sending sms to " + phoneNumber);
            Log.d(TAG, "Message: " + message);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
