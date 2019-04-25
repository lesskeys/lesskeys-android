package at.ac.uibk.keylessapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import at.ac.uibk.keylessapp.Utils.SSLUtils;
import at.ac.uibk.keylessapp.Utils.Utils;

/**
 * Activity for the LogIn screen.
 *
 * @author Lukas Dötlinger
 */
public class LogInActivity extends AppCompatActivity {

    private String device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // initial step to ignore untrusted certificates
        SSLUtils.disableSSLCertificateChecking();

        // check for permission to read phone state
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        } else {

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            final String deviceId = telephonyManager.getDeviceId();
            device = deviceId;

            // get the current Firebase token
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String firebaseToken = instanceIdResult.getToken();
                    SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
                    ;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("savedFirebaseToken", firebaseToken);
                    editor.commit();
                }
            });

            autoLogIn(deviceId);

            final EditText input_username = (EditText) findViewById(R.id.login_username);
            final EditText input_password = (EditText) findViewById(R.id.login_password);

            Button login_button = (Button) findViewById(R.id.login_button);

            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String username = input_username.getText().toString();
                        String password = input_password.getText().toString();

                        new LogInRequest(username, password, deviceId).execute();
                        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
                        ;
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("savedUsername", username);
                        editor.commit();
                    } catch (Exception e) {
                        Log.e("LogInActivity", e.getMessage(), e);
                    }
                }
            });

            final TextView versionNumber = (TextView) findViewById(R.id.version_number);
            versionNumber.setText("v " + BuildConfig.VERSION_NAME);
        }
    }

    /**
     * Method to automatically login the user, by reading old token from SharedPref and sending them
     * to the server using the background-task implemented in the abstract class AutoLogInRequest.
     * @param deviceId The unique id of a phone.
     */
    private void autoLogIn(String deviceId) {
        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
        String savedToken = sharedPref.getString("savedToken", "-error-");
        String savedDate = sharedPref.getString("savedDate", "-error-");
        String savedFirebaseToken = sharedPref.getString("savedFirebaseToken", "-error-");
        if (!(savedToken.equals("-error-") || savedDate.equals("-error-"))) {
            new AutoLogInRequest(savedToken, savedDate, deviceId, savedFirebaseToken).execute();
        }
    }

    /**
     * Method to safe the data from the servers response to the SharedPref.
     * @param response The response from the server.
     */
    private void safeResponse(Map<String, String> response) {
        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("savedToken", response.get("token"));
        editor.putString("savedDate", response.get("date"));
        editor.putString("savedSession", response.get("session"));
        editor.putString("savedRole",response.get("role"));
        String isLockOnline = response.get("lockOnline") == null ? "true" : response.get("lockOnline");
        if (isLockOnline.equals("true")) {
            editor.putString("toSendSession", response.get("session"));
        }
        editor.putString("savedDeviceId", this.device);
        editor.commit();
    }

    /**
     * Method to open the MainActivity if the login was successful.
     */
    private void proceedAfterLogin() {
        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
        String role= sharedPref.getString("savedRole","error");
        if(role.equals("Visitor")){
            Intent intent = new Intent(this, MainActivityVisitor.class);
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }

    /**
     * Private class used as a background task to send a login-request to the server.
     */
    private class LogInRequest extends AsyncTask<Void, Void, String> {

        private Map<String, String> request = new HashMap<>();
        private String answer;

        private LogInRequest(String username, String password, String deviceId) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
            request.put("username", username);
            request.put("password", password);
            request.put("deviceId", deviceId);
            request.put("firebaseToken", sharedPref.getString("savedFirebaseToken", "-error-"));
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url = getString(R.string.backend_ip)+"/login";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
                safeResponse(response);
                return answer = response.get("answer");
            } catch (ResourceAccessException e) {
                return answer = "ResourceAccessException";
            } catch (Exception e) {
                Log.e("LogInActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            switch (answer) {
                case "Success":
                    proceedAfterLogin();
                    break;
                case "ResourceAccessException":
                    Toast.makeText(getApplicationContext(), "Server not available!", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Private class used as a background task to send a auto-login-request to the server.
     */
    private class AutoLogInRequest extends AsyncTask<Void, Void, String> {

        private Map<String, String> request = new HashMap<>();
        private String answer;

        private AutoLogInRequest(String token, String date, String deviceId, String firebase) {
            request.put("token", token);
            request.put("date", date);
            request.put("deviceId", deviceId);
            request.put("firebaseToken", firebase);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url = getString(R.string.backend_ip)+"/autologin";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
                safeResponse(response);
                return answer = response.get("answer");
            } catch (ResourceAccessException e) {
                return answer = "ResourceAccessException";
            } catch (Exception e) {
                Log.e("LogInActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (answer.equals("Success")) {
                proceedAfterLogin();
            } else if (answer.equals("ResourceAccessException")) {
                Toast.makeText(getApplicationContext(), "Server not available!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, therefore app proceeds
                } else {
                    // permission was not granted, therefore app closes
                    finishAndRemoveTask();
                }
                return;
            }

        }
    }
}