package at.ac.uibk.keylessapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import at.ac.uibk.keylessapp.Models.SystemUser;

/**
 * Main activity class, which contains all the buttons to navigate to the different activities.
 * After a successful login, this activity is being displayed.
 *
 * @author Davide De Sclavis
 */
public class MainActivity extends AppCompatActivity {

    private SystemUser user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("keyless-pref",Context.MODE_PRIVATE);
        String username = sharedPref.getString("savedUsername","-error-");
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(username);

        /*
         Check if NFC is enabled
         */
        Context context;
        context=getApplicationContext();
        final TextView nfc_an= (TextView) findViewById(R.id.textNFCon);
        final TextView nfc_aus= (TextView) findViewById(R.id.textNFCoff);
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        final NfcAdapter adapter = manager.getDefaultAdapter();
        if (!(adapter != null && adapter.isEnabled())) {
            nfc_aus.setVisibility(View.VISIBLE);
            nfc_an.setVisibility(View.INVISIBLE);
        } else {
            nfc_an.setVisibility(View.VISIBLE);
            nfc_aus.setVisibility(View.INVISIBLE);
        }

        configuringButtons();

        final  SwipeRefreshLayout mSwipeRefreshlayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mSwipeRefreshlayout.setRefreshing(true);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshlayout.setRefreshing(false);
                    if (!(adapter != null && adapter.isEnabled())) {
                        nfc_aus.setVisibility(View.VISIBLE);
                        nfc_an.setVisibility(View.INVISIBLE);
                    }else{
                        nfc_an.setVisibility(View.VISIBLE);
                        nfc_aus.setVisibility(View.INVISIBLE);
                    }
                }
            },700);
        }});
    }

    /**
     * Method to configure all buttons in MainActivity.
     */
    public void configuringButtons(){
        Button nfc_act_btn = (Button) findViewById(R.id.button_add);
        Button key_btn= (Button) findViewById(R.id.button_keys);
        Button accessprotocol_btn= (Button) findViewById(R.id.button_accessprotocol);
        Button usersettings_btn= (Button) findViewById(R.id.button_usersettings);
        Button subUser_btn = (Button) findViewById(R.id.button_subUser);
        Button remoteMessage_btn = (Button) findViewById(R.id.button_remoteMessages);
        Button foundKey_btn= (Button) findViewById(R.id.buttonFoundKey);
        Button logout_btn= (Button) findViewById(R.id.buttonlogout);
        Button remote_doors_btn= (Button) findViewById(R.id.button_remote_doors);

        //nfcActivityButton
        nfc_act_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NfcActivity.class));
            }
        });

        //foundKeyActivity
        foundKey_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FoundKeyActivity.class));
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSharedPreferences("keyless-pref");
                startActivity(new Intent(MainActivity.this, LogInActivity.class));
            }
        });

        //keysActivityButton
        key_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, KeyActivity.class));
            }
        });


        //accesprotocollActivityButton
        accessprotocol_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                startActivity(new Intent(MainActivity.this, AccessProtocolActivity.class));
            }
        });

        //userActivityButton
        usersettings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,UserActivity.class));
            }
        });

        //subUserActivityButton
        subUser_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SubUserActivity.class));
            }
        });

        //RemoteDoorActivityButton
        remote_doors_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RemoteDoorActivity.class));
            }
        });

        remoteMessage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RemoteMessageActivity.class));
            }
        });
    }

    private void setCurrentSystemUser(SystemUser user){
        this.user=user;
    }

    /**
     * Private class used as a background-task to request the data from the currently logged in user
     * from the server.
     */
    private class UserDataRequest extends AsyncTask<Void,Void,String> {

        private SystemUser answer = new SystemUser();
        Map<String, String> request= new HashMap<>();

        private UserDataRequest() {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url= getString(R.string.backend_ip)+"/user/current";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                answer = restTemplate.postForObject(url,request,SystemUser.class);
                setCurrentSystemUser(answer);
                return answer.getEmail();
            }catch (Exception e){
                Log.e("UserActivity",e.getMessage(),e);
            }
            return null;
        }
    }
}
