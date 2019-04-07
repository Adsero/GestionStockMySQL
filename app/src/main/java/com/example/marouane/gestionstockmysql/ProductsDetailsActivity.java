package com.example.marouane.gestionstockmysql;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

public class ProductsDetailsActivity extends AppCompatActivity {

    private ImageView imgProd;
    private TextView nom;
    private TextView prix;
    private TextView desc;
    private TextView timeAgo;
    private TextView publishedBy;

    Produit pd = null;
    Annonceur annonceur = null;
    int indexOfProdInTheCart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_detail_enhanced);
        getSupportActionBar().hide();

        imgProd = (ImageView) findViewById(R.id.imgPrd);
        nom = findViewById(R.id.nomTv);
        prix = findViewById(R.id.prixTv);
        desc = findViewById(R.id.descTv);
        timeAgo = findViewById(R.id.timeAgo);
        publishedBy = findViewById(R.id.publishedBy);

        pd = getIntent().getParcelableExtra("produit");
        annonceur = getIntent().getParcelableExtra("annonceur");
        indexOfProdInTheCart = getPordIndexIfExistsOnCart(pd);

        System.out.println(pd);

        pd.setSeenMark(this, Produit.SEEN);
        ProduitConnectClass con = new ProduitConnectClass("fillOneProduct");
        con.execute(
                LoginActivity.serverIP + "/Produit/getOne.php?id=" + pd.getIdProd());

        String imageUrl = LoginActivity.serverIP + "/resources/images/" + pd.getImages();

        loadImageToImageView(imageUrl, imgProd);

    }

    public int getPordIndexIfExistsOnCart(Produit p) {
        int index = -1;
        for (int i = 0; i < LoginActivity.panier.size(); i++) {
            if (LoginActivity.panier.get(i).equals(p)) {
                index = i;
//                Toast.makeText(ProductsDetailsActivity.this, "Rahna l9ina had lproduit fvotre panier !", Toast.LENGTH_LONG).show();
            }
        }
        return index;
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
            pDialog = new ProgressDialog(ProductsDetailsActivity.this);
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
                    publishedBy.setText(object.getString("libelle"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            switch (choice) {
                case "fillOneProduct":
                    fillOneProduct(allRecords);
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

    public void fillOneProduct(ArrayList<Produit> allRecords) {
        try {

            nom.setText(pd.getNom());
            prix.setText("MAD " + pd.getPrix());
            desc.setText(pd.getDesc());
            timeAgo.setText(pd.timeAgo());

            System.out.println(pd);
//            imgProd.setImageResource(getResources().getIdentifier(pd.getImages().toString(), "drawable", getPackageName()));
        } catch (Exception ex) {
            Toast.makeText(ProductsDetailsActivity.this, "Erreur >>>>>>>>> " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public void loadImageToImageView(String url, ImageView imageView) {
        final String u = url;
        System.out.println("URL >> " + u);
        Picasso.with(ProductsDetailsActivity.this).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.placeholder)
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        Toast.makeText(ProductsDetailsActivity.this, "L'image du produit est >> " + u, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(ProductsDetailsActivity.this, "Une erreur lors du chargement de l'image.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
