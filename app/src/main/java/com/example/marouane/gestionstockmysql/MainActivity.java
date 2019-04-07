package com.example.marouane.gestionstockmysql;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marouane.gestionstockmysql.admin.AllStoresFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import Models.Annonceur;
import Models.Produit;
import Models.Store;

public class MainActivity extends AppCompatActivity {
    TextView annonceurEspace;
    ListView prodsLv;
    RecyclerView recyclerView;

    ImageView affPanier;

    ArrLstAdapt adpt;
    ArrayList<Produit> allProds;
    ArrayList<Store> stores;

    private StoreRecyclerViewAdapter adapter;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        getSupportActionBar().hide();

        annonceurEspace = findViewById(R.id.tview);
        prodsLv = findViewById(R.id.products_lv);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        affPanier = findViewById(R.id.affPanier);

        allProds = new ArrayList<>();

        ProduitConnectClass con = new ProduitConnectClass("fillProds");
        con.execute(
                LoginActivity.serverIP + "/Produit/getAll.php");

        prodsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Produit pd = allProds.get(position);

                Intent intent = new Intent(MainActivity.this, ProductsDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("produit", pd);
//                bundle.putParcelable("annonceur", annonceur);

                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        annonceurEspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                startActivityForResult(intent, 3);

            }
        });

        if (!runtime_permissions())
            startService();

        ConnectClass conn = new ConnectClass("fill");
        conn.execute(
                LoginActivity.serverIP + "/Magasin/getAll.php");

    }

    private void startService() {
        Intent i = new Intent(getApplicationContext(), LocationServices.class);
        ContextCompat.startForegroundService(this, i);
//        startService(i);
//        stopService(i);
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                runtime_permissions();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
//                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
//                            .setSmallIcon(R.drawable.carticon)
//                            .setContentTitle("Coordinates")
//                            .setContentText(intent.getExtras().get("coordinates").toString())
//                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
//                    notificationManager.notify(0, builder.build());
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3 && resultCode == 4) {
            finish();
            startActivity(getIntent());
        }
    }

    class ProduitConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Produit> allRecords = new ArrayList<>();
        ProgressDialog pDialog;
        String choice;

        public ProduitConnectClass(String choice) {
            this.choice = choice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Chargement...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
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
                    System.out.println(allRecords);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "Une erreur s'est produite, ressayer plus tard..." + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();

            switch (choice) {
                case "fillProds":
                    fillTheProdsList(allRecords);
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

    public void fillTheProdsList(ArrayList<Produit> allRecords) {
        try {
            allProds = allRecords;
            adpt = new ArrLstAdapt(this, R.layout.product_row, allProds);
            prodsLv.setAdapter(adpt);
            adpt.notifyDataSetChanged();
        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, "Erreur !", Toast.LENGTH_LONG).show();
        }
    }

    // Store AsyncTask

    class ConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Store> allRecords = new ArrayList<>();
        String choice;

        public ConnectClass(String choice) {
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
                    allRecords.add(new Store(object.getInt("id_magasin"), object.getString("libelle"), object.getString("logo"), object.getString("emplacement_geo"), new Annonceur(object.getInt("proprietaire")), object.getInt("zone_detection")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("SERVER RESPONSE >> " + s);
                Toast.makeText(getApplicationContext(), "Erreur !" + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            switch (choice) {
                case "fill":
                    fillTheList(allRecords);
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
            System.out.println("From the server >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + returnedValue);
            return returnedValue;
        }
    }

    public void fillTheList(ArrayList<Store> allRecords) {
        try {
            stores = allRecords;
            adapter = new StoreRecyclerViewAdapter(this, stores);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Erreur !", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }
}
