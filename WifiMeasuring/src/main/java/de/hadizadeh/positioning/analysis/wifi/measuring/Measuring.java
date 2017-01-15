package de.hadizadeh.positioning.analysis.wifi.measuring;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import de.hadizadeh.positioning.model.SignalInformation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Measuring extends Activity {
    private int logSeconds = 10 * 60;
    private Map<String, List<Integer>> loggedData;
    private Map<String, String> identificationTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measuring);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        identificationTable = new HashMap<String, String>();

        // Nano Router
        identificationTable.put("10:FE:ED:AC:6B:FC".toLowerCase(), "0 Meter"); // 1
        identificationTable.put("10:FE:ED:AC:70:36".toLowerCase(), "5 Meter"); // 2
        identificationTable.put("10:FE:ED:AC:6D:B8".toLowerCase(), "15 Meter"); // 3

        List<String> keyWhiteList = new ArrayList<String>();
        for (Map.Entry<String, String> ap : identificationTable.entrySet()) {
            keyWhiteList.add(ap.getKey());
        }

        final WifiTechnology wifiTechnology = new WifiTechnology(this, "WIFI", keyWhiteList);
        //wifiTechnology.startScanning();
        loggedData = new HashMap<String, List<Integer>>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000); // Initialize reading
                    for (int i = 0; i <= logSeconds; i++) {
                        Log.i("Time", i + ". second");
                        logSignalData(wifiTechnology.getSignalData());
                        System.out.println(wifiTechnology.getSignalData().toString());
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                createCsvFile();
            }
        }).start();
    }

    private void logSignalData(Map<String, SignalInformation> signalData) {
        for (Map.Entry<String, SignalInformation> apData : signalData.entrySet()) {
            if (identificationTable.containsKey(apData.getKey())) {
                if (!loggedData.containsKey(identificationTable.get(apData.getKey()))) {
                    loggedData.put(identificationTable.get(apData.getKey()), new ArrayList<Integer>());
                }
                loggedData.get(identificationTable.get(apData.getKey())).add((int) apData.getValue().getStrength());
            }
        }
    }

    private void createCsvFile() {
        String text = "Time;";
        for (int i = 0; i <= logSeconds; i++) {
            text += i + ";";
        }
        text += "\n";
        for (Map.Entry<String, List<Integer>> dataLine : loggedData.entrySet()) {
            text += formatRssiLine(dataLine.getKey(), dataLine.getValue()) + "\n";
        }
        System.out.println(text);

        File file = new File(Environment.getExternalStorageDirectory(), "wifiRssiData.csv");
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(text);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("Persistence", "File created.");
        Toast.makeText(this, "File created.", Toast.LENGTH_LONG).show();
    }

    private String formatRssiLine(String title, List<Integer> rssiValues) {
        String text = title + ";";
        for (Integer rssiValue : rssiValues) {
            text += rssiValue + ";";
        }
        return text;
    }
}
