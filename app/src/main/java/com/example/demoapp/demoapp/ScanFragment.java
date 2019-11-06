package com.example.demoapp.demoapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
public class ScanFragment extends Fragment {

    public static final String FRAGMENT_TAG = "ScanFragment";
    String TABLE_NAME = "qr_code_app";
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonDynamoDBClient dbClient;
    Table dbTable;

    // TODO: Rename and change types of parameters
    private TextView scanResult;
    private Button okButton;
    private CountDownTimer timer;
    private Context context;

    public ScanFragment() {
    }

    public static ScanFragment newInstance() {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        scanResult = (TextView) getView().findViewById(R.id.scan_result_textView);
        okButton = (Button)getView().findViewById(R.id.ok_button);
        IntentIntegrator.forSupportFragment(ScanFragment.this).initiateScan();
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                IntentIntegrator.forSupportFragment(ScanFragment.this).initiateScan();
            }
        });
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
            } else {
                Log.d("MainActivity", "Scanned" + result.getContents());
                String item_id = result.getContents();
                getTimeLeft(item_id);

                timer = new CountDownTimer(50000, 1000) {
                    int counter = 1;

                    @Override
                    public void onTick(long millisUntilFinished) {
                        scanResult.setText("Time Left: "+ String.valueOf(millisUntilFinished/1000));
                        counter++;
                    }

                    @Override
                    public void onFinish() {
                        scanResult.setText("EXPIRED");
                    }
                }.start();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void getTimeLeft(String id){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                String id = strings[0];
                credentialsProvider = new CognitoCachingCredentialsProvider(
                        getActivity(),
                        "ap-southeast-1:91f6eaad-2386-4374-ba88-e23bf7fac3fe", // Identity pool ID
                        Regions.AP_SOUTHEAST_1 // Region
                );
                dbClient = new AmazonDynamoDBClient(credentialsProvider);
                dbTable = Table.loadTable(dbClient, TABLE_NAME);
                Log.d("ScanFragment", "Getting item from Table");
                Document document = dbTable.getItem(new Primitive(id));
                String result = document.get("start_time").asString();
                Log.d("ScanFragment", "Retrieved Item " +  result);
                Log.d("Result", result);
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                scanResult.setText(s);
                super.onPostExecute(s);
            }
        }.execute(id);


    }
}
