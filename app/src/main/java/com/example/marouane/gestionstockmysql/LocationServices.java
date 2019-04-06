package com.example.marouane.gestionstockmysql;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import Models.Produit;

public class LocationServices extends Service {
    public static final String CHANNEL_ID = "NotificationChannel";
    public static final int NOTIFICATION_TIMEOUT = 10000;

    private LocationManager locationManager;
    private LocationListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("coordinates", location.getLongitude() + " " + location.getLatitude());
                sendBroadcast(i);

                System.out.println("Requesting new notifications...");

                // Get the nearby Stores products...
                ProduitConnectClass con = new ProduitConnectClass("notify");
                con.execute(
                        "http://" + LoginActivity.serverIP + "/GestionStock/Produit/getNearbyProds.php?lat=" + location.getLatitude() + "&lng=" + location.getLongitude());


                /* This code will be executed after the return of the AsyncTask*/
                // Compare with the seen ones
                // Display the unseen products notifications

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, NOTIFICATION_TIMEOUT, 0, listener);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }

    class ProduitConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Produit> allRecords = new ArrayList<>();
        String choice;

        public ProduitConnectClass(String choice) {
            this.choice = choice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Convert data to a list of clients
            try {
                JSONArray all = new JSONArray(s);
                for (int i = 0; i < all.length(); i++) {
                    JSONObject object = all.getJSONObject(i);
                    allRecords.add(new Produit(object.getInt("id"), object.getInt("prix"), object.getInt("qteStock"), object.getString("nom"), object.getString("description"), object.getString("images"), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(object.getString("date_modif"))));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (choice) {
                case "notify":
                    displayNotifications(allRecords);
                    break;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... strings) {
            String returnedValue = null;
            StringBuffer buffer = new StringBuffer();
            try {
                URL url = new URL(strings[0]); //Use the 1st String cause it is the URL needed
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                InputStream inputStream = con.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                }
                returnedValue = buffer.toString();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
//            System.out.println("From the server >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + returnedValue);
            return returnedValue;
        }
    }

    public void displayNotifications(ArrayList<Produit> allRecords) {
        try {
            for (int i = 0; i < allRecords.size(); i++) {
                Produit p = allRecords.get(i);
                if (!p.isSeen(this)) {
                    // Send a notification directly from the Background Service
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(LocationServices.this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.carticon)
                            .setContentTitle(p.getNom())
                            .setContentText(String.valueOf(p.getPrix() + " DHs"))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

                    Intent intent = new Intent(this, ProductsDetailsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("produit", p);

                    intent.putExtras(bundle);

                    PendingIntent contentIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);


                    builder.setContentIntent(contentIntent);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(LocationServices.this);
                    notificationManager.notify(i, builder.build());

                }
//                p.setSeenMark(this, Produit.UNSEEN);
            }
        } catch (Exception ex) {
            Toast.makeText(LocationServices.this, "Erreur !", Toast.LENGTH_LONG).show();
        }
    }
}
