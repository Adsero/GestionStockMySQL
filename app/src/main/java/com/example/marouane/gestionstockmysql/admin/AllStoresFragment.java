package com.example.marouane.gestionstockmysql.admin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.marouane.gestionstockmysql.EditAnnonceFragment;
import com.example.marouane.gestionstockmysql.LoginActivity;
import com.example.marouane.gestionstockmysql.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import Models.Annonceur;
import Models.Store;

public class AllStoresFragment extends Fragment {

    private View v;

    ListView storesLv;
    ArrLstAdapt adpt;
    ArrayList<Store> allStores;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_allstores, container, false);

        storesLv = v.findViewById(R.id.stores_lv);
        allStores = new ArrayList<>();

        ConnectClass con = new ConnectClass("fill");
        con.execute(
                LoginActivity.serverIP + "/Magasin/getAll.php");


        storesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Informations")
                        .setMessage("Que voulez-vous faire ?")
                        .setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditStoreFragment editStore = new EditStoreFragment();
                                editStore.setStore(allStores.get(position));

                                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container, editStore);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        })
                        .setNegativeButton("Supprimer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                builder.setTitle("Informations")
                                        .setMessage("Voulez vous vraiment supprimer ce magasin ?")
                                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Instantiate the RequestQueue.
                                                RequestQueue queue = Volley.newRequestQueue(v.getContext());
                                                String url = LoginActivity.serverIP + "/Magasin/delete.php?id=" + allStores.get(position).getId();

                                                // Request a string response from the provided URL.
                                                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                                        new Response.Listener<String>() {
                                                            @Override
                                                            public void onResponse(String response) {
                                                                if (response.equals("0")) {
                                                                    Toast.makeText(v.getContext(), "Magasin a été supprimée avec succés.", Toast.LENGTH_LONG).show();
                                                                    allStores.remove(position);
                                                                    adpt.notifyDataSetChanged();
                                                                } else {
                                                                    Toast.makeText(v.getContext(), "Une erreur s'est produite, merci de ressayer plus tard.", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        Toast.makeText(v.getContext(), "Une erreur s'est produite, merci de ressayer plus tard." + error, Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                                // Add the request to the RequestQueue.
                                                queue.add(stringRequest);
                                            }
                                        })
                                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();
            }
        });


        return v;
    }

    class ConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Store> allRecords = new ArrayList<>();
        ProgressDialog pDialog;
        String choice;

        public ConnectClass(String choice) {
            this.choice = choice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(v.getContext());
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
                    allRecords.add(new Store(object.getInt("id_magasin"), object.getString("libelle"), object.getString("logo"), object.getString("emplacement_geo"), new Annonceur(object.getInt("proprietaire")), object.getInt("zone_detection")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();

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
            allStores = allRecords;
            adpt = new ArrLstAdapt(v.getContext(), R.layout.store_row, allStores);
            storesLv.setAdapter(adpt);
            adpt.notifyDataSetChanged();
        } catch (Exception ex) {
            Toast.makeText(v.getContext(), "Erreur !", Toast.LENGTH_LONG).show();
        }
    }

    public class ArrLstAdapt extends ArrayAdapter<Store> {

        private int resource;
        private Context context;
        private ArrayList<Store> listAllStores;

        public ArrLstAdapt(@NonNull Context context, int resource, @NonNull ArrayList<Store> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
            this.listAllStores = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Jib lview
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View row = inflater.inflate(resource, parent, false);

            // khdem 3liha
            TextView nom = row.findViewById(R.id.name);
            TextView desc = row.findViewById(R.id.description);
            ImageView img = row.findViewById(R.id.img);

            Store store = listAllStores.get(position);

            nom.setText(store.getName());
            desc.setText(store.getGeo());

            String imageUrl = LoginActivity.serverIP + "/resources/images/" + store.getLogo();
            loadImageToImageView(imageUrl, img);

            return row;
        }

        public void loadImageToImageView(String url, ImageView imageView) {
            Picasso.with(context).load(url).placeholder(R.mipmap.ic_launcher)
                    .error(R.drawable.placeholder)
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    });
        }
    }
}
