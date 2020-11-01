package com.example.keephersafeGPStrial;

import android.os.Parcel;
import android.os.Parcelable;

public class EntityModel implements Parcelable {

    public int id;
    public double latitude;
    public double longitude;
    public int pulse;
    public int decision;  //0 is safe, 1 is unsafe
    public int prediction; //0 is safe, 1 is unsafe
    public EntityModel() {

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public EntityModel createFromParcel(Parcel parcel) {
            return new EntityModel(parcel);
        }

        @Override
        public EntityModel[] newArray(int i) {
            return new EntityModel[i];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public int getDecision() {
        return decision;
    }

    public void setDecision(int decision) {
        this.decision = decision;
    }

    public int getPrediction() {
        return prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    public EntityModel(int id, double latitude, double longitude, int pulse, int decision, int prediction) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pulse = pulse;
        this.decision = decision;
        this.prediction = prediction;
    }



    @Override
    public int describeContents() {
        return 0;
    }
    protected EntityModel(Parcel in) {
        this.id = in.readInt();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.pulse = in.readInt();
        this.decision = in.readInt();
        this.prediction = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeInt(pulse);
        parcel.writeInt(decision);
        parcel.writeInt(prediction);
    }

    @Override
    public String toString() {
        return id + "||" + latitude + "||"+longitude+"||" + pulse+"||"+decision+"||" + prediction;

    }
}
