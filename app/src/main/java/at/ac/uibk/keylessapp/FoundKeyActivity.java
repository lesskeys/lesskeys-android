package at.ac.uibk.keylessapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.nxp.nfclib.CardType;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.EV1ApplicationKeySettings;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.exceptions.NxpNfcLibException;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by descl on 14.08.2018.
 * Class which gives a user the possibility to read and get the name of a found key.
 */

public class FoundKeyActivity extends AppCompatActivity {

    //Key for TapLinx
    private String m_strKey="dd115779539e7c583860d388e447ac02";
    private byte[] masterKey;
    private NxpNfcLib m_libInstance= null;
    private String TAG= NfcActivity.class.getSimpleName();
    private IDESFireEV1 objDesfire;
    TextView keynameTextView;


    /**
     * method to initialize the Taplinx Library
     * therefore we have a key (m_strKey) which is defined at the beginning of the class
     */
    private void initializeLibrary(){
        m_libInstance= NxpNfcLib.getInstance();
        m_libInstance.registerActivity(this, m_strKey);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_key);
        initializeLibrary();
        //Toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        keynameTextView=(TextView) findViewById(R.id.textViewKeyName);
        keynameTextView.setText("");
    }

    //Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * activate NFC intents only if the app is in foreground
     */
    @Override
    protected void onResume(){
        m_libInstance.startForeGroundDispatch();
        super.onResume();
    }

    @Override
    protected void onPause(){
        m_libInstance.stopForeGroundDispatch();
        super.onPause();
    }


    @Override
    public void onNewIntent (final Intent intent){
        Log.d(TAG,"onNewIntent");
        cardLogic(intent);
        super.onNewIntent(intent);
    }

    /**
     *  Connects the reader of the phone to the chip. Checks if the given chip is
     *  a chip of the type DESFireEV1, which is used in this project. If the chip is a DESFireEV1,
     *  the desfireCardLogic is executed. Otherwise a short text tells the user that the found chip
     *  is not part of the system for the facility.
     * @param intent
     */
    private void cardLogic(final Intent intent){
        CardType cardType = CardType.UnknownCard;
        try{
            cardType = m_libInstance.getCardType(intent);
        } catch(NxpNfcLibException nxp){
            Toast.makeText(this, nxp.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG,"Card type found: "+cardType.getTagName());

        if (CardType.DESFireEV1==cardType){
            objDesfire= DESFireFactory.getInstance().getDESFire(m_libInstance.getCustomModules());
            try{
                objDesfire.getReader().connect();
                //set a timeout to prevent exceptions in authenticate
                objDesfire.getReader().setTimeout(1000);

                desfireCardLogic();

            }catch (Throwable t){
                t.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Kein Schlüssel des Hauses", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method which is executed if the chip is a DESFireEV1. The method gets
     * the uid of the chip, which is a byte array and then converts it to a String
     * for sending it to the Server.
     */
    public void desfireCardLogic(){
        try{
            byte[] uid= objDesfire.getUID();
            StringBuilder sb = new StringBuilder(uid.length * 2);
            for(byte b: uid) sb.append(String.format("%02x", b & 0xff));
            String uidString = sb.toString();
            new KeyNameRequest(uidString).execute();
            objDesfire.getReader().close();
        }catch (Exception e){
            Log.d(TAG,"ERROR");
            e.printStackTrace();
        }
    }


    public void setKeyName(String result) {
        this.keynameTextView.setText(result);
    }

    /**
     * Inner class for the KeyNameRequest.
     * A post request with the session, username and the uid is executed.
     * The response of the request is a String with the name of the owner or a message which
     * tells the user that the key is not  part of the system.
     */
    private class KeyNameRequest extends AsyncTask<Void,Void,String> {
        private Map<String,String> answer= new HashMap<>();
        private String uid;
        private String session;
        private String username;
        private String firstName;
        private String lastName;

        private KeyNameRequest(String uid) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            this.session = sharedPref.getString("savedSession", "-error-");
            this.username = sharedPref.getString("savedUsername", "-error-");
            this.uid= uid;
        }


        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url= getString(R.string.backend_ip)+"/key/find";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, Object> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                request.put("uid", uid);
                answer= restTemplate.postForObject(url,request,Map.class);
                if(answer !=null){
                    firstName= answer.get("firstName");
                    lastName= answer.get("lastName");
                    Log.d(TAG,firstName+lastName);
                    return firstName+ " "+ lastName;
                }else{
                    return "Kein Besitzer";
                }
            }catch (Exception e){
                Log.e("UserActivity",e.getMessage(),e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            setKeyName(result);
        }

    }


}
