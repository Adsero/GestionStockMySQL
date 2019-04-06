package Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by root on 22/03/18.
 */

public class Annonceur implements Parcelable {
    private int idAnnonceur;
    private String nom;
    private String prenom;
    private Date date_naiss;
    private String login;
    private String pass;

    public Annonceur() {
    }

    public Annonceur(int idAnnonceur) {
        this.idAnnonceur = idAnnonceur;
    }

    public Annonceur(int idAnnonceur, String nom, String prenom, Date date_naiss, String login, String pass) {
        this.idAnnonceur = idAnnonceur;
        this.nom = nom;
        this.prenom = prenom;
        this.date_naiss = date_naiss;
        this.login = login;
        this.pass = pass;
    }

    public int getIdAnnonceur() {
        return idAnnonceur;
    }

    public void setIdAnnonceur(int idAnnonceur) {
        this.idAnnonceur = idAnnonceur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Date getDate_naiss() {
        return date_naiss;
    }

    public void setDate_naiss(Date date_naiss) {
        this.date_naiss = date_naiss;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public static Creator<Annonceur> getCREATOR() {
        return CREATOR;
    }

    protected Annonceur(Parcel in) {
        idAnnonceur = in.readInt();
        nom = in.readString();
        prenom = in.readString();
        login = in.readString();
        pass = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idAnnonceur);
        dest.writeString(nom);
        dest.writeString(prenom);
        dest.writeString(login);
        dest.writeString(pass);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Annonceur> CREATOR = new Creator<Annonceur>() {
        @Override
        public Annonceur createFromParcel(Parcel in) {
            return new Annonceur(in);
        }

        @Override
        public Annonceur[] newArray(int size) {
            return new Annonceur[size];
        }
    };

    @Override
    public String toString() {
        return "Annonceur{" +
                "idAnnonceur=" + idAnnonceur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", date_naiss=" + date_naiss +
                ", login='" + login + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }
}
