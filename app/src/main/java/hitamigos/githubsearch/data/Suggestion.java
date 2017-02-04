package hitamigos.githubsearch.data;

import android.os.Parcel;

import online.osslab.suggestions.model.SearchSuggestion;


public  class Suggestion implements SearchSuggestion {

   // private ColorWrapper colorWrapper;

    private String colorName;

    private boolean isHistory;

//    public Suggestion(ColorWrapper color){
//
//        this.colorWrapper = color;
//        this.colorName = colorWrapper.getName();
//    }

    public Suggestion(Parcel source) {
        this.colorName = source.readString();
    }

   // public ColorWrapper getColor(){return colorWrapper;}

    public void setIsHistory(boolean isHistory){
        this.isHistory = isHistory;
    }

    public boolean getIsHistory(){return this.isHistory;}

    @Override
    public String getBody() {
        //return colorWrapper.getName();
        return null;
    }

    @Override
    public Creator getCreator() {
        return CREATOR;
    }


    public static final Creator<Suggestion> CREATOR = new Creator<Suggestion>() {
        @Override
        public Suggestion createFromParcel(Parcel in) {
            return new Suggestion(in);
        }

        @Override
        public Suggestion[] newArray(int size) {
            return new Suggestion[size];
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