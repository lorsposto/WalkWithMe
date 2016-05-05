package itp341.sposto.lorraine.walkwithme.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by LorraineSposto on 5/4/16.
 */
public class Contact implements Parcelable {
    private long id;
    private String phoneNumber;
    private Uri uri;

    public Contact(long id, String phoneNumber, Uri uri) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.uri = uri;
    }

    public Contact(Parcel in) {
        super();
        readFromParcel(in);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", uri=" + uri +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) {
            return false;
        }
        Contact other = (Contact) o;
        return (id == other.getId() && phoneNumber.equals(other.getPhoneNumber()) && uri.equals(other.getUri()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeLong(id);
        dest.writeString(phoneNumber);
    }

    public void readFromParcel(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        id = in.readLong();
        phoneNumber = in.readString();

    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {

            return new Contact[size];
        }

    };

    public static ArrayList<String> getPhoneNumbersFromList(ArrayList<Contact> contacts) {
        ArrayList<String> pns = new ArrayList<>();
        for (Contact c : contacts) {
            pns.add(c.getPhoneNumber());
        }
        return pns;
    }
}
