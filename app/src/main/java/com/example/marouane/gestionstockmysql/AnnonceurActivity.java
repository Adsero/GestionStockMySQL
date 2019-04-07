package com.example.marouane.gestionstockmysql;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import Models.Annonceur;
import Models.Store;

public class AnnonceurActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private Annonceur annonceur;
    private Store store;


    ImageView logo;
    TextView title;
    TextView details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonceur);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        annonceur = (Annonceur) getIntent().getParcelableExtra("annonceur");


        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_my_annonces:
                        MyAnnoncesFragment myAnnonces = new MyAnnoncesFragment();
                        myAnnonces.setAnnonceur(annonceur);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myAnnonces).commit();
                        break;

                    case R.id.nav_add_annonce:
                        AddAnnonceFragment addAnnonce = new AddAnnonceFragment();
                        addAnnonce.setAnnonceur(annonceur);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, addAnnonce).commit();
                        break;

                    case R.id.nav_profile:
                        EditProfileFragment editProfileFragment= new EditProfileFragment();
                        editProfileFragment.setAnnonceur(annonceur);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editProfileFragment).commit();
                        break;

                    case R.id.nav_edit_zone:
                        DetectionZoneFragment detectZoneFragment= new DetectionZoneFragment();
                        detectZoneFragment.setAnnonceur(annonceur);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, detectZoneFragment).commit();
                        break;

                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        View header = navigationView.getHeaderView(0);

        logo = header.findViewById(R.id.logo);
        title = header.findViewById(R.id.title);
        details = header.findViewById(R.id.details);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = LoginActivity.serverIP + "/Annonceur/getOwnersMagasin.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Convert data to a list of clients
                        System.out.println("||||||||" + response);
                        try {
                            JSONArray all = new JSONArray(response);
                            for (int i = 0; i < all.length(); i++) {
                                JSONObject object = all.getJSONObject(i);
                                store = new Store(object.getInt("id_magasin"), object.getString("libelle"), object.getString("logo"), object.getString("emplacement_geo"), new Annonceur(object.getInt("proprietaire")), object.getInt("zone_detection"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Une erreur emis par le serveur >> " + response, Toast.LENGTH_LONG).show();
                            System.out.println("Une erreur emis par le serveur >> " + response);
                        }
                        initStoreInfo();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Une erreur s'est produite, merci de ressayer plus tard.", Toast.LENGTH_LONG).show();
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            MyAnnoncesFragment myAnnonces = new MyAnnoncesFragment();
            myAnnonces.setAnnonceur(annonceur);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myAnnonces).commit();
        }
    }

    public void initStoreInfo() {
        try {
            String imageUrl = LoginActivity.serverIP + "/resources/images/" + store.getLogo();
            loadImageToImageView(imageUrl, logo);

            title.setText(store.getName());
            details.setText(String.format("%s %s", annonceur.getPrenom().toUpperCase(), annonceur.getNom()));
        } catch (Exception e) {
            System.out.println("||||||||||||||||||||||| >> " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Une erreur s'est produite lors du chargement du contenu, merci de ressayer plus tard.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void loadImageToImageView(String url, ImageView imageView) {
        Picasso.with(this).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.placeholder)
                .transform(new CircleTransform())
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Toast.makeText(getApplicationContext(), "Une erreur sur le chargement de l'image.", Toast.LENGTH_LONG).show();
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
}
