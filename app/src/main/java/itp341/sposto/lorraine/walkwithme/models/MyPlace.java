package itp341.sposto.lorraine.walkwithme.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by LorraineSposto on 5/5/16.
 */
public class MyPlace implements Serializable, Comparable {
    private String placeName;
    private String id;
    private String address;
    private Date date;

    public MyPlace(String name, String id, String address, Date date) {
        this.placeName = name;
        this.id = id;
        this.address = address;
        this.date = date;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(Object another) {
        if (another.getClass().equals(this.getClass())) {
            return 0;
        }
        return date.compareTo(((MyPlace) another).date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyPlace)) return false;

        MyPlace myPlace = (MyPlace) o;

        if (id != null ? !id.equals(myPlace.id) : myPlace.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = placeName != null ? placeName.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

}

