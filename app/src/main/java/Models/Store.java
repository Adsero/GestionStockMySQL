package Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Store implements Parcelable {
    private int id;
    private String name;
    private String logo;
    private String geo;
    private Annonceur owner;
    private int detectionZone;

    public Store() {
    }

    public Store(int id) {
        this.id = id;
    }

    public Store(int id, String name, String logo, String geo, Annonceur owner, int detectionZone) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.geo = geo;
        this.owner = owner;
        this.detectionZone = detectionZone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public Annonceur getOwner() {
        return owner;
    }

    public void setOwner(Annonceur owner) {
        this.owner = owner;
    }

    public int getDetectionZone() {
        return detectionZone;
    }

    public void setDetectionZone(int detectionZone) {
        this.detectionZone = detectionZone;
    }

    protected Store(Parcel in) {
        id = in.readInt();
        name = in.readString();
        logo = in.readString();
        geo = in.readString();
        owner = in.readParcelable(Annonceur.class.getClassLoader());
        detectionZone = in.readInt();
    }

    public static final Creator<Store> CREATOR = new Creator<Store>() {
        @Override
        public Store createFromParcel(Parcel in) {
            return new Store(in);
        }

        @Override
        public Store[] newArray(int size) {
            return new Store[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(logo);
        parcel.writeString(geo);
        parcel.writeParcelable(owner, i);
        parcel.writeInt(detectionZone);
    }

    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", logo='" + logo + '\'' +
                ", geo='" + geo + '\'' +
                ", owner=" + owner +
                ", detectionZone=" + detectionZone +
                '}';
    }
}
