package online.osslab.suggestions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import online.osslab.R;
import online.osslab.suggestions.model.SearchSuggestion;
import online.osslab.util.Util;
import online.osslab.util.view.BodyTextView;
import online.osslab.util.view.IconImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public class SearchSuggestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "";

    private List<SearchSuggestion> searchSuggestions;

    private Listener listener;

    private Context context;

    private SearchSuggestion selectedItem;

    private Drawable rightIconDrawable;

    private int bodyTextSizePx;

    private int textColor = -1;

    private int rightIconColor = -1;

    public interface OnBindSuggestionCallback{

        void onBindSuggestion(IconImageView leftIcon, BodyTextView bodyText, SearchSuggestion item, int itemPosition);
    }

    private OnBindSuggestionCallback onBindSuggestionCallback;

    public interface Listener{

        void onItemSelected(SearchSuggestion item);

        void onMoveItemToSearchClicked(SearchSuggestion item);
    }

    public void setOnBindSuggestionCallback(OnBindSuggestionCallback callback){
        this.onBindSuggestionCallback = callback;
    }

    public void setTextColor(int color){

        boolean notify = false;
        if(this.textColor !=color)
            notify = true;

        this.textColor = color;

        if(notify)
            notifyDataSetChanged();
    }

    public void setRightIconColor(int color){

        boolean notify = false;
        if(this.rightIconColor !=color)
            notify = true;

        this.rightIconColor = color;

        if(notify)
            notifyDataSetChanged();
    }

    public static class SearchSuggestionViewHolder extends RecyclerView.ViewHolder{

        private static final String TAG = "";

        public BodyTextView bodyText;

        public IconImageView leftIcon;

        public IconImageView rightIcon;

        private Listener listener;

        public interface Listener{

            void onItemClicked(int adapterPosition);

            void onMoveItemToSearchClicked(int adapterPosition);
        }

        public SearchSuggestionViewHolder(View v, Listener listener) {
            super (v);

            this.listener = listener;
            bodyText = (BodyTextView) v.findViewById(R.id.suggestion_text);
            leftIcon = (IconImageView) v.findViewById(R.id.suggestion_left_icon);
            rightIcon = (IconImageView) v.findViewById(R.id.suggestion_right_icon);

            rightIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(SearchSuggestionViewHolder.this.listener !=null)
                        SearchSuggestionViewHolder.this.listener.onMoveItemToSearchClicked(getAdapterPosition());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(SearchSuggestionViewHolder.this.listener !=null)
                        SearchSuggestionViewHolder.this.listener.onItemClicked(getAdapterPosition());
                }
            });
        }

    }

    public SearchSuggestionsAdapter(Context context, int suggestionTextSize, Listener listener) {

        this.context = context;
        this.listener = listener;
        this.bodyTextSizePx = suggestionTextSize;

        searchSuggestions = new ArrayList<>();

        rightIconDrawable = this.context.getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
        rightIconDrawable = DrawableCompat.wrap(rightIconDrawable);
        DrawableCompat.setTint(rightIconDrawable, this.context.getResources().getColor(R.color.gray_active_icon));
    }

    public List<? extends SearchSuggestion> getDataSet(){
        return searchSuggestions;
    }

    public void swapData(List<? extends SearchSuggestion> searchSuggestions){

        this.searchSuggestions.clear();
        this.searchSuggestions.addAll(searchSuggestions);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_suggestion_item, viewGroup, false);
        SearchSuggestionViewHolder viewHolder = new SearchSuggestionViewHolder(view, new SearchSuggestionViewHolder.Listener() {

            @Override
            public void onItemClicked(int adapterPosition) {

                if(listener !=null)
                    listener.onItemSelected(searchSuggestions.get(adapterPosition));
            }

            @Override
            public void onMoveItemToSearchClicked(int adapterPosition) {

                if(listener !=null)
                    listener.onMoveItemToSearchClicked(searchSuggestions.get(adapterPosition));
            }

        });

        viewHolder.rightIcon.setImageDrawable(rightIconDrawable);
        viewHolder.bodyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSizePx);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {

        SearchSuggestion item = searchSuggestions.get(position);

        SearchSuggestionViewHolder viewHolder = (SearchSuggestionViewHolder) vh;

        resetImageView(viewHolder.leftIcon);

        viewHolder.bodyText.setText(item.getBody());

        viewHolder.leftIcon.setImageDrawable(null);

        if(textColor !=-1)
            viewHolder.bodyText.setTextColor(textColor);

        if(rightIconColor !=-1)
            Util.setIconColor(viewHolder.rightIcon.getDrawable(), rightIconColor);

        if(onBindSuggestionCallback !=null) {

            //we need to employ a locking technique in order to prevent client from
            //setting properties on the icon and text that are to be set by the library only
            viewHolder.bodyText.lock();
            viewHolder.leftIcon.lock();
            onBindSuggestionCallback.onBindSuggestion(viewHolder.leftIcon, viewHolder.bodyText, item, position);
            viewHolder.bodyText.unlock();
            viewHolder.leftIcon.unlock();
        }
    }

    @Override
    public int getItemCount() {

        return searchSuggestions !=null ? searchSuggestions.size() : 0;
    }

    public void clearDataSet(){

        int rage = searchSuggestions.size();
        searchSuggestions.clear();
        notifyItemRangeRemoved(0, rage);
    }

    //todo
    //reset all properties that the client might have
    //changed.
    private void resetImageView(ImageView imageView){

        imageView.setImageDrawable(null);
        imageView.setAlpha(1.0f);
    }
}
