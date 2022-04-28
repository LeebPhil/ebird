import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.util.Date;

/**
 * ResponseObject of eBird API.
 */
public class ResponseObject {
    public String speciesCode;
    public String comName;
    public String sciName;
    public String locId;
    public String locName;
    public String obsDt;
    public int howMany;
    public double lat;
    public double lng;
    public boolean obsValid;
    public boolean obsReviewed;
    public boolean locationPrivate;
    public String subId;

    // region Getters and Setters

    public String getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(String speciesCode) {
        this.speciesCode = speciesCode;
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public String getSciName() {
        return sciName;
    }

    public void setSciName(String sciName) {
        this.sciName = sciName;
    }

    public String getLocId() {
        return locId;
    }

    public void setLocId(String locId) {
        this.locId = locId;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public String getObsDt() {
        return obsDt;
    }

    public void setObsDt(String obsDt) {
        this.obsDt = obsDt;
    }

    public int getHowMany() {
        return howMany;
    }

    public void setHowMany(int howMany) {
        this.howMany = howMany;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isObsValid() {
        return obsValid;
    }

    public void setObsValid(boolean obsValid) {
        this.obsValid = obsValid;
    }

    public boolean isObsReviewed() {
        return obsReviewed;
    }

    public void setObsReviewed(boolean obsReviewed) {
        this.obsReviewed = obsReviewed;
    }

    public boolean isLocationPrivate() {
        return locationPrivate;
    }

    public void setLocationPrivate(boolean locationPrivate) {
        this.locationPrivate = locationPrivate;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }
    // endregion
}
