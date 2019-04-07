package com.example.marouane.gestionstockmysql;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

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
import Models.Store;

public class StoreProductsActivity extends AppCompatActivity {

    ListView prodsLv;
    TextView storeTitle;
    ImageView storeLogo;

    ArrLstAdapt adpt;
    ArrayList<Produit> allProds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_products);
        getSupportActionBar().hide();

        prodsLv = findViewById(R.id.products_lv);
        storeTitle = findViewById(R.id.item_title);
        storeLogo = findViewById(R.id.item_image);

        Store store = getIntent().getParcelableExtra("store");

        storeTitle.setText(store.getName());
        String imageUrl = LoginActivity.serverIP + "/resources/images/" + store.getLogo();
        loadImageToImageView(imageUrl, storeLogo);


        prodsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Produit pd = allProds.get(position);

                Intent intent = new Intent(StoreProductsActivity.this, ProductsDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("produit", pd);

                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        ProduitConnectClass con = new ProduitConnectClass("fillProds");
        con.execute(
                LoginActivity.serverIP + "/Produit/getStoresProducts.php?id="+store.getId());

    }

    private void loadImageToImageView(String url, ImageView imageView) {
        final String u = url;
        System.out.println("URL >> " + u);
        Picasso.with(this).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.placeholder)
                .transform(new CircleTransform())
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        Toast.makeText(ProductsDetailsActivity.this, "L'image du produit est >> " + u, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(StoreProductsActivity.this, "Une erreur lors du chargement de l'image.", Toast.LENGTH_LONG).show();
                    }
                });
    }


    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size/2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
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
            pDialog = new ProgressDialog(StoreProductsActivity.this);
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
                Toast.makeText(StoreProductsActivity.this, "Une erreur s'est produite, ressayer plus tard..." + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(StoreProductsActivity.this, "Erreur !", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}
