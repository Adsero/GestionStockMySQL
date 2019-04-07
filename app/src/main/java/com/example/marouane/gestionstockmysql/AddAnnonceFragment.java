package com.example.marouane.gestionstockmysql;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import Models.Annonceur;
import Models.Produit;
import Models.Store;

import static android.app.Activity.RESULT_OK;

public class AddAnnonceFragment extends Fragment {

    Annonceur annonceur;
    Store store;
    Produit prod;
    View v;

    private ImageView imgProd;
    private EditText nom, prix, desc, stockQuantity;
    private Button btnEdit;

    private final int IMG_REQUEST = 1;
    private Bitmap bitmap;

    ProgressDialog pDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.add_annonce_fragment, container, false);

        imgProd = v.findViewById(R.id.imgPrd);
        nom = v.findViewById(R.id.nomEdit);
        prix = v.findViewById(R.id.prixEdit);
        desc = v.findViewById(R.id.descEdit);

        btnEdit = v.findViewById(R.id.btnAdd);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Informations")
                        .setMessage("Voulez vous vraiment publier cette annonce ?")
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
                                String url = LoginActivity.serverIP + "/Produit/add.php";

                                // Request a string response from the provided URL.
                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                if (response.equals("0")) {
                                                    Toast.makeText(v.getContext(), "Votre annonce a été publiée avec succés.", Toast.LENGTH_LONG).show();
                                                    MyAnnoncesFragment myAnnonces = new MyAnnoncesFragment();
                                                    myAnnonces.setAnnonceur(annonceur);

                                                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                    fragmentTransaction.replace(R.id.fragment_container, myAnnonces);
                                                    fragmentTransaction.addToBackStack(null);
                                                    fragmentTransaction.commit();
                                                } else {
                                                    Toast.makeText(v.getContext(), "Une erreur émise par le serveur >>" + response, Toast.LENGTH_LONG).show();
                                                    System.out.println("Response >> " + response);
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
                                        params.put("annonceur_id", String.valueOf(annonceur.getIdAnnonceur()));
                                        params.put("nom", String.valueOf(nom.getText()));
                                        params.put("prix", String.valueOf(prix.getText()));
                                        params.put("description", String.valueOf(desc.getText()));
                                        params.put("store", String.valueOf(store.getId()));
                                        params.put("images", String.valueOf(imageToString(bitmap)));

                                        return params;
                                    }
                                };

//                                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
//                                        60000,
//                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//                                ));

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
        });

        imgProd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(v.getContext(), "Selecting image...", Toast.LENGTH_LONG).show();
                selectImage();
            }
        });

        return v;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(v.getContext().getContentResolver(), path);
                imgProd.setImageBitmap(bitmap);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Erreur >> " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setAnnonceur(Annonceur annonceur) {
        this.annonceur = annonceur;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }
}
