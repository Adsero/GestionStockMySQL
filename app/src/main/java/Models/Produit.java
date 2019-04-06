package Models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Locale;

/**
 * Created by root on 22/03/18.
 */

public class Produit implements Parcelable {
    public static final int SEEN = 1;
    public static final int UNSEEN = 0;

    private int idProd, prix, qte;
    private String nom, desc;
    private String images;
    private Date published;

    public Produit() {
    }

    public Produit(int idProd) {
        this.idProd = idProd;
    }

    public Produit(int idProd, int prix, int qte, String nom, String desc, String images, Date published) {
        this.idProd = idProd;
        this.prix = prix;
        this.qte = qte;
        this.nom = nom;
        this.desc = desc;
        this.images = images;
        this.published = published;
    }

    protected Produit(Parcel in) {
        idProd = in.readInt();
        prix = in.readInt();
        qte = in.readInt();
        nom = in.readString();
        desc = in.readString();
        images = in.readString();
        published = (Date) in.readSerializable();
    }

    public static final Creator<Produit> CREATOR = new Creator<Produit>() {
        @Override
        public Produit createFromParcel(Parcel in) {
            return new Produit(in);
        }

        @Override
        public Produit[] newArray(int size) {
            return new Produit[size];
        }
    };

    public int getIdProd() {
        return idProd;
    }

    public void setIdProd(int idProd) {
        this.idProd = idProd;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public void setSeenMark(Context context, int seenValue) {
        try {
            boolean found = false;
            JSONArray all = new JSONArray(readFromFile(context, "history.txt"));
            for (int i = 0; i < all.length(); i++) {
                JSONObject object = all.getJSONObject(i);
                if (object.getInt(String.valueOf("id")) == this.idProd) {
                    object.put("seen", seenValue);
                    found = true;
                }
            }
            if (!found) {
                JSONObject object = new JSONObject();
                object.put("id", this.idProd);
                object.put("seen", seenValue);
                all.put(object);
            }
            System.out.println("################################### >> " + all.toString());
            writeToFile(all.toString(), context, "history.txt");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean isSeen(Context context) {
        boolean isSeen = false;
        try {
            JSONArray all = new JSONArray(readFromFile(context, "history.txt"));
            for (int i = 0; i < all.length(); i++) {
                JSONObject object = all.getJSONObject(i);
                if (object.getInt(String.valueOf("id")) == this.idProd) {
                    isSeen = object.getInt("seen") == 1;
                }
            }
//            System.out.println("//////////////// >> " + all.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isSeen;
    }

    public String timeAgo() {
        return new PrettyTime(new Locale("fr")).format(this.published);
    }

    private void writeToFile(String data, Context context, String path) {
        createTheFileIfNotExists(context, path);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(path, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context, String path) {
        createTheFileIfNotExists(context, path);
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(path);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void createTheFileIfNotExists(Context context, String name) {
        File file = new File(context.getFilesDir(), name);
        if (!file.exists()) {
            writeToFile("[]", context, name);
            System.out.println("111111111111111111111111111111111 > the file exists !!");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(idProd);
        parcel.writeInt(prix);
        parcel.writeInt(qte);
        parcel.writeString(nom);
        parcel.writeString(desc);
        parcel.writeString(images);
        parcel.writeSerializable(published);
    }

    @Override
    public String toString() {
        return "Produit{" +
                "idProd=" + idProd +
                ", prix=" + prix +
                ", qte=" + qte +
                ", nom='" + nom + '\'' +
                ", desc='" + desc + '\'' +
                ", images='" + images + '\'' +
                ", published=" + published +
                '}';
    }
}
