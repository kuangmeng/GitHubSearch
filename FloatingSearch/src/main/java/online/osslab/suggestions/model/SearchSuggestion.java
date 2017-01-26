package online.osslab.suggestions.model;

import android.os.Parcelable;

import online.osslab.FloatingSearchView;

/**
 * An object that represents a single suggestion item
 * in the suggestions drop down generated in response
 * to an entered query in the {@link FloatingSearchView}
 */
public interface SearchSuggestion extends Parcelable{

    /**
     * Returns the text that should be displayed
     * for the suggestion represented by this object.
     *
     * @return the text for this suggestion
     */
    String getBody();

    /**
     * Returns a creator object that will be used
     * for saving state.
     *
     * <p>Classes that implement this object have
     * the responsibility to include getBody() value
     * in their Parcelable implementation. Failure to
     * do so will result in empty suggestion items after
     * a configuration change</p>
     *
     * @return a {@link Creator Creator} that
     *         will be used to save state.
     */
    Creator getCreator();

}
