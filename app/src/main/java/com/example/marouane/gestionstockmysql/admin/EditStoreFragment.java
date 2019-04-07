package com.example.marouane.gestionstockmysql.admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.example.marouane.gestionstockmysql.LoginActivity;
import com.example.marouane.gestionstockmysql.MapsActivity;
import com.example.marouane.gestionstockmysql.R;
import com.example.marouane.gestionstockmysql.RegisterActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import Models.Annonceur;
import Models.Store;

import static android.app.Activity.RESULT_OK;

public class EditStoreFragment extends Fragment {

    private View v;

    private ImageView logo;
    private EditText name;
    private Button validate, selectMap, assignOwner;
    private final int GET_GEO_REQUEST = 1;
    private final int ASSIGN_OWNER_REQUEST = 11;

    private Annonceur annonceur;
    private Store store;

    ProgressDialog pDialog;

    private Bitmap bitmap;
    private final int IMG_REQUEST = 111;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_editstore, container, false);

        logo = v.findViewById(R.id.logo);
        name = v.findViewById(R.id.name);
        selectMap = v.findViewById(R.id.btnGetGeo);
        validate = v.findViewById(R.id.btnAdd);
//        assignOwner = v.findViewById(R.id.btnAssignOwner);

        annonceur = null;

        String imageUrl = LoginActivity.serverIP + "/resources/images/" + store.getLogo();
        loadImageToImageView(imageUrl, logo);

        name.setText(store.getName());


        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(v.getContext(), "Selecting image...", Toast.LENGTH_LONG).show();
                selectImage();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                store.setOwner(annonceur);
                store.setName(name.getText().toString());

                if (store.getName() != null && store.getLogo() != null && store.getGeo() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Informations")
                            .setMessage("Voulez vous vraiment enregistrer les changement effectués ?")
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
                                    String url = LoginActivity.serverIP + "/Magasin/update.php";

                                    // Request a string response from the provided URL.
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    if (response.equals("0")) {
                                                        Toast.makeText(v.getContext(), "Le Magasin a été modifié avec succés.", Toast.LENGTH_LONG).show();
                                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AllStoresFragment()).commit();
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
                                            params.put("name", String.valueOf(store.getName()));
                                            params.put("geo", String.valueOf(store.getGeo()));

                                            String imageParam = "";
                                            if (bitmap != null) {
                                                imageParam = imageToString(bitmap);
                                            } else {
                                                imageParam = imageToString(((BitmapDrawable) logo.getDrawable()).getBitmap());
                                            }
                                            params.put("logo", String.valueOf(imageParam));
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
                } else {
                    Snackbar.make(v, "Vérifer que vous avez fournis toutes les informations nécessaires.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        selectMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                startActivityForResult(intent, GET_GEO_REQUEST);
            }
        });

//        assignOwner.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(v.getContext(), RegisterActivity.class);
//                startActivityForResult(intent, ASSIGN_OWNER_REQUEST);
//            }
//        });

        return v;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    public void loadImageToImageView(String url, ImageView imageView) {
        Picasso.with(getContext()).load(url).placeholder(R.mipmap.ic_launcher)
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GEO_REQUEST && resultCode == RESULT_OK && data != null) {
            String lat = data.getStringExtra("lat");
            String lng = data.getStringExtra("lng");
            selectMap.setBackgroundColor(getResources().getColor(R.color.colorSecondary));

            store.setGeo(lat + "," + lng);

            Snackbar.make(v, "L'emplacement géographique du magasin a été bien déterminé", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else if (requestCode == ASSIGN_OWNER_REQUEST && resultCode == RESULT_OK && data != null) {
            annonceur = data.getParcelableExtra("owner");
            Snackbar.make(v, annonceur.toString(), Snackbar.LENGTH_LONG).setAction("Action", null).show();

            assignOwner.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
        } else if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(v.getContext().getContentResolver(), path);
                logo.setImageBitmap(bitmap);
                store.setLogo(String.valueOf(imageToString(bitmap)));
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Erreur >> " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }
}
