package at.ac.uibk.keylessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import at.ac.uibk.keylessapp.Models.SystemUser;

/**
 * Created by descl on 13.11.2018.
 */

public class MainActivityVisitor extends AppCompatActivity{

    private SystemUser user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_visitor);

        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
        String username = sharedPref.getString("savedUsername","-error-");
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(username);

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


        Button logout_bt= (Button) findViewById(R.id.buttonlogoutvisitor);
        //logout
        logout_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSharedPreferences("keyless-pref");
                startActivity(new Intent(MainActivityVisitor.this, LogInActivity.class));
            }
        });

        final SwipeRefreshLayout mSwipeRefreshlayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
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
}
