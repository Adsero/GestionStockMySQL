package Models;

/**
 * Created by root on 22/03/18.
 */

public class Commande {
    private int idCmd;
    private Annonceur annonceur;
    private double total = 0;

    public Commande() {}

    public Commande(int idCmd, Annonceur annonceur, double total) {
        this.idCmd = idCmd;
        this.annonceur = annonceur;
        this.total = total;
    }

    public int getIdCmd() {
        return idCmd;
    }

    public void setIdCmd(int idCmd) {
        this.idCmd = idCmd;
    }

    public Annonceur getAnnonceur() {
        return annonceur;
    }

    public void setAnnonceur(Annonceur annonceur) {
        this.annonceur = annonceur;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
