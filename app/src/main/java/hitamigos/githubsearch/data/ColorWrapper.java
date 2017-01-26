package hitamigos.githubsearch.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ColorWrapper {

    @SerializedName("hex")
    @Expose
    private String hex;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("rgb")
    @Expose
    private String rgb;

    /**
     *
     * @return
     * The hex
     */
    public String getHex() {
        return hex;
    }

    /**
     *
     * @param hex
     * The hex
     */
    public void setHex(String hex) {
        this.hex = hex;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The rgb
     */
    public String getRgb() {
        return rgb;
    }

    /**
     *
     * @param rgb
     * The rgb
     */
    public void setRgb(String rgb) {
        this.rgb = rgb;
    }

}