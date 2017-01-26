package hitamigos.githubsearch.data;

import android.os.Parcel;

import online.osslab.suggestions.model.SearchSuggestion;


public  class ColorSuggestion implements SearchSuggestion {

    private ColorWrapper colorWrapper;

    private String colorName;

    private boolean isHistory;

    public ColorSuggestion(ColorWrapper color){

        this.colorWrapper = color;
        this.colorName = colorWrapper.getName();
    }

    public ColorSuggestion(Parcel source) {
        this.colorName = source.readString();
    }

    public ColorWrapper getColor(){
        return colorWrapper;
    }

    public void setIsHistory(boolean isHistory){
        this.isHistory = isHistory;
    }

    public boolean getIsHistory(){return this.isHistory;}

    @Override
    public String getBody() {
        return colorWrapper.getName();
    }

    @Override
    public Creator getCreator() {
        return CREATOR;
    }

    ///////

    public static final Creator<ColorSuggestion> CREATOR = new Creator<ColorSuggestion>() {
        @Override
        public ColorSuggestion createFromParcel(Parcel in) {
            return new ColorSuggestion(in);
        }

        @Override
        public ColorSuggestion[] newArray(int size) {
            return new ColorSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(colorName);
    }
}