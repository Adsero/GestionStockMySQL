package com.example.marouane.gestionstockmysql;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.marouane.gestionstockmysql.AddAnnonceFragment;
import com.example.marouane.gestionstockmysql.LoginActivity;
import com.example.marouane.gestionstockmysql.MapsActivity;
import com.example.marouane.gestionstockmysql.MyAnnoncesFragment;
import com.example.marouane.gestionstockmysql.R;
import com.example.marouane.gestionstockmysql.RegisterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Models.Annonceur;
import Models.Store;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {

    private View v;

    EditText name, lastName, login, pwd, pwd2nd;
    Button validate;

    Annonceur annonceur;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_edit_profile, container, false);

        name = v.findViewById(R.id.name);
        lastName = v.findViewById(R.id.lastName);
        login = v.findViewById(R.id.login);
        pwd = v.findViewById(R.id.pass);
        pwd2nd = v.findViewById(R.id.pass2ndTime);

        validate = v.findViewById(R.id.btnAddUser);

        // Fill the form with the existing user info
        name.setText(annonceur.getNom());
        lastName.setText(annonceur.getPrenom());
        login.setText(annonceur.getLogin());

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!login.getText().toString().equals("") &&
                        !pwd.getText().toString().equals("") &&
                        !pwd2nd.getText().toString().equals("")) {
                    try {
                        annonceur = new Annonceur(annonceur.getIdAnnonceur(), name.getText().toString(), lastName.getText().toString(), new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01"), login.getText().toString().trim(), pwd.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (pwd.getText().toString().equals(pwd2nd.getText().toString())) {
                        new ClientConnectClass("updateProfile")
                                .execute(LoginActivity.serverIP + "/Annonceur/update.php?" +
                                        "id=" + annonceur.getIdAnnonceur() +
                                        "&name=" + annonceur.getNom() +
                                        "&lastname=" + annonceur.getPrenom() +
                                        "&login=" + annonceur.getLogin() +
                                        "&pass=" + annonceur.getPass());
                        System.out.println(LoginActivity.serverIP + "/Annonceur/update.php?id=" + annonceur.getIdAnnonceur() + "&name=" + annonceur.getNom() + "&lastname=" + annonceur.getPrenom() + "&login=" + annonceur.getLogin() + "&pass=" + annonceur.getPass());
                    } else {
                        Toast.makeText(getContext(), "La confirmation de votre pass a été échoué !", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Vous devez remplir tout les champs !", Toast.LENGTH_LONG).show();
                }
            }
        });

        return v;
    }

    class ClientConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Annonceur> allRecords = new ArrayList<>();
        ProgressDialog pDialog;
        String choice;

        ClientConnectClass(String choice) {
            this.choice = choice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Chargement...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();

            MyAnnoncesFragment myAnnonces = new MyAnnoncesFragment();
            myAnnonces.setAnnonceur(annonceur);

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, myAnnonces);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
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

    public void setAnnonceur(Annonceur annonceur) {
        this.annonceur = annonceur;
    }
}
