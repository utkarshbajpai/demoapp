package com.example.demoapp.demoapp;

import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class AWSDynamoClientConnection  extends AsyncTask<Context, Void, Table>{
    String TABLE_NAME = "qr_code_app";
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonDynamoDBClient dbClient;
    Table dbTable;

    @Override
    protected Table doInBackground(Context... contexts) {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                contexts[0],
                "ap-southeast-1:91f6eaad-2386-4374-ba88-e23bf7fac3fe", // Identity pool ID
                Regions.AP_SOUTHEAST_1 // Region
        );
        dbClient = new AmazonDynamoDBClient(credentialsProvider);
        dbTable = Table.loadTable(dbClient, TABLE_NAME);
        return dbTable;
    }

}
