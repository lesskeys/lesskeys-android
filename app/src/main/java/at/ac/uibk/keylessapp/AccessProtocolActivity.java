package at.ac.uibk.keylessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.keylessapp.Models.SystemLogEntry;

/**
 * Created by descl on 28.04.2018.
 *
 * Class which displays the acces protocol of a flat or the main entrance.
 */

public class AccessProtocolActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessprotocol);
        Button goToSystemLogRequestListActivity = (Button) findViewById(R.id.button_goToSystemLogRequestActivity);

        goToSystemLogRequestListActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccessProtocolActivity.this, SystemLogRequestListActivity.class));
            }
        });
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getProtocol();

        final SwipeRefreshLayout mSwipeRefreshlayout= (SwipeRefreshLayout) findViewById(R.id.swiperefreshProtocol);
        mSwipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshlayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshlayout.setRefreshing(false);
                        getProtocol();
                    }
                },700);
            }
        });
    }

    //Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void getProtocol(){
        new ProtocolRequest().execute();
    }

    protected void showProtocol(List<String> answer){
        ListView testView= (ListView) findViewById(R.id.protocolList);
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, answer);
        testView.setAdapter(adapter);
    }

    public void goBackToLogin() {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }

    /**
    * Class for sending protocol requests to the server
    * the request parameter is the session token of the user
    * the response is a List<> where every list item is a protocol entry
     */
    private class ProtocolRequest extends AsyncTask<Void,Void,String>{
        String username;

        private List<SystemLogEntry> answer = new ArrayList<>();

        private String getSessionToken() {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
            this.username = sharedPref.getString("savedUsername", "-error-");
            return sharedPref.getString("savedSession", "-error-");
        }

        @Override
        protected String doInBackground(Void... params){

            try {
                final String url= getString(R.string.backend_ip)+"/log";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", getSessionToken());
                request.put("username", this.username);
                SystemLogEntry[] response= restTemplate.postForObject(url, request, SystemLogEntry[].class);
                for (SystemLogEntry e : response) {
                    answer.add(e);
                }
                return answer.toString();
            }catch(Exception e){
                Log.e("AccessProtocolActivity", e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            List<String> toDisplay= new ArrayList<>();
            if (result!= null){
                for(SystemLogEntry e: answer){
                    toDisplay.add(e.getLogTime().toString()+"| "+e.getType().toString()+" | "+e.getEvent()+" | "+e.getActor());
                }
                showProtocol(toDisplay);
            } else {
                goBackToLogin();
            }
        }


    }

}
