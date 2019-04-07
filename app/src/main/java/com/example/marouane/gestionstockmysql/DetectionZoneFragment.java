package com.example.marouane.gestionstockmysql;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.marouane.gestionstockmysql.LoginActivity;
import com.example.marouane.gestionstockmysql.R;
import com.example.marouane.gestionstockmysql.admin.AllStoresFragment;
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
import java.util.HashMap;
import java.util.Map;

import Models.Annonceur;
import Models.Store;

public class DetectionZoneFragment extends Fragment {

    private View v;
    private Button editZone, validate;
    private TextView zone;

    private Annonceur annonceur;
    private Store store;

    private ProgressDialog pDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_detect_zone, container, false);

        editZone = v.findViewById(R.id.btnEditZone);
        validate = v.findViewById(R.id.btnValidate);

        zone = v.findViewById(R.id.zone);

        pDialog = new ProgressDialog(v.getContext());
        pDialog.setMessage("Chargement...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        initStoreInfo();

        editZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NumberPicker picker = new NumberPicker(getContext());
                picker.setMinValue(1);
                picker.setMaxValue(50);
                picker.setValue(store.getDetectionZone());

                picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                        zone.setText(String.valueOf(newVal));
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(picker);
                builder.setTitle("Choisir le diamètre du zone de détection en Km.")
                        .setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.show();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!zone.getText().toString().equals("")) {
                    handleZoneStuff();
                } else {
                    Snackbar.make(v, "Vous devez fournir une valeur svp.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });


        return v;
    }

    private void handleZoneStuff() {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Informations")
                .setMessage("Voulez vous vraiment modifier la zone de détection du magasin ?")
                .setPositiveButton("Oui, je confirme.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pDialog = new ProgressDialog(v.getContext());
                        pDialog.setMessage("Chargement...");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(true);
                        pDialog.show();

                        // Instantiate the RequestQueue.
                        RequestQueue queue = Volley.newRequestQueue(v.getContext());
                        String url = LoginActivity.serverIP + "/Magasin/updateZone.php";

                        // Request a string response from the provided URL.
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (response.equals("0")) {
                                            Toast.makeText(v.getContext(), "Le Magasin a été modifié avec succés.", Toast.LENGTH_LONG).show();
                                            MyAnnoncesFragment myAnnonces = new MyAnnoncesFragment();
                                            myAnnonces.setAnnonceur(annonceur);
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myAnnonces).commit();
                                        } else {
                                            Toast.makeText(v.getContext(), "Une erreur émise par le serveur >>" + response, Toast.LENGTH_LONG).show();
                                        }
                                        pDialog.dismiss();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(v.getContext(), "Une erreur s'est produite, merci de ressayer plus tard.", Toast.LENGTH_LONG).show();
                                pDialog.dismiss();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("id", String.valueOf(store.getId()));
                                params.put("zone", String.valueOf(zone.getText()));

                                return params;
                            }
                        };

                        // Add the request to the RequestQueue.
                        queue.add(stringRequest);
                    }
                })
                .setNegativeButton("Non.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();

    }

    private void initStoreInfo() {


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = LoginActivity.serverIP + "/Annonceur/getOwnersMagasin.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Convert data to a list of clients
                        try {
                            JSONArray all = new JSONArray(response);
                            for (int i = 0; i < all.length(); i++) {
                                JSONObject object = all.getJSONObject(i);
                                store = new Store(object.getInt("id_magasin"), object.getString("libelle"), object.getString("logo"), object.getString("emplacement_geo"), new Annonceur(object.getInt("proprietaire")), object.getInt("zone_detection"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Une erreur emis par le serveur >> " + response, Toast.LENGTH_LONG).show();
                            System.out.println("Une erreur emis par le serveur >> " + response);
                        }

                        zone.setText(String.valueOf(store.getDetectionZone()));
                        pDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Une erreur s'est produite, merci de ressayer plus tard.", Toast.LENGTH_LONG).show();
                pDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", String.valueOf(annonceur.getIdAnnonceur()));

                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void setAnnonceur(Annonceur annonceur) {
        this.annonceur = annonceur;
    }
}
