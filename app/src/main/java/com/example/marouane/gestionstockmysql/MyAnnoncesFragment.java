package com.example.marouane.gestionstockmysql;

import android.app.ProgressDialog;
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
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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

import Models.Annonceur;
import Models.Produit;

public class MyAnnoncesFragment extends Fragment {

    Annonceur annonceur;

    ListView prodsLv;
    ArrLstAdapt adpt;
    ArrayList<Produit> allProds;

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.my_annonces_fragment, container, false);

        // Récupération de la liste des annonces appartenant à l'utilisateur connecté
        allProds = new ArrayList<>();

        prodsLv = v.findViewById(R.id.products_lv);

        ProduitConnectClass con = new ProduitConnectClass("fillProds");
        if (annonceur != null) {
            con.execute(
                    "http://" + LoginActivity.serverIP + "/GestionStock/Produit/getAnnonceurAnnonces.php?id=" + annonceur.getIdAnnonceur() + "");
        } else {
            Toast.makeText(v.getContext(), "Une erreur s'est produite, ressayer plus tard...", Toast.LENGTH_LONG).show();
        }

        prodsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Informations")
                        .setMessage("Que voulez-vous faire ?")
                        .setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(v.getContext(), "L'id du produit qui doit être modifer est >> " + allProds.get(position).getIdProd(), Toast.LENGTH_LONG).show();

                                EditAnnonceFragment editAnnonce = new EditAnnonceFragment();
                                editAnnonce.setAnnonceur(annonceur);
                                editAnnonce.setProd(allProds.get(position));

                                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container, editAnnonce);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        })
                        .setNegativeButton("Supprimer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                builder.setTitle("Informations")
                                        .setMessage("Voulez vous vraiment supprimer cette annonce ?")
                                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Instantiate the RequestQueue.
                                                RequestQueue queue = Volley.newRequestQueue(v.getContext());
                                                String url = "http://" + LoginActivity.serverIP + "/GestionStock/Produit/delete.php?id=" + allProds.get(position).getIdProd();

                                                // Request a string response from the provided URL.
                                                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                                        new Response.Listener<String>() {
                                                            @Override
                                                            public void onResponse(String response) {
                                                                if (response.equals("0")) {
                                                                    Toast.makeText(v.getContext(), "Annonce a été supprimée avec succés.", Toast.LENGTH_LONG).show();
                                                                    allProds.remove(position);
                                                                    adpt.notifyDataSetChanged();
                                                                } else {
                                                                    Toast.makeText(v.getContext(), "Une erreur s'est produite, merci de ressayer plus tard.", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        Toast.makeText(v.getContext(), "Une erreur s'est produite, merci de ressayer plus tard.", Toast.LENGTH_LONG).show();
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
                    allRecords.add(new Produit(object.getInt("id"), object.getInt("prix"), object.getInt("qteStock"), object.getString("nom"), object.getString("description"), object.getString("images"), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(object.getString("date_modif"))));
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
            System.out.println("From the server >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + returnedValue);
            return returnedValue;
        }
    }

    public void fillTheProdsList(ArrayList<Produit> allRecords) {
        try {
            allProds = allRecords;
            adpt = new ArrLstAdapt(v.getContext(), R.layout.product_row, allProds);
            prodsLv.setAdapter(adpt);
            adpt.notifyDataSetChanged();
        } catch (Exception ex) {
            Toast.makeText(v.getContext(), "Erreur !", Toast.LENGTH_LONG).show();
        }
    }

    public void setAnnonceur(Annonceur annonceur) {
        this.annonceur = annonceur;
    }
}
