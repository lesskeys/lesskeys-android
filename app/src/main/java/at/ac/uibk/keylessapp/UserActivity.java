package at.ac.uibk.keylessapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.keylessapp.Models.SystemLock;
import at.ac.uibk.keylessapp.Models.SystemUser;
import at.ac.uibk.keylessapp.Utils.Utils;


/**
 * Created by descl on 01.06.2018.
 * This class is used for editing the logged in user.
 * For example change your password.
 * It is also possible to create a new user.
 */

public class UserActivity extends AppCompatActivity{
    private String passwortChangeAnswer="";
    private SystemUser currentSystemUser;
    List<String> stringListofLocks= new ArrayList<>();
    List<SystemLock> systemLocksList= new ArrayList<>();
    private String serveranswer = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new UserDataRequest().execute();
        new LockListRequest().execute();
        final SwipeRefreshLayout mSwipeRefreshlayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshUserActivity);
        mSwipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshlayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshlayout.setRefreshing(false);
                        new UserDataRequest().execute();
                        new LockListRequest().execute();
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

    /**
     * this method is used to change a users password or email
     * also a new user could be added by clicking the addUser button
     * When adding a new a new dialog is opened where the user can add firstname, email, pw of the
     * new created subuser.
     * @param user
     */
    public void showUserSettings(SystemUser user){
        EditText firstname= (EditText) findViewById(R.id.editTextName);
        EditText email= (EditText) findViewById(R.id.editTextEMail);
        final Button password= (Button) findViewById(R.id.buttonPassword);
        final Button addUser= (Button) findViewById(R.id.buttonNewUser);

        firstname.setText(user.getFirstName());
        email.setText(user.getEmail());

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
                LinearLayout layout= new LinearLayout(UserActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                alertDialog.setTitle("PASSWORT");
                alertDialog.setMessage("Zur Änderung des Passwortes zuerst das alte und dann das neue Passwort eingeben");

                final TextView inputOldInfo= new TextView(UserActivity.this);
                final TextView inputNewInfo= new TextView(UserActivity.this);
                final TextView inputNewInf02= new TextView(UserActivity.this);
                TextView newline= new TextView(UserActivity.this);

                inputOldInfo.setText("Altes Passwort");
                inputNewInfo.setText("Neues Passwort");
                inputNewInf02.setText("Passwort wiederholen");


                final EditText inputold= new EditText(UserActivity.this);
                inputold.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputold.setTransformationMethod(PasswordTransformationMethod.getInstance());

                final EditText inputnew= new EditText(UserActivity.this);
                inputnew.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputnew.setTransformationMethod(PasswordTransformationMethod.getInstance());

                final EditText inputnew2= new EditText(UserActivity.this);
                inputnew2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputnew2.setTransformationMethod(PasswordTransformationMethod.getInstance());

                layout.addView(newline);
                layout.addView(inputOldInfo);
                layout.addView(inputold);
                layout.addView(inputNewInfo);
                layout.addView(inputnew);
                layout.addView(inputNewInf02);
                layout.addView(inputnew2);

                alertDialog.setView(layout);
                alertDialog.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alertDialog.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String oldPw= "";
                        String newPw= "";
                        String newPw2="";


                        oldPw=inputold.getText().toString();
                        newPw=inputnew.getText().toString();
                        newPw2= inputnew2.getText().toString();
                        if(newPw.equals(newPw2)){
                            new UserPasswordChangeRequest(oldPw,newPw,newPw2).execute();
                            while(getPasswortChangeAnswer().isEmpty()){}
                            if (!getPasswortChangeAnswer().isEmpty() && !(getPasswortChangeAnswer().contains("Failure"))){
                                dialogInterface.dismiss();
                            }else {
                                Toast.makeText(UserActivity.this,"Passwortänderung fehlgeschlagen",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(UserActivity.this,"Passwörter stimmen nicht überein",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                alertDialog.show();
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
                LinearLayout layout= new LinearLayout(UserActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                alertDialog.setTitle("E-Mail");
                alertDialog.setMessage("Zur Änderung der E-mail(Benutzername) die neue E-mail in das Textfeld eingeben");
                final EditText inputEmail= new EditText(UserActivity.this);
                inputEmail.setInputType(InputType.TYPE_CLASS_TEXT);
                inputEmail.setText(getCurrentSystemUser().getEmail());
                layout.addView(inputEmail);
                alertDialog.setView(layout);
                alertDialog.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email= inputEmail.getText().toString();
                        new UserDataChangeRequest(email,"").execute();
                        dialogInterface.dismiss();
                    }
                });

                alertDialog.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();
            }
        });


        firstname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
                LinearLayout layout= new LinearLayout(UserActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                alertDialog.setTitle("Name");
                alertDialog.setMessage("Zur Änderung des Namens diesen in das Eingabefeld eingeben und auf Speichern klicken");
                final EditText inputName= new EditText(UserActivity.this);
                inputName.setInputType(InputType.TYPE_CLASS_TEXT);
                inputName.setText(getCurrentSystemUser().getFirstName());
                layout.addView(inputName);
                alertDialog.setView(layout);
                alertDialog.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name= inputName.getText().toString();
                        new UserDataChangeRequest("",name).execute();
                        dialogInterface.dismiss();
                    }
                });

                alertDialog.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();

            }
        });


        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
                LinearLayout layout= new LinearLayout(UserActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                alertDialog.setTitle("Neuen Benutzer erstellen");

                TextView infoNewFirstName= new TextView(UserActivity.this);
                TextView infoNewLastName= new TextView(UserActivity.this);
                TextView infoNewUserName= new TextView(UserActivity.this);
                TextView infoNewPassword= new TextView(UserActivity.this);
                TextView newline= new TextView(UserActivity.this);

                infoNewFirstName.setText("Vorname");
                infoNewLastName.setText("Nachname");
                infoNewUserName.setText("Email");
                infoNewPassword.setText("Passwort");

                final ListView locks= new ListView(UserActivity.this);

                final EditText newFirstName= new EditText(UserActivity.this);
                final EditText newLastName= new EditText(UserActivity.this);
                final EditText newUserName= new EditText(UserActivity.this);
                final EditText newPw= new EditText(UserActivity.this);
                final Spinner roles = new Spinner(UserActivity.this);
                final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(UserActivity.this,android.R.layout.simple_spinner_dropdown_item,
                        Utils.getSubRoles(currentSystemUser.getRole()));
                roles.setAdapter(spinnerAdapter);


                newPw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                newPw.setTransformationMethod(PasswordTransformationMethod.getInstance());

                newFirstName.setText("Name");
                newLastName.setText("Nachname");
                newUserName.setText("e-mail");

                newUserName.setInputType(InputType.TYPE_CLASS_TEXT);
                newFirstName.setInputType(InputType.TYPE_CLASS_TEXT);
                newLastName.setInputType(InputType.TYPE_CLASS_TEXT);

                final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(UserActivity.this,android.R.layout.select_dialog_multichoice,stringListofLocks);
                locks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                locks.setAdapter(arrayAdapter);

                layout.addView(newline);
                layout.addView(infoNewFirstName);
                layout.addView(newFirstName);
                layout.addView(infoNewLastName);
                layout.addView(newLastName);
                layout.addView(infoNewUserName);
                layout.addView(newUserName);
                layout.addView(infoNewPassword);
                layout.addView(newPw);
                layout.addView(roles);
                layout.addView(locks);
                alertDialog.setView(layout);

                alertDialog.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alertDialog.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newpw=newPw.getText().toString();
                        String newfirstname= newFirstName.getText().toString();
                        String newlastname= newLastName.getText().toString();
                        String newusername= newUserName.getText().toString();
                        String role= roles.getSelectedItem().toString();
                        List<Long> locksToSend= new ArrayList<>();
                        SparseBooleanArray checked= locks.getCheckedItemPositions();
                        for(int j= 0; j<locks.getAdapter().getCount();j++){
                            if(checked.get(j)){
                                locksToSend.add(systemLocksList.get(j).getLockId());
                            }
                        }
                        new CreateNewUserRequest(newusername,newpw,newfirstname,newlastname,role,locksToSend).execute();
                        if(serveranswer.equals("Failure!")){
                            Toast.makeText(UserActivity.this,"Erstellung des neuen Benutzer fehlgeschlagen",Toast.LENGTH_SHORT).show();
                        }else{
                            dialogInterface.dismiss();
                        }


                    }
                });
                alertDialog.show();
            }

        });

    }

    public String getPasswortChangeAnswer() {
        return passwortChangeAnswer;
    }

    public void setPasswortChangeAnswer(String passwortChangeAnswer) {
        this.passwortChangeAnswer = passwortChangeAnswer;
    }

    public SystemUser getCurrentSystemUser() {
        return currentSystemUser;
    }

    public void setCurrentSystemUser(SystemUser currentSystemUser) {
        this.currentSystemUser = currentSystemUser;
    }

    private void showPasswordChangeAnswer(String answer){
        setPasswortChangeAnswer(answer);
    }

    protected void returnLockListForUser(List<SystemLock> answer){
        for(SystemLock l: answer){
            stringListofLocks.add(l.getName().toString());
            systemLocksList.add(l);
        }

    }



    private class UserDataRequest extends AsyncTask<Void,Void,String> {
        private SystemUser answer = new SystemUser();
        private String session;
        private String username;

        private UserDataRequest() {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            this.session = sharedPref.getString("savedSession", "-error-");
            this.username = sharedPref.getString("savedUsername", "-error-");
        }


        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url = getString(R.string.backend_ip)+"/user/current";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                SystemUser response= restTemplate.postForObject(url,request,SystemUser.class);
                answer=response;
                return answer.toString();
            }catch (Exception e){
                Log.e("UserActivity",e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            showUserSettings(answer);
            setCurrentSystemUser(answer);
        }

    }

    /**
     * class to change the password of a logged in user
     */
    private class UserPasswordChangeRequest extends AsyncTask<Void,Void,String> {
        private String answer;
        private String oldPw;
        private String newPw1;
        private String newPw2;
        private String session;
        private String username;

        private UserPasswordChangeRequest(String oldPw,String newPw1, String newPw2) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            this.session = sharedPref.getString("savedSession", "-error-");
            this.username = sharedPref.getString("savedUsername", "-error-");
            this.oldPw= oldPw;
            this.newPw1= newPw1;
            this.newPw2= newPw2;
        }



        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url = getString(R.string.backend_ip)+"/user/edit-password";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                request.put("oldPw",oldPw);
                request.put("newPw1",newPw1);
                request.put("newPw2",newPw2);
                String response= restTemplate.postForObject(url,request,String.class);
                answer= response;
                setPasswortChangeAnswer(answer);
                return answer;
            } catch (Exception e){
                Log.e("UserActivity", e.getMessage(),e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            setPasswortChangeAnswer(answer);
        }

    }

    private void showServerAnswer(String answer){
       this.serveranswer= answer;
    }


    /**
     * class to send the new created user to the server
     */
    private class CreateNewUserRequest extends AsyncTask<Void,Void,String>{
        private Map<String, Object> requestBody = new HashMap<>();
        String answer;

        private CreateNewUserRequest(String newUsername, String newPw, String newFirstName, String newLastName, String role,  List<Long> locks){
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            requestBody.put("session", sharedPref.getString("savedSession", "-error-"));
            requestBody.put("username", sharedPref.getString("savedUsername", "-error-"));
            requestBody.put("newUsername",newUsername);
            requestBody.put("newPw1",newPw);
            requestBody.put("newPw2",newPw);
            requestBody.put("newFirstName",newFirstName);
            requestBody.put("newLastName",newLastName);
            requestBody.put("newRole",role);
            requestBody.put("lockIds", locks);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url= getString(R.string.backend_ip)+"/user/add";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> response= new HashMap<>();
                response= restTemplate.postForObject(url, requestBody,Map.class);

                answer= response.get("status");
                return answer;
            }catch (Exception e){
                Log.e("UserActivity",e.getMessage(),e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            showServerAnswer(answer);
        }

    }


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
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                SystemLock[] response= restTemplate.postForObject(url,request,SystemLock[].class);
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

    private class UserDataChangeRequest extends AsyncTask<Void,Void,String>{
        private String email;
        private String firstname;
        private String lastname;
        private String session;
        private String username;

        private UserDataChangeRequest(String email,String firstname){
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            this.session = sharedPref.getString("savedSession", "-error-");
            this.username = sharedPref.getString("savedUsername", "-error-");
            this.email=email;
            this.firstname=firstname;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url = getString(R.string.backend_ip)+"/user/edit";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                if(email.equals("")){
                    request.put("newUsername",username);
                    email=username;
                }else{
                    request.put("newUsername",email);
                }
                if(firstname.equals("")){
                    request.put("newFirstName",getCurrentSystemUser().getFirstName());
                }else{
                    request.put("newFirstName",firstname);
                }
                restTemplate.put(url,request);
                return "";

            }catch (Exception e){
                Log.e("UserActivity",e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("savedUsername", email);
            editor.commit();

        }
    }



}
