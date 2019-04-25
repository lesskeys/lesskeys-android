package at.ac.uibk.keylessapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.os.AsyncTask;

import com.nxp.nfclib.CardType;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.DESFireFile;
import com.nxp.nfclib.desfire.EV1ApplicationKeySettings;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.exceptions.InvalidResponseLengthException;
import com.nxp.nfclib.exceptions.NxpNfcLibException;


import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.spec.SecretKeySpec;

import at.ac.uibk.keylessapp.Models.KeyMode;
import at.ac.uibk.keylessapp.Models.SystemKey;
import at.ac.uibk.keylessapp.Models.SystemLock;
import at.ac.uibk.keylessapp.Utils.Utils;


/**
 * NfcActivity class used to write on new NFC chips
 * We use the Taplinx library to write on MIFARE DesFireEV1 Chips
 */
public class NfcActivity extends AppCompatActivity {
    //Key for TapLinx
    List<String> stringListofLocks= new ArrayList<>();
    List<SystemLock> systemLocksList= new ArrayList<>();
    private byte[] masterKey;
    private NxpNfcLib m_libInstance= null;
    private String TAG= NfcActivity.class.getSimpleName();
    private  EditText name= null;
    private  ListView locks= null;
    private  AlertDialog alertDialog= null;
    private AlertDialog.Builder alert= null;
    //Desfire card object
    private IDESFireEV1 objDesfire;



    /**
     * This key is used to authenticate to a factory new DESFireEV1.
     */
    public static final byte[] DEFAULT_KEY_2KTDES = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    };


    /**
     * method to initialize the Taplinx Library
     * therefore we have a key which is defined in a separat file
     */
    private void initializeLibrary(){
        Scanner scanner = new Scanner(new File("taplinx-key.txt"));
        String key = scanner.nextLine();
        m_libInstance= NxpNfcLib.getInstance();
        m_libInstance.registerActivity(this, key);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_nfc);
        new MasterKeyRequest().execute();

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getLockListForUser();
        initializeLibrary();
        instantiate();
        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(NfcActivity.this,android.R.layout.simple_list_item_multiple_choice,stringListofLocks);
        locks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        locks.setAdapter(arrayAdapter);
        Button accept= (Button) findViewById(R.id.buttonAddKey);

        alert.setTitle("Schlüssel erstellen");
        alert.setMessage("Schlüssel nun an das Telefon halten");
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog= alert.show();
            }
        });
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

    protected void instantiate(){
        name= (EditText) findViewById(R.id.editTextAddKeyName);
        locks= (ListView) findViewById(R.id.ListViewAddKeyLockList);
        alert= new AlertDialog.Builder(NfcActivity.this);
    }

    /**
     *  activate NFC intents only if app is in foreground
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
     *  the desfireCardLogic is executed. Otherwise the user is informed that the key is not a
     *  DesfireEV1 and therefore not usable, and secure.
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

                    String input_text="";
                    input_text=name.getText().toString();
                    List<Long> lockstoSend= new ArrayList<>();
                    SparseBooleanArray checked= locks.getCheckedItemPositions();
                    for(int j= 0; j<locks.getAdapter().getCount();j++){
                        if(checked.get(j)){
                            lockstoSend.add(systemLocksList.get(j).getLockId());
                        }
                    }
                    desfireCardLogic(input_text,lockstoSend);

                }catch (Throwable t){
                    t.printStackTrace();
                }
        }else{
            Toast.makeText(this, "Kein DesfireEV1", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * The desfireCardLogic method is used for the DesFireEV1
     * If the new detected Tag is a DesFireEV1, this method is executed.
     * In the method we create a new application, and in this application we create a new file
     * which contains the keyData.
     * We also send the new Key with the unlocked locks of a user to the Server. For the key
     * we use the helper class SystemKey.
     * @param keyName
     * @param lockstoSend
     */
    public void desfireCardLogic(String keyName, List<Long> lockstoSend){
        byte[] newAppId = new byte[]{0x01, 0x01,0x01 };
        String randomString = Utils.randomSecureString16();
        Log.e("RandomString: ", randomString);
        int fileSize = randomString.getBytes().length;
        byte[] data = randomString.getBytes();
        byte[] keyUID= objDesfire.getUID();
        StringBuilder sb = new StringBuilder(keyUID.length * 2);
        for(byte b: keyUID) sb.append(String.format("%02x", b & 0xff));
        String uidString= sb.toString();
        //byte[] data= new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10};

        try{
            objDesfire.getReader().setTimeout(1000);
            objDesfire.selectApplication(0);

            Key defKey= new SecretKeySpec(DEFAULT_KEY_2KTDES,"DESede");
            KeyData defKeyData= new KeyData();
            defKeyData.setKey(defKey);

            objDesfire.authenticate(0,IDESFireEV1.AuthType.Native,KeyType.THREEDES,defKeyData);
            Log.d(TAG, "With defaultKey authenticated.");

            objDesfire.changeKey(0,KeyType.AES128,DEFAULT_KEY_2KTDES,masterKey,(byte) 0);
            Log.d(TAG, "Changed to AES");
            Key aes= new SecretKeySpec(masterKey,"AES");
            KeyData aesKeyData= new KeyData();
            aesKeyData.setKey(aes);
            objDesfire.authenticate(0,IDESFireEV1.AuthType.AES,KeyType.AES128,aesKeyData);

            //begin with writing on desfire

            //format card all applications and files will be deleted
            objDesfire.format();
            EV1ApplicationKeySettings.Builder applicationBuilder = new EV1ApplicationKeySettings.Builder();

            //set permissions for application
            EV1ApplicationKeySettings appsettings = applicationBuilder.setAppKeySettingsChangeable(true)
                    .setAppMasterKeyChangeable(true)
                    .setAuthenticationRequiredForDirectoryConfigurationData(false)
                    .setAuthenticationRequiredForFileManagement(false)
                    .setKeyTypeOfApplicationKeys(KeyType.AES128).build();

            //create new application
            objDesfire.createApplication(newAppId,appsettings);
            objDesfire.selectApplication(newAppId);

            //create new file
            objDesfire.createFile(0, new DESFireFile.StdDataFileSettings(
                    IDESFireEV1.CommunicationType.Enciphered, (byte)0, (byte)0, (byte)0, (byte)0, fileSize
            ));

            objDesfire.authenticate(0,IDESFireEV1.AuthType.AES, KeyType.AES128, aesKeyData);

            //write on file
            objDesfire.writeData(0,0,data);
            Log.d(TAG, "Wrote to file");
            //preparing locks
            String test="";
            //preparing key object and send it
            SystemKey key = new SystemKey(newAppId,masterKey);
            key.setContent(randomString);
            key.setKeyName(keyName);
            key.setUid(uidString);
            new KeyExchange(key, lockstoSend).execute();

            for( long l: lockstoSend){
                test= test+" "+ l;
            }

            //Toast.makeText(getApplicationContext(), "Schlüssel erstellt!", Toast.LENGTH_LONG).show();

            byte[] read = objDesfire.readData(0,0,fileSize);
            //close reader
            //m_textView.setText(test);
            alertDialog.dismiss();
            objDesfire.getReader().close();
        }catch (InvalidResponseLengthException e1){
            Toast.makeText(getApplicationContext(), "Chip ist verschlüsselt", Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Log.d(TAG,"ERROR");
            e.printStackTrace();
        }

    }

    public void showStatus(String status) {
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
    }

    protected void getLockListForUser(){
        new LockListRequest().execute();
    }

    protected void returnLockListForUser(List<SystemLock> answer){
        for(SystemLock l: answer){
            stringListofLocks.add(l.getName().toString());
            systemLocksList.add(l);
        }

    }

    /**
     * class for sending the newly created key with lockpermissions, name, and mode to the server
     */
    private class KeyExchange extends AsyncTask<Void, Void, String> {

        private Map<String, Object> request = new HashMap<>();

        public KeyExchange(SystemKey key, List<Long> locks) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("aid", key.getAid());
            request.put("content", key.getContent());
            request.put("name", key.getKeyName());
            request.put("uid",key.getUid());
            request.put("lockIds", locks);
            request.put("mode", KeyMode.ENABLED);
        }

        @Override
        protected String doInBackground(Void... params){
            try{
                final String url = getString(R.string.backend_ip)+"/key/register";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
                String answer = response.get("status");
                return answer;
            }catch(Exception e){
                Log.e("KeyExchange",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            showStatus(result);
        }
    }


    /**
     * class for requesting the locks of a given user
     */
    private class LockListRequest extends AsyncTask<Void, Void, String> {
        private List<SystemLock> answer = new ArrayList<>();
        private String session;
        private String username;

        private LockListRequest(){
            SharedPreferences sharedPreferences= getSharedPreferences("keyless-pref",Context.MODE_PRIVATE);
            this.session= sharedPreferences.getString("savedSession","-error-");
            this.username= sharedPreferences.getString("savedUsername", "-error-");
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url = getString(R.string.backend_ip)+"/lock/get-for-user";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                SystemLock[] response = restTemplate.postForObject(url,request,SystemLock[].class);
                for(SystemLock l: response){
                    answer.add(l);
                }
                return answer.toString();
            }catch (Exception e){
                Log.e("NfcActivity", e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            returnLockListForUser(answer);
        }


    }

    public void setMasterKey(byte[] answer){
        this.masterKey= answer;
    }

    /**
     * class to request the secret masterKey of the system. The masterKey is used to
     * encrypt the content of the key.
     */
    private class MasterKeyRequest extends AsyncTask<Void,Void,String> {
        byte[] answer;
        private String session;
        private String username;

        private MasterKeyRequest() {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            this.session = sharedPref.getString("savedSession", "-error-");
            this.username = sharedPref.getString("savedUsername", "-error-");
        }


        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url= getString(R.string.backend_ip)+"/master-key/aes";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                byte[] response= restTemplate.postForObject(url,request,byte[].class);
                answer=response;
            }catch (Exception e){
                Log.e("UserActivity",e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            setMasterKey(answer);
        }

    }
}
