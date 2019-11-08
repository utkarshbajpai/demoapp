package com.example.demoapp.demoapp;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class DatabaseAccess {

    private Context context;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient dbClient;
    private Table dbTable;

    private static volatile DatabaseAccess instance;

    private DatabaseAccess(Context context) {
        this.context = context;

        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-southeast-1:91f6eaad-2386-4374-ba88-e23bf7fac3fe", // Identity pool ID
                Regions.AP_SOUTHEAST_1 // Region
        );
        dbClient = new AmazonDynamoDBClient(credentialsProvider);
        dbClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        dbTable = Table.loadTable(dbClient, "qr_code_app");
    }

    public static synchronized DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public Document getItem(String id) {
        Document item = dbTable.getItem(new Primitive(id));
        if (item == null) {
            item = new Document();
            item.put("serial_number", id);
            item.put("expiry_time_days", 3);
            item.put("start_timestamp", System.currentTimeMillis() / 1000);
            dbTable.putItem(item);
        }
        return item;
    }

}
