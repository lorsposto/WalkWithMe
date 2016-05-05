package itp341.sposto.lorraine.walkwithme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;

/**
 * Created by LorraineSposto on 4/25/16.
 */
public class AuthenticateActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authenticate_digits);
        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // TODO: associate the session userID with your user model
                Intent data = new Intent();
                data.putExtra(Keys.SharedPref.KEY_DIGITS_ID, session.getId());
                String pn = phoneNumber != null ? phoneNumber : session.getPhoneNumber();
                data.putExtra(Keys.SharedPref.KEY_DIGITS_PHONE_NUMBER, pn);
                Log.d(AuthenticateActivity.class.getSimpleName(), "Auth success " + pn);
                setResult(RESULT_OK, data);
                finish();

            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
