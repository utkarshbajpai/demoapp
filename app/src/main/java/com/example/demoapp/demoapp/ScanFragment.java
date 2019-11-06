package com.example.demoapp.demoapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ScanFragment extends Fragment {

    public static final String FRAGMENT_TAG = "ScanFragment";

    // TODO: Rename and change types of parameters
    private TextView serialNumber;
    private TextView dateOpened;
    private TextView timeOpened;
    private TextView timeLeft;

    private Button okButton;
    private CountDownTimer timer;
    private Context context;
    Table dbTable;

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
        serialNumber = (TextView) getView().findViewById(R.id.serial_number_textView);
        dateOpened = (TextView) getView().findViewById(R.id.date_opened_textView);
        timeOpened = (TextView) getView().findViewById(R.id.time_opened_textView);
        timeLeft = (TextView) getView().findViewById(R.id.time_left_textView);

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
                String itemId = result.getContents();

                ScanInfo scanInfo = getScanInfo(itemId);

                if(scanInfo == null){
                    // Mark bottle as opened and publish current time and days till expiry to DynamoDB
                    long currentTimeSeconds = System.currentTimeMillis() / 1000;
                    // FIXME: Replaced with actual time left from QR scan
                    scanInfo = new ScanInfo(currentTimeSeconds, 3 * 24 * 3600);
                }

                serialNumber.setText("Serial Number: " + itemId);

                // Create date and hour for time opened
                Date openDate = new Date(scanInfo.startTimestamp * 1000L);
                SimpleDateFormat jdfDay = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat jdfTime = new SimpleDateFormat("HH:mm:ss");
                jdfDay.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                jdfTime.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String openDay = jdfDay.format(openDate);
                String openTime = jdfDay.format(openDate);

                dateOpened.setText("Opened On: " + openDay);
                timeOpened.setText("Time Opened: " + openTime);

                // Display countdown
                if(scanInfo.timeLeft < 0) {
                    timeLeft.setText("Time Left: EXPIRED");
                } else {
                    timer = new CountDownTimer(scanInfo.timeLeft * 1000, 1000) {
                        int counter = 1;

                        @Override
                        public void onTick(long millisUntilFinished) {
                            timeLeft.setText("Time Left: " + String.valueOf(millisUntilFinished / 1000));
                            counter++;
                        }

                        @Override
                        public void onFinish() {
                            timeLeft.setText("Time Left: EXPIRED");
                        }

                    }.start();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    ScanInfo getScanInfo(String id){
        Document document = dbTable.getItem(new Primitive(id));
        int result = document.get("start_time").asInt();

        // If not present, return null

        // Get expiryTimeDays and startTimestamp from DynamoDB
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long expiryTimeSeconds = expiryTimeDays * 24 * 3600;
        long timeLeft = startTimestamp + expiryTimeSeconds - currentTimeSeconds;

        return new ScanInfo(startTimestamp, timeLeft);
    }
}
