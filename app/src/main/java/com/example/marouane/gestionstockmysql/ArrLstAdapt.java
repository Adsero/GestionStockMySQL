package com.example.marouane.gestionstockmysql;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Models.Produit;

/**
 * Created by root on 22/03/18.
 */

public class ArrLstAdapt extends ArrayAdapter<Produit> {

    private int resource;
    private Context context;
    private ArrayList<Produit> listAllProds;

    public ArrLstAdapt(@NonNull Context context, int resource, @NonNull ArrayList<Produit> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.listAllProds = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Jib lview
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(resource, parent, false);

        // khdem 3liha
        TextView nom = row.findViewById(R.id.designation);
        TextView desc = row.findViewById(R.id.desc);
        TextView prix = row.findViewById(R.id.prix);
        TextView qte = row.findViewById(R.id.qte);
        ImageView img = row.findViewById(R.id.img);

        Produit pd = listAllProds.get(position);

        nom.setText(pd.getNom());
        desc.setText(pd.getDesc());
        prix.setText("MAD " + pd.getPrix());
        qte.setText(pd.timeAgo());

        String imageUrl = "http://" + LoginActivity.serverIP + "/GestionStock/resources/images/" + pd.getImages();
        loadImageToImageView(imageUrl, img);

        return row;
    }

    public void loadImageToImageView(String url, ImageView imageView) {
        Picasso.with(context).load(url).placeholder(R.mipmap.ic_launcher)
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
}