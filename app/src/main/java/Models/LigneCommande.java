package Models;

/**
 * Created by root on 22/03/18.
 */

public class LigneCommande {
    private int idLigneCmd;
    private Produit prod;
    private int qte;

    public LigneCommande() {}

    public LigneCommande(int idLigneCmd, Produit prod, int qte) {
        this.idLigneCmd = idLigneCmd;
        this.prod = prod;
        this.qte = qte;
    }

    public int getIdLigneCmd() {
        return idLigneCmd;
    }

    public void setIdLigneCmd(int idLigneCmd) {
        this.idLigneCmd = idLigneCmd;
    }

    public Produit getProd() {
        return prod;
    }

    public void setProd(Produit prod) {
        this.prod = prod;
    }

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }
}
