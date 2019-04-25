package at.ac.uibk.keylessapp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.ac.uibk.keylessapp.Models.SystemKeyPermission;
import at.ac.uibk.keylessapp.R;
import at.ac.uibk.keylessapp.Utils.Utils;

public class RemoteMessageService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        String click_action= remoteMessage.getNotification().getClickAction();
        Intent intent= new Intent(click_action);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
        Set<String> messages = sharedPref.getStringSet("messages", new HashSet<String>());
        messages.add(remoteMessage.getNotification().getBody());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("messages", messages);
        editor.commit();

        int icon = R.mipmap.ic_launcher;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setContentIntent(pendingIntent)
                .setSmallIcon(icon)
                .build();

        notificationManager.notify(0, notification);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This is also called if token is
     * initially created.
     * The new token is sent to the backend server.
     */
    @Override
    public void onNewToken(String token) {
        Map<String, String> request = new HashMap<>();
        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
        request.put("session", sharedPref.getString("savedSession", "-error-"));
        request.put("username", sharedPref.getString("savedUsername", "-error-"));
        request.put("firebaseToken", token);

        try {
            final String url= "https://keyless.arctis.at:58080/user/firebase-token/update";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            restTemplate.put(url, request);
        } catch(Exception e) {
            Log.e("KeyActivity", e.getMessage(),e);
        }
    }
}
