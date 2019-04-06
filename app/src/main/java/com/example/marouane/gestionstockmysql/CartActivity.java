package com.example.marouane.gestionstockmysql;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import Models.Annonceur;
import Models.Commande;
import Models.Produit;

public class CartActivity extends AppCompatActivity {

    private ListView listView;
    private TextView total, prodsNbr, finalTotal;
    private Button btnValidate;
    
    ArrayList<Produit> panier = LoginActivity.panier;
    private ArrayList<Produit> allProds;
    private Annonceur annonceur;

    double totalValue = 0;
    int nbrProds = 0;
    double totalFinale;

    int nbrCmd = 0;

    ArrLstAdapt adpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_cart);
        getSupportActionBar().hide();

        listView = findViewById(R.id.products_lv);
        total = findViewById(R.id.total);
        prodsNbr = findViewById(R.id.nbrProd);
        finalTotal = findViewById(R.id.finalTotal);

        btnValidate = findViewById(R.id.validate);

        adpt = new ArrLstAdapt(this, R.layout.product_row, panier);
        listView.setAdapter(adpt);
        adpt.notifyDataSetChanged();

        if (!panier.isEmpty()) {
            for (Produit produit : panier) {
                nbrProds += produit.getQte();
                totalValue += produit.getPrix() * produit.getQte();
            }
        }

        prodsNbr.setText("" + nbrProds);
        total.setText("$ " + String.format("%.2f", totalValue));

        annonceur = (Annonceur) getIntent().getParcelableExtra("annonceur");

        ProduitConnectClass PrdCon = new ProduitConnectClass("fillProds");
        PrdCon.execute(
                "http://"+ LoginActivity.serverIP+"/GestionStock/Produit/getAll.php");

        CommandeConnectClass con = new CommandeConnectClass("fillClientCmds");
        con.execute(
                "http://"+ LoginActivity.serverIP+"/GestionStock/Commande/getClientCmds.php?idC=" + annonceur.getIdAnnonceur());

        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                builder.setTitle("Confirmation.")
                        .setMessage("Voulez vous vraiment valider votre commande ?")
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!panier.isEmpty()) {
                                    Toast.makeText(CartActivity.this, "Tberkat lboutona dyal <<validate>>", Toast.LENGTH_LONG).show();
                                    handleCart();
                                } else {
                                    Toast.makeText(CartActivity.this, "Votre panier est vide !!", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Non Merci.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                builder.setTitle("Informations")
                        .setMessage("Que voulez-vous faire ?")
                        .setPositiveButton("Retirer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoginActivity.panier.remove(position);
                                finish();
                                startActivity(getIntent());
                            }
                        })
                        .setNegativeButton("Rien !", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

    }

    public void handleCart() {
        try {
            Produit oldPro = null;
            for (Produit p : panier) {
                for (Produit dbPrd : allProds) {
                    if (p.getIdProd() == dbPrd.getIdProd()) {
                        oldPro = dbPrd;
                    }
                }
                if (oldPro != null)
                {
                    Toast.makeText(CartActivity.this, "L'oldProd >> " + oldPro.getDesc(), Toast.LENGTH_LONG).show();
                    oldPro.setQte(oldPro.getQte() - p.getQte());
                } else {
                    Toast.makeText(CartActivity.this, "Error >> L'oldProd est null", Toast.LENGTH_LONG).show();
                }
                try {
                    oldPro.setDesc(URLEncoder.encode(oldPro.getDesc(), "UTF-8")); // rigl les espaces bach maymchiwch hakak fURL
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                new ProduitConnectClass("updatePlease").execute(
                        "http://"+ LoginActivity.serverIP+"/GestionStock/Produit/update.php?id=" + oldPro.getIdProd() + "&prix=" + oldPro.getPrix() + "&qteStock=" + oldPro.getQte() + "&nom=" + oldPro.getNom() + "&description=" + oldPro.getDesc() + "&images=" + oldPro.getImages());
                System.out.println("http://"+ LoginActivity.serverIP+"/GestionStock/Produit/update.php?id=" + oldPro.getIdProd() + "&prix=" + oldPro.getPrix() + "&qteStock=" + oldPro.getQte() + "&nom=" + oldPro.getNom() + "&description=" + oldPro.getDesc() + "&images=" + oldPro.getImages());
            }
        } catch (Exception ex) {
            Toast.makeText(CartActivity.this, "Error >> " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        validateCommande(annonceur, 002, totalFinale);
    }

    public void validateCommande(Annonceur annonceur, int idCmd, double total) {
        Commande cmd = new Commande();
        cmd.setIdCmd(idCmd);
        cmd.setAnnonceur(annonceur);
        cmd.setTotal(total);

        new CommandeConnectClass("validateCmd").execute(
                "http://"+ LoginActivity.serverIP+"/GestionStock/Commande/add.php?id=" + cmd.getIdCmd() + "&idC=" + cmd.getAnnonceur().getIdAnnonceur() + "&total=" + cmd.getTotal());

//        Toast.makeText(CartActivity.this, "Commande validée avec succés !!", Toast.LENGTH_LONG).show();
        finish();
    }

    public void fillProdsList(ArrayList<Produit> allRecords) {
        try {
            allProds = allRecords;
        } catch (Exception ex) {
            Toast.makeText(CartActivity.this, "Error >>>>>>>>> " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void fillClientCommands(ArrayList<Commande> allRecords) {
        try {
            nbrCmd = allRecords.size();
            if (nbrCmd != 0) {
                Toast.makeText(CartActivity.this, "Mer7ba w1000 mer7ba !!", Toast.LENGTH_LONG).show();
                totalFinale = totalValue * 0.7;
            } else {
                // ymchi ynini had khona ma3ndmo lwjah 3lach y7chm
                totalFinale = totalValue;
            }

            finalTotal.setText("$ " + String.format("%.2f", totalFinale));

        } catch (Exception ex) {
            Toast.makeText(CartActivity.this, "Error >>>>>>>>> " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    class ProduitConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Produit> allRecords = new ArrayList<>();
        ProgressDialog pDialog;
        String choice;

        ProduitConnectClass(String choice) {
            this.choice = choice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CartActivity.this);
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

            switch (choice) {
                case "updatePlease":
                    Toast.makeText(CartActivity.this, "Mise à jour du produit est terminé ! ", Toast.LENGTH_LONG).show();
                    System.out.println("ServerResponse >>" + s);
                    break;
                case "fillProds":
                    fillProdsList(allRecords);
                    break;

            }
            pDialog.dismiss();
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

    class CommandeConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Commande> allRecords = new ArrayList<>();
        ProgressDialog pDialog;
        String choice;

        CommandeConnectClass(String choice) {
            this.choice = choice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CartActivity.this);
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
                    allRecords.add(new Commande(object.getInt("id"), annonceur, object.getDouble("total")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (choice) {
                case "fillClientCmds":
                    fillClientCommands(allRecords);
                    break;
                case "validateCmd":
                    Toast.makeText(CartActivity.this, "Done validating the command !! ", Toast.LENGTH_LONG).show();
                    LoginActivity.panier.clear();
                    break;
                default:
                    Toast.makeText(CartActivity.this, "No CommandConnectClass Operation specified !! ", Toast.LENGTH_LONG).show();
                    break;
            }

            pDialog.dismiss();
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

}
