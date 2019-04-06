package com.example.marouane.gestionstockmysql;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;

import Models.Annonceur;
import Models.Produit;

public class LoginActivity extends AppCompatActivity {

    // Specifier ici le serveur contenant les script PHP
    public final static String serverIP = "192.168.1.7";

    public static ArrayList<Produit> panier = new ArrayList<>();

    Button btnLogin, btnRegister;
    EditText login, pass;
    private final String ADMIN_LOGIN = "admin";
    private final String ADMIN_PASS = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        login = findViewById(R.id.input_login);
        pass = findViewById(R.id.input_password);

        login.setText("mag");
        pass.setText("m");

        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!login.getText().toString().equals("") && !pass.getText().toString().equals("")) {

                    if (login.getText().toString().equals(ADMIN_LOGIN) && pass.getText().toString().equals(ADMIN_PASS)) {
                        Intent adminIntent = new Intent(LoginActivity.this, AdminActivity.class);
                        startActivity(adminIntent);
                    }
                    {
                        Annonceur cli = new Annonceur();
                        cli.setLogin(login.getText().toString().trim());
                        cli.setPass(pass.getText().toString());

                        ClientConnectClass con = new ClientConnectClass("login");
                        con.execute(
                                "http://" + serverIP + "/GestionStock/Annonceur/getUserByLoginAndPass.php?login=" + cli.getLogin() + "&pass=" + cli.getPass());

                    }
                } else {
                    Snackbar.make(v, "Vous devez remplir tout les champs.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

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
            pDialog = new ProgressDialog(LoginActivity.this);
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
                    allRecords.add(new Annonceur(object.getInt("id"), object.getString("nom"), object.getString("prenom"), new SimpleDateFormat("yyyy-MM-dd").parse(object.getString("date_naiss")), object.getString("login"), object.getString("pass")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                Toast.makeText(LoginActivity.this, "Une erreur s'est produite, ressayer plus tard...", Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();

            switch (choice) {
                case "login":
                    handleTheLoginStuff(allRecords);
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

    public void handleTheLoginStuff(ArrayList<Annonceur> allRecords) {

        try {
            Annonceur annonceur = allRecords.get(0);
            if (annonceur != null) {

                Intent productsIntent = new Intent(LoginActivity.this, AnnonceurActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("annonceur", annonceur);
                productsIntent.putExtras(bundle);

                startActivity(productsIntent);

            }

        } catch (NullPointerException ex) {
            Toast.makeText(LoginActivity.this, "Error >>>>>" + ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IndexOutOfBoundsException exi) {

            Toast.makeText(LoginActivity.this, "Couldn't connect, user not found ! ", Toast.LENGTH_LONG).show();
        }
    }
}
