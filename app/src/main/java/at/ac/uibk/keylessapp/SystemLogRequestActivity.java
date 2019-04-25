package at.ac.uibk.keylessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import at.ac.uibk.keylessapp.Models.SystemLogRequest;

/**
 * Created by descl on 25.11.2018.
 */

public class SystemLogRequestActivity extends AppCompatActivity {

    private SystemLogRequest logRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_log_request);

        Bundle b = getIntent().getExtras();
        if(b.getString("message") != null) {
            String message = b.getString("message");
            String text = b.getString("text");
            showSystemLogRequestMessage(text, message);
        } else {
            new SystemLogMessageRequest().execute();
        }

        Button buttonAccept= (Button) findViewById(R.id.button_systemLogAccept);
        Button buttonDecline= (Button) findViewById(R.id.button_systemLogDecline);

        if(b.getString("message") != null) {
            final long id = b.getLong("id");
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new SystemLogAcceptRequest(id).execute();
                    startActivity(new Intent(SystemLogRequestActivity.this, SystemLogRequestListActivity.class));
                }
            });
        } else {
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new SystemLogAcceptRequest(logRequest.getRequestId()).execute();
                    startActivity(new Intent(SystemLogRequestActivity.this, SystemLogRequestListActivity.class));
                }
            });
        }

        buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SystemLogRequestActivity.this, SystemLogRequestListActivity.class));
            }
        });
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * method for the toolbar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showSystemLogRequestMessage(SystemLogRequest logRequest){
        this.logRequest=logRequest;
        TextView messageView= (TextView) findViewById(R.id.textView_message_content_systemlogrequest);
        TextView logType= (TextView) findViewById(R.id.textViewLogType);
        messageView.setText(logRequest.getMessage());
        logType.setText("Log vom Typ: "+logRequest.getType().toString()+" vom Tag: "+logRequest.getDay().toString());
    }

    public void showSystemLogRequestMessage(String text, String message){
        TextView messageView= (TextView) findViewById(R.id.textView_message_content_systemlogrequest);
        TextView logType= (TextView) findViewById(R.id.textViewLogType);
        messageView.setText(message);
        logType.setText(text);
    }

    private class SystemLogMessageRequest extends AsyncTask<Void, Void, String> {

        private Map<String, String> request = new HashMap<>();
        private SystemLogRequest newestRequest;

        private SystemLogMessageRequest() {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url = getString(R.string.backend_ip)+"/log/request/newest";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                newestRequest = restTemplate.postForObject(url, request, SystemLogRequest.class);
                return newestRequest.toString();
            } catch (ResourceAccessException e) {
                return "ResourceAccessException";
            } catch (Exception e) {
                Log.e("SystemLogRequestActivit", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            showSystemLogRequestMessage(newestRequest);
        }
    }


    private class SystemLogAcceptRequest extends AsyncTask<Void, Void, String> {

        private Map<String, Object> request = new HashMap<>();

        private SystemLogAcceptRequest(long logrequestId) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("requestId", logrequestId);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url = getString(R.string.backend_ip)+"/log/accept-request";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                restTemplate.put(url,request);
                return "done";
            } catch (ResourceAccessException e) {
                return "ResourceAccessException";
            } catch (Exception e) {
                Log.e("SystemLogRequestActivit", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) { }
    }

}
