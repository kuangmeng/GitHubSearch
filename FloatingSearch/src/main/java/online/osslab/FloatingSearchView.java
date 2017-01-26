package online.osslab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import online.osslab.suggestions.SearchSuggestionsAdapter;
import online.osslab.suggestions.model.SearchSuggestion;
import online.osslab.util.Util;
import online.osslab.util.adapter.GestureDetectorListenerAdapter;
import online.osslab.util.adapter.OnItemTouchListenerAdapter;
import online.osslab.util.adapter.TextWatcherAdapter;
import online.osslab.util.view.MenuView;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public class FloatingSearchView extends FrameLayout {

    private static final String TAG = "FloatingSearchView";

    private final int BACKGROUND_DRAWABLE_ALPHA_SEARCH_ACTIVE = 150;

    private final int BACKGROUND_DRAWABLE_ALPHA_SEARCH_INACTIVE = 0;

    private final int MENU_ICON_ANIM_DURATION = 250;

    private final int BACKGROUND_FADE__ANIM_DURATION = 250;

    private final int ATTRS_SEARCH_BAR_MARGIN_DEFAULT = 0;

    /*
     * The ideal min width that the left icon plus the query EditText
     * should have. It applies only when determining how to render
     * the action items, it doesn't set the views' min attributes.
     */
    public final int SEARCH_BAR_LEFT_SECTION_DESIRED_WIDTH;

    public final static int LEFT_ACTION_MODE_SHOW_HAMBURGER_ENUM_VAL = 1;
    public final static int LEFT_ACTION_MODE_SHOW_SEARCH_ENUM_VAL = 2;
    public final static int LEFT_ACTION_MODE_SHOW_HOME_ENUM_VAL = 3;
    public final static int LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL = 4;

    @IntDef({LEFT_ACTION_MODE_SHOW_HAMBURGER_ENUM_VAL, LEFT_ACTION_MODE_SHOW_SEARCH_ENUM_VAL,LEFT_ACTION_MODE_SHOW_HOME_ENUM_VAL
            ,LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LeftActionMode {}

    @LeftActionMode private final int ATTRS_SEARCH_BAR_LEFT_ACTION_MODE_DEFAULT = LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL;

    private final boolean ATTRS_DISMISS_ON_OUTSIDE_TOUCH_DEFAULT = false;

    private final boolean ATTRS_SEARCH_BAR_SHOW_SEARCH_KEY_DEFAULT = true;

    private final boolean ATTRS_SEARCH_BAR_SHOW_SEARCH_HINT_NOT_FOCUSED_DEFAULT = true;

    private final int ATTRS_SUGGESTION_TEXT_SIZE_SP_DEFAULT = 18;

    private final int SUGGEST_LIST_COLLAPSE_ANIM_DURATION = 200;

    private final Interpolator SUGGEST_LIST_COLLAPSE_ANIM_INTERPOLATOR = new LinearInterpolator();

    private final int SUGGEST_ITEM_ADD_ANIM_DURATION = 250;

    private final Interpolator SUGGEST_ITEM_ADD_ANIM_INTERPOLATOR = new LinearInterpolator();

    private final int SUGGESTION_ITEM_ANIM_DURATION = 120;

    private Activity hostActivity;

    private Drawable backgroundDrawable;
    private boolean dismissOnOutsideTouch = true;

    private CardView querySection;
    private OnSearchListener searchListener;
    private boolean isFocused;
    private OnFocusChangeListener focusChangeListener;
    private EditText searchInput;
    private String titleText;
    private boolean isTitleSet;
    private int searchInputTextColor = -1;
    private int searchInputHintColor = -1;
    private View searchInputParent;
    private String historyQuery = "";
    private OnQueryChangeListener queryListener;
    private ImageView leftAction;
    private OnLeftMenuClickListener onMenuClickListener;
    private OnHomeActionClickListener onHomeActionClickListener;
    private ProgressBar searchProgress;
    private DrawerArrowDrawable menuBtnDrawable;
    private Drawable iconBackArrow;
    private Drawable iconSearch;
    @LeftActionMode int leftActionMode;
    private boolean searchCanFocus;
    private String searchHint;
    private boolean showSearchKey;
    private boolean isMenuOpen = false;
    private MenuView menuView;
    private int actionMenuItemColor;
    private int overflowIconColor;
    private OnMenuItemClickListener actionMenuItemListener;
    private ImageView clearButton;
    private Drawable iconClear;
    private boolean skipQueryFocusChangeEvent;
    private boolean skipTextChangeEvent;

    private View divider;

    private RelativeLayout suggestionsSection;
    private View suggestionListContainer;
    private RecyclerView suggestionsList;
    private int suggestionTextColor = -1;
    private int suggestionRightIconColor;
    private SearchSuggestionsAdapter suggestionsAdapter;
    private SearchSuggestionsAdapter.OnBindSuggestionCallback onBindSuggestionCallback;
    private boolean isCollapsing = false;
    private int suggestionsTextSizePx;
    private boolean isInitialLayout = true;
    private boolean isSuggestionsSecHeightSet;

    //An interface for implementing a listener that will get notified when the suggestions
    //section's height is set. This is to be used internally only.
    private interface OnSuggestionSecHeightSetListener{
        void onSuggestionSecHeightSet();
    }
    private OnSuggestionSecHeightSetListener onSuggestionSecHeightListener;

    /**
     * Interface for implementing a callback to be
     * invoked when the left menu (navigation menu) is
     * clicked.
     */
    public interface OnLeftMenuClickListener{

        /**
         * Called when the menu button was
         * clicked and the menu's state is now opened.
         */
        void onMenuOpened();

        /**
         * Called when the back button was
         * clicked and the menu's state is now closed.
         */
        void onMenuClosed();
    }

    /**
     * Interface for implementing a callback to be
     * invoked when the home action button (the back arrow)
     * is clicked.
     *
     * <p>Note: This is only relevant when leftActionMode is
     * set to {@value #LEFT_ACTION_MODE_SHOW_HOME_ENUM_VAL}</p>
     */
    public interface OnHomeActionClickListener{

        /**
         * Called when the home button was
         * clicked.
         */
        void onHomeClicked();
    }

    /**
     * Interface for implementing a listener to listen
     * to when the current search has completed.
     */
    public interface OnSearchListener {

        /**
         * Called when a suggestion was clicked indicating
         * that the current search has completed.
         *
         * @param searchSuggestion
         */
        void onSuggestionClicked(SearchSuggestion searchSuggestion);

        /**
         * Called when the current search has completed
         * as a result of pressing search key in the keyboard.
         */
        void onSearchAction();
    }

    /**
     * Interface for implementing a listener to listen
     * when an item in the action (the item can be presented as an action
     * ,or as a menu item in the overflow menu) menu has been selected.
     */
    public interface OnMenuItemClickListener{

        /**
         * Called when a menu item in has been
         * selected.
         *
         * @param item the selected menu item.
         */
        void onActionMenuItemSelected(MenuItem item);
    }

    /**
     * Interface for implementing a listener to listen
     * to for state changes in the query text.
     */
    public interface OnQueryChangeListener{

        /**
         * Called when the query has changed. it will
         * be invoked when one or more characters in the
         * query was changed.
         *
         * @param oldQuery the previous query
         * @param newQuery the new query
         */
        void onSearchTextChanged(String oldQuery, String newQuery);
    }

    /**
     * Interface for implementing a listener to listen
     * to for focus state changes.
     */
    public interface OnFocusChangeListener{

        /**
         * Called when the search bar has gained focus
         * and listeners are now active.
         */
        void onFocus();

        /**
         * Called when the search bar has lost focus
         * and listeners are no more active.
         */
        void onFocusCleared();
    }

    public FloatingSearchView(Context context) {
        this(context, null);
    }

    public FloatingSearchView(Context context, AttributeSet attrs){
        super(context, attrs);
        SEARCH_BAR_LEFT_SECTION_DESIRED_WIDTH = Util.dpToPx(225);
        init(attrs);
    }

    private void init(AttributeSet attrs){

        hostActivity = getHostActivity();

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflate(getContext(), R.layout.floating_search_layout, this);

        backgroundDrawable = new ColorDrawable(Color.BLACK);

        querySection = (CardView)findViewById(R.id.search_query_section);
        clearButton = (ImageView)findViewById(R.id.clear_btn);
        searchInput = (EditText)findViewById(R.id.search_text);
        searchInputParent = findViewById(R.id.search_input_parent);
        leftAction = (ImageView)findViewById(R.id.search_left_action);
        searchProgress = (ProgressBar)findViewById(R.id.search_progress);
        initDrawables();
        clearButton.setImageDrawable(iconClear);
        menuView = (MenuView)findViewById(R.id.menu_view);

        divider = findViewById(R.id.search_divider);

        suggestionsSection = (RelativeLayout)findViewById(R.id.search_suggestions_section);
        suggestionListContainer = findViewById(R.id.suggestions_list_container);
        suggestionsList = (RecyclerView)findViewById(R.id.suggestions_list);

        setupViews(attrs);
    }

    private void initDrawables(){

        menuBtnDrawable = new DrawerArrowDrawable(getContext());

        iconClear = getResources().getDrawable(R.drawable.ic_clear_black_24dp);
        iconClear = DrawableCompat.wrap(iconClear);

        iconBackArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
        iconBackArrow = DrawableCompat.wrap(iconBackArrow);

        iconSearch = getResources().getDrawable(R.drawable.ic_search_black_24dp);
        iconSearch = DrawableCompat.wrap(iconSearch);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if(isInitialLayout) {

            int addedHeight = Util.dpToPx(5*3);
            final int finalHeight = suggestionsSection.getMeasuredHeight()+addedHeight;

            //we need to add 5dp to the suggestionsSection because we are
            //going to move it up by 5dp in order to cover the search bar's
            //rounded corners. We also need to add an additional 10dp to
            //suggestionsSection in order to hide suggestionListContainer
            //rounded corners and top/bottom padding.
            suggestionsSection.getLayoutParams().height = finalHeight;
            suggestionsSection.requestLayout();

            ViewTreeObserver vto = suggestionListContainer.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    if (suggestionsSection.getHeight() == finalHeight) {

                        if (Build.VERSION.SDK_INT < 16) {
                            suggestionListContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        } else {
                            suggestionListContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }

                        if (onSuggestionSecHeightListener != null)
                            onSuggestionSecHeightListener.onSuggestionSecHeightSet();

                        isSuggestionsSecHeightSet = true;
                    }
                }

            });

            isInitialLayout = false;

        }

        //pass on the layout
        super.onLayout(changed, l, t, r, b);
    }

    private void setupViews(AttributeSet attrs){

        if(attrs!=null)
            applyXmlAttributes(attrs);

        backgroundDrawable.setAlpha(BACKGROUND_DRAWABLE_ALPHA_SEARCH_INACTIVE);

        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(backgroundDrawable);
        } else {
            setBackground(backgroundDrawable);
        }

        setupQueryBar();

        if(!isInEditMode())
            setupSuggestionSection();
    }

    private void applyXmlAttributes(AttributeSet attrs){

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingSearchView);

        try {

            setDismissOnOutsideClick(true);

            int searchBarWidth = a.getDimensionPixelSize(R.styleable.FloatingSearchView_searchBarWidth, ViewGroup.LayoutParams.MATCH_PARENT);

            querySection.getLayoutParams().width = searchBarWidth;
            divider.getLayoutParams().width = searchBarWidth;
            suggestionListContainer.getLayoutParams().width = searchBarWidth;

            int searchBarLeftMargin = a.getDimensionPixelSize(R.styleable.FloatingSearchView_searchBarMarginLeft, ATTRS_SEARCH_BAR_MARGIN_DEFAULT);
            int searchBarTopMargin = a.getDimensionPixelSize(R.styleable.FloatingSearchView_searchBarMarginTop, ATTRS_SEARCH_BAR_MARGIN_DEFAULT);
            int searchBarRightMargin = a.getDimensionPixelSize(R.styleable.FloatingSearchView_searchBarMarginRight, ATTRS_SEARCH_BAR_MARGIN_DEFAULT);

            LayoutParams querySectionLP = (LayoutParams) querySection.getLayoutParams();
            LayoutParams dividerLP = (LayoutParams) divider.getLayoutParams();
            LinearLayout.LayoutParams suggestListSectionLP = (LinearLayout.LayoutParams) suggestionsSection.getLayoutParams();

            int cardPadding = Util.dpToPx(3);

            querySectionLP.setMargins(searchBarLeftMargin, searchBarTopMargin, searchBarRightMargin, 0);
            dividerLP.setMargins(searchBarLeftMargin + cardPadding, 0, searchBarRightMargin + cardPadding, ((MarginLayoutParams) divider.getLayoutParams()).bottomMargin);
            suggestListSectionLP.setMargins(searchBarLeftMargin, 0, searchBarRightMargin, 0);

            querySection.setLayoutParams(querySectionLP);
            divider.setLayoutParams(dividerLP);
            suggestionsSection.setLayoutParams(suggestListSectionLP);

            setSearchHint(a.getString(R.styleable.FloatingSearchView_searchHint));

            setShowSearchKey(a.getBoolean(R.styleable.FloatingSearchView_showSearchKey, ATTRS_SEARCH_BAR_SHOW_SEARCH_KEY_DEFAULT));

            setDismissOnOutsideClick(a.getBoolean(R.styleable.FloatingSearchView_dismissOnOutsideTouch, ATTRS_DISMISS_ON_OUTSIDE_TOUCH_DEFAULT));

            setSuggestionItemTextSize(a.getDimensionPixelSize(R.styleable.FloatingSearchView_suggestionTextSize, Util.spToPx(ATTRS_SUGGESTION_TEXT_SIZE_SP_DEFAULT)));

            setLeftActionMode(a.getInt(R.styleable.FloatingSearchView_leftActionMode, LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL));

            if (a.hasValue(R.styleable.FloatingSearchView_searchMenu)) {
                menuView.resetMenuResource(a.getResourceId(R.styleable.FloatingSearchView_searchMenu, 0));
            }

            setBackgroundColor(a.getColor(R.styleable.FloatingSearchView_backgroundColor,getResources().getColor(R.color.background)));
            setLeftActionIconColor(a.getColor(R.styleable.FloatingSearchView_leftActionColor, getResources().getColor(R.color.left_action_icon)));
            setActionMenuOverflowColor(a.getColor(R.styleable.FloatingSearchView_actionMenuOverflowColor, getResources().getColor(R.color.overflow_icon_color)));
            setMenuItemIconColor(a.getColor(R.styleable.FloatingSearchView_menuItemIconColor, getResources().getColor(R.color.menu_icon_color)));
            setDividerColor(a.getColor(R.styleable.FloatingSearchView_dividerColor, getResources().getColor(R.color.divider)));
            setClearBtnColor(a.getColor(R.styleable.FloatingSearchView_clearBtnColor, getResources().getColor(R.color.clear_btn_color)));
            setViewTextColor(a.getColor(R.styleable.FloatingSearchView_viewTextColor, getResources().getColor(R.color.dark_gray)));
            setHintTextColor(a.getColor(R.styleable.FloatingSearchView_hintTextColor, getResources().getColor(R.color.hint_color)));
            setSuggestionRightIconColor(a.getColor(R.styleable.FloatingSearchView_suggestionRightIconColor, getResources().getColor(R.color.gray_active_icon)));

        } finally {

            a.recycle();
        }
    }

    private Activity getHostActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    private void setupQueryBar(){

        searchInput.setTextColor(this.searchInputTextColor);
        searchInput.setHintTextColor(this.searchInputHintColor);

        if(!isInEditMode() && hostActivity !=null)
            hostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if(isInEditMode())
            menuView.reset(actionMenuAvailWidth());


        ViewTreeObserver vto = querySection.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT < 16) {
                    querySection.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                } else {
                    querySection.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                menuView.reset(actionMenuAvailWidth());

                if (isFocused)
                    menuView.hideIfRoomItems(false);
            }
        });

        menuView.setMenuCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {

                if (actionMenuItemListener != null)
                    actionMenuItemListener.onActionMenuItemSelected(item);

                //todo check if we should care about this return or not
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }

        });

        menuView.setOnVisibleWidthChanged(new MenuView.OnVisibleWidthChanged() {
            @Override
            public void onVisibleWidthChanged(int newVisibleWidth) {

                //todo avaoid magic numbers
                if (newVisibleWidth == 0) {
                    clearButton.setTranslationX(-Util.dpToPx(4));
                    searchInput.setPadding(0, 0, newVisibleWidth + Util.dpToPx(48) + Util.dpToPx(4), 0);
                } else {
                    clearButton.setTranslationX(-newVisibleWidth);
                    searchInput.setPadding(0, 0, newVisibleWidth + Util.dpToPx(48), 0);
                }
            }
        });

        menuView.setActionIconColor(this.actionMenuItemColor);
        menuView.setOverflowColor(this.overflowIconColor);

        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                searchInput.setText("");
            }
        });

        clearButton.setVisibility(View.INVISIBLE);
        searchInput.addTextChangedListener(new TextWatcherAdapter() {

            public void onTextChanged(final CharSequence s, int start, int before, int count) {

                if (skipTextChangeEvent || !isFocused) {
                    skipTextChangeEvent = false;
                } else {

                    if (searchInput.getText().toString().length() != 0 && clearButton.getVisibility() == View.INVISIBLE) {
                        clearButton.setAlpha(0.0f);
                        clearButton.setVisibility(View.VISIBLE);
                        ViewCompat.animate(clearButton).alpha(1.0f).setDuration(500).start();
                    } else if (searchInput.getText().toString().length() == 0)
                        clearButton.setVisibility(View.INVISIBLE);

                    if (queryListener != null && isFocused)
                        queryListener.onSearchTextChanged(historyQuery, searchInput.getText().toString());

                    historyQuery = searchInput.getText().toString();
                }
            }

        });

        searchInput.setOnFocusChangeListener(new TextView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (skipQueryFocusChangeEvent) {
                    skipQueryFocusChangeEvent = false;
                } else {

                    if (hasFocus != isFocused)
                        setSearchFocusedInternal(hasFocus);
                }
            }
        });

        searchInput.setOnKeyListener(new OnKeyListener() {

            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                if (showSearchKey && keyCode == KeyEvent.KEYCODE_ENTER) {

                    setSearchFocusedInternal(false);

                    if (searchListener != null)
                        searchListener.onSearchAction();

                    return true;
                }
                return false;
            }
        });

        if(leftActionMode == LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL)
            searchInputParent.setTranslationX(-Util.dpToPx(48+20-16));

        refreshLeftIcon();

        leftAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (searchInput.isFocused()) {

                    setSearchFocusedInternal(false);
                } else {

                    switch (leftActionMode){

                        case LEFT_ACTION_MODE_SHOW_HAMBURGER_ENUM_VAL:{
                            toggleLeftMenu();
                        }break;
                        case LEFT_ACTION_MODE_SHOW_SEARCH_ENUM_VAL:{
                            setSearchFocusedInternal(true);
                        }break;
                        case LEFT_ACTION_MODE_SHOW_HOME_ENUM_VAL:{
                            if(onHomeActionClickListener !=null)
                                onHomeActionClickListener.onHomeClicked();
                        }break;
                        case LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL:{
                            //do nothing
                        }
                    }
                }

            }
        });
    }

    /**
     * Sets the menu button's color.
     *
     * @param color the color to be applied to the
     *              left menu button.
     */
    public void setLeftActionIconColor(int color){

        menuBtnDrawable.setColor(color);
        DrawableCompat.setTint(iconBackArrow, color);
        DrawableCompat.setTint(iconSearch, color);
    }

    /**
     * Sets the clear button's color.
     *
     * @param color the color to be applied to the
     *              clear button.
     */
    public void setClearBtnColor(int color){
        DrawableCompat.setTint(iconClear, color);
    }

    /**
     * Sets the action menu icons' color.
     *
     * @param color the color to be applied to the
     *              action menu items.
     */
    public void setMenuItemIconColor(int color){

        this.actionMenuItemColor = color;

        if(menuView !=null)
            menuView.setActionIconColor(this.actionMenuItemColor);
    }

    /**
     * Sets the action menu overflow icon's color.
     *
     * @param color the color to be applied to the
     *              overflow icon.
     */
    public void setActionMenuOverflowColor(int color){

        this.overflowIconColor = color;

        if(menuView !=null)
            menuView.setOverflowColor(this.overflowIconColor);
    }

    /**
     * Sets the background color of the search
     * view.
     *
     * @param color the color to be applied to the search bar and
     *              the suggestion section background.
     */
    public void setBackgroundColor(int color){

        if(querySection !=null && suggestionsList !=null){
            querySection.setCardBackgroundColor(color);
            suggestionsList.setBackgroundColor(color);
        }
    }

    /**
     * Sets the text color of the search
     * and suggestion text.
     *
     * @param color the color to be applied to the search and suggestion
     *              text.
     */
    public void setViewTextColor(int color){

        this.searchInputTextColor = color;
        this.suggestionTextColor = color;

        if(searchInput !=null && suggestionsAdapter !=null){
            searchInput.setTextColor(color);
            suggestionsAdapter.setTextColor(color);
        }
    }

    /**
     * Sets the text color of the search
     * hint.
     *
     * @param color the color to be applied to the search hint.
     */
    public void setHintTextColor(int color){

        this.searchInputHintColor = color;

        if(searchInput !=null){
            searchInput.setHintTextColor(color);
        }
    }

    /**
     * Sets the color of the search divider that
     * divides the search section from the suggestions.
     *
     * @param color the color to be applied the divider.
     */
    public void setDividerColor(int color){

        if(divider !=null)
            divider.setBackgroundColor(color);
    }

    /**
     * Mimics a menu click that opens the menu. Useful when for navigation
     * drawers when they open as a result of dragging.
     */
    public void openMenu(boolean withAnim) {

        //todo go over
        openMenu(true, withAnim, false);
    }

    /**
     * Mimics a menu click that closes. Useful when fo navigation
     * drawers when they close as a result of selecting and item.
     *
     * @param withAnim true, will close the menu button with
     *                 the  Material animation
     */
    public void closeMenu(boolean withAnim) {

        //todo go over
        closeMenu(true, withAnim, false);
    }

    /**
     * Set the mode for the left action button.
     *
     * @param mode
     */
    private void setLeftActionMode(int mode){

        //todo implement dynamic leftActionMode setting and expose method
        leftActionMode = mode;
        //refreshLeftIcon();
    }

    private void refreshLeftIcon(){

        leftAction.setVisibility(VISIBLE);

        switch (leftActionMode){

            case LEFT_ACTION_MODE_SHOW_HAMBURGER_ENUM_VAL:{
                leftAction.setImageDrawable(menuBtnDrawable);
            }break;
            case LEFT_ACTION_MODE_SHOW_SEARCH_ENUM_VAL:{
                leftAction.setImageDrawable(iconSearch);
            }break;
            case LEFT_ACTION_MODE_SHOW_HOME_ENUM_VAL:{
                leftAction.setImageDrawable(menuBtnDrawable);
                menuBtnDrawable.setProgress(1.0f);
            }break;
            case LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL:{
                leftAction.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Inflates the menu items from
     * an xml resource.
     *
     * @param menuId a menu xml resource reference
     */
    public void inflateOverflowMenu(int menuId){
       menuView.resetMenuResource(menuId);

        //todo check fo synchronization problems in MenuView
        //when calling it this way
       menuView.reset(actionMenuAvailWidth());
       if(isFocused)
            menuView.hideIfRoomItems(false);
    }

    private int actionMenuAvailWidth(){
        if(isInEditMode())
            return Util.dpToPx(360) - SEARCH_BAR_LEFT_SECTION_DESIRED_WIDTH;
        return querySection.getWidth() - SEARCH_BAR_LEFT_SECTION_DESIRED_WIDTH;
    }

    private void toggleLeftMenu(){

        if(isMenuOpen){
            closeMenu(true, true, true);
        }else{
            openMenu(true, true, true);
        }
    }

    private void openMenu(boolean changeMenuIcon, boolean withAnim, boolean notifyListener){

        if (onMenuClickListener != null && notifyListener)
            onMenuClickListener.onMenuOpened();

        isMenuOpen = true;
        if (changeMenuIcon)
            openMenuDrawable(menuBtnDrawable, withAnim);
    }

    private void closeMenu(boolean changeMenuIcon, boolean withAnim, boolean notifyListener) {

        if (onMenuClickListener != null && notifyListener)
            onMenuClickListener.onMenuClosed();

        isMenuOpen = false;
        if(changeMenuIcon)
            closeMenuDrawable(menuBtnDrawable, withAnim);
    }

    /**
     * todo go over
     *
     * Enables clients to directly manipulate
     * the menu icon's progress.
     *
     * <p>Useful for custom animation/behaviors.</p>
     *
     * @param progress the desired progress of the menu
     *                 icon's rotation. 0.0 == hamburger
     *                 shape, 1.0 == back arrow shape
     */
    public void setMenuIconProgress(float progress){

        menuBtnDrawable.setProgress(progress);

        if(progress == 0)
            closeMenu(false, true, true);
        else if(progress == 1.0)
            openMenu(false);
    }

    /**
     * Wrapper implementation for EditText.setFocusable(boolean focusable)
     *
     * @param focusable true, to make search focus when
     *                  clicked.
     */
    public void setSearchFocusable(boolean focusable){
        searchInput.setFocusable(focusable);
    }

    /**
     * Set a hint that will appear in the
     * search input. Default hint is R.string.abc_search_hint
     * which is "search..." (when device language is set to english)
     *
     * @param searchHint
     */
    public void setSearchHint(String searchHint){

        this.searchHint = searchHint != null ? searchHint : getResources().getString(R.string.abc_search_hint);
        searchInput.setHint(this.searchHint);
    }

    /**
     * Sets the title for the search bar.
     *
     * <p>Note that after the title is set, when
     * the search gains focus, the title will be replaced
     * by the search hint.</p>
     *
     * @param title the title to be shown when search
     *              is not focused
     */
    public void setSearchBarTitle(CharSequence title){

        this.titleText = title.toString();
        isTitleSet = true;
        searchInput.setText(title);
    }

    /**
     * Sets the search text.
     *
     * <p>Note that this is the different from
     * {@link #setSearchBarTitle(CharSequence title) setSearchBarTitle} in
     * that it keeps the text when the search gains focus.</p>
     *
     * @param text the text to be set for the search
     *             input.
     */
    public void setSearchText(CharSequence text){

        isTitleSet = false;
        searchInput.setText(text);
    }

    /**
     * Sets whether the the button with the search icon
     * will appear in the soft-keyboard or not.
     *
     * <p>Notice that if this is set to false,
     * {@link OnSearchListener#onSearchAction()} onSearchAction}, will
     * not get called.</p>
     *
     * @param show to show the search button in
     *             the soft-keyboard.
     */
    public void setShowSearchKey(boolean show){
        showSearchKey = show;
        if(show)
            searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        else
            searchInput.setImeOptions(EditorInfo.IME_ACTION_NONE);
    }

    /**
     * Returns the current query text.
     *
     * @return the current query
     */
    public String getQuery(){

        return searchInput.getText().toString();
    }

    /**
     * Shows a circular progress on top of the
     * menu action button.
     *
     * <p>Call hidProgress()
     * to change back to normal and make the menu
     * action visible.</p>
     */
    public void showProgress(){

        leftAction.setVisibility(View.GONE);
        searchProgress.setVisibility(View.VISIBLE);
        ObjectAnimator fadeInProgress = new ObjectAnimator().ofFloat(searchProgress, "alpha", 0.0f, 1.0f);
        fadeInProgress.start();
    }

    /**
     * Hides the progress bar after
     * a prior call to showProgress()
     */
    public void hideProgress() {

        leftAction.setVisibility(View.VISIBLE);
        searchProgress.setVisibility(View.GONE);
        ObjectAnimator fadeInExit = new ObjectAnimator().ofFloat(leftAction, "alpha", 0.0f, 1.0f);
        fadeInExit.start();
    }

    /**
     * Sets whether the search is focused or not.
     *
     * @param focused true, to set the search to be active/focused.
     */
    public void setSearchFocused(boolean focused) {

        if(!this.isFocused && onSuggestionSecHeightListener ==null){

            if(isSuggestionsSecHeightSet){
                setSearchFocusedInternal(true);
            }else{

                onSuggestionSecHeightListener = new OnSuggestionSecHeightSetListener() {
                    @Override
                    public void onSuggestionSecHeightSet() {
                        setSearchFocusedInternal(true);
                        onSuggestionSecHeightListener = null;
                    }
                };
            }
        }
    }

    public void setOnBindSuggestionCallback(SearchSuggestionsAdapter.OnBindSuggestionCallback callback){
        this.onBindSuggestionCallback = callback;

        if(suggestionsAdapter !=null)
            suggestionsAdapter.setOnBindSuggestionCallback(onBindSuggestionCallback);
    }

    public void setSuggestionRightIconColor(int color){
        this.suggestionRightIconColor = color;

        if(suggestionsAdapter !=null)
            suggestionsAdapter.setRightIconColor(this.suggestionRightIconColor);
    }

    private void setSuggestionItemTextSize(int sizePx){

        this.suggestionsTextSizePx = sizePx;
        //setup adapter and make method public
    }

    private void setupSuggestionSection() {

        boolean showItemsFromBottom = true;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, showItemsFromBottom);
        suggestionsList.setLayoutManager(layoutManager);
        suggestionsList.setItemAnimator(null);

        final GestureDetector gestureDetector = new GestureDetector(getContext(),new GestureDetectorListenerAdapter(){

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                if(hostActivity !=null)
                    Util.closeSoftKeyboard(hostActivity);

                return false;
            }
        });

        suggestionsList.addOnItemTouchListener(new OnItemTouchListenerAdapter() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }
        });

        suggestionsAdapter = new SearchSuggestionsAdapter(getContext(), suggestionsTextSizePx, new SearchSuggestionsAdapter.Listener() {

            @Override
            public void onItemSelected(SearchSuggestion item) {

                setSearchFocusedInternal(false);

                if(searchListener !=null)
                    searchListener.onSuggestionClicked(item);
            }

            @Override
            public void onMoveItemToSearchClicked(SearchSuggestion item) {

                searchInput.setText(item.getBody());

                //move cursor to end of text
                searchInput.setSelection(searchInput.getText().length());
            }
        });

        suggestionsAdapter.setTextColor(this.suggestionTextColor);
        suggestionsAdapter.setRightIconColor(this.suggestionRightIconColor);

        suggestionsList.setAdapter(suggestionsAdapter);

        int cardViewBottomPadding = Util.dpToPx(5);

        //move up the suggestions section enough to cover the search bar
        //card's bottom left and right corners
        suggestionsSection.setTranslationY(-cardViewBottomPadding);

    }

    private void moveSuggestListToInitialPos(){

        //move the suggestions list to the collapsed position
        //which is translationY of -listHeight
        suggestionListContainer.setTranslationY(-suggestionListContainer.getMeasuredHeight());
    }

    /**
     * Clears the current suggestions and replaces it
     * with the provided list of new suggestions.
     *
     * @param newSearchSuggestions a list containing the new suggestions
     */
    public void swapSuggestions(final List<? extends SearchSuggestion> newSearchSuggestions){
        Collections.reverse(newSearchSuggestions);
        swapSuggestions(newSearchSuggestions, true);
    }

    private void swapSuggestions(final List<? extends SearchSuggestion> newSearchSuggestions, boolean withAnim){
        divider.setVisibility(View.GONE);
        //update adapter
        suggestionsAdapter.swapData(newSearchSuggestions);

        //todo inspect line
        //this is needed because the list gets populated
        //from bottom up.
        suggestionsList.scrollBy(0, -(newSearchSuggestions.size() * getTotalItemsHeight(newSearchSuggestions)));

        int fiveDp = Util.dpToPx(6);
        int threeDp = Util.dpToPx(3);
        ViewCompat.animate(suggestionListContainer).cancel();
        float translationY = (-suggestionListContainer.getHeight())+getVisibleItemsHeight(newSearchSuggestions);

        Log.d("swapSuggestions", translationY+"");
        //todo refactor go over and make more clear
        final float newTranslationY = (translationY+fiveDp)<0 ?
                newSearchSuggestions.size()==0 ? translationY : translationY+threeDp
                : -fiveDp;

        if(withAnim) {
            ViewCompat.animate(suggestionListContainer).
                    setStartDelay(SUGGESTION_ITEM_ANIM_DURATION).
                    setInterpolator(SUGGEST_ITEM_ADD_ANIM_INTERPOLATOR).
                    setDuration(SUGGEST_ITEM_ADD_ANIM_DURATION).
                    translationY(newTranslationY).
                    setListener(new ViewPropertyAnimatorListenerAdapter() {


                        @Override
                        public void onAnimationCancel(View view) {

                            suggestionListContainer.setTranslationY(newTranslationY);
                        }
                    }).start();
        }else{

            suggestionListContainer.setTranslationY(newTranslationY);
        }

        if(newSearchSuggestions.size()>0)
            divider.setVisibility(View.VISIBLE);
        else
            divider.setVisibility(View.GONE);
    }

    //returns the height that a given suggestion list's items
    //will take up.
    private int getVisibleItemsHeight(List<? extends SearchSuggestion> suggestions){

        int visibleItemsHeight = 0;

        for(SearchSuggestion suggestion: suggestions) {
            visibleItemsHeight += getSuggestionItemHeight(suggestion);

            //if the current total is more than the list container's height, we
            //don't care about the rest of the items' heights because they won't be
            //visible.
            if(visibleItemsHeight> suggestionListContainer.getHeight())
                break;
        }

        return visibleItemsHeight;
    }

    private int getTotalItemsHeight(List<? extends SearchSuggestion> suggestions){

        int totalItemHeight = 0;

        for(SearchSuggestion suggestion: suggestions)
            totalItemHeight += getSuggestionItemHeight(suggestion);

        return totalItemHeight;
    }

    //returns the height of a given suggestion item based on it's text length
    private int getSuggestionItemHeight(SearchSuggestion suggestion) {
        int leftRightMarginsWidth = Util.dpToPx(124);

        //todo improve efficiency
        TextView textView = new TextView(getContext());
        textView.setTypeface(Typeface.DEFAULT);
        textView.setText(suggestion.getBody(), TextView.BufferType.SPANNABLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, suggestionsTextSizePx);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(suggestionsList.getWidth()-leftRightMarginsWidth, MeasureSpec.AT_MOST);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        int heightPlusPadding = textView.getMeasuredHeight()+Util.dpToPx(8);
        int minHeight = Util.dpToPx(48);
        int height = heightPlusPadding >= minHeight ? heightPlusPadding : minHeight;
        return heightPlusPadding >= minHeight ? heightPlusPadding : minHeight;
    }

    /**
     * Collapses the suggestions list and
     * then clears its suggestion items.
     */
    public void clearSuggestions(){
        clearSuggestions(null);
    }

    private interface OnSuggestionsClearListener{

        void onCleared();
    }

    private void clearSuggestions(final OnSuggestionsClearListener listener) {

        if(!isCollapsing) {

            collapseSuggestionsSection(new OnSuggestionsCollapsedListener() {
                @Override
                public void onCollapsed() {

                    suggestionsAdapter.clearDataSet();

                    if (listener != null)
                        listener.onCleared();

                    divider.setVisibility(GONE);
                }
            });
        }
    }

    private interface OnSuggestionsCollapsedListener{

        void onCollapsed();
    }

    private void collapseSuggestionsSection(final OnSuggestionsCollapsedListener listener){

        isCollapsing = true;

        final int destTranslationY = -(suggestionListContainer.getHeight()+Util.dpToPx(3));

        ViewCompat.animate(suggestionListContainer).
                translationY(destTranslationY).
                setDuration(SUGGEST_LIST_COLLAPSE_ANIM_DURATION).
                setListener(new ViewPropertyAnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(View view) {

                        if (listener != null)
                            listener.onCollapsed();

                        isCollapsing = false;
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                        suggestionListContainer.setTranslationY(destTranslationY);
                    }
                }).start();
    }

    public void clearSearchFocus(){
        setSearchFocusedInternal(false);
    }

    public boolean isSearchBarFocused(){
        return isFocused;
    }

    private void setSearchFocusedInternal(boolean focused){

        this.isFocused = focused;

        if(focused){

            leftAction.setVisibility(View.VISIBLE);

            transitionInLeftSection(true);

            if(isMenuOpen)
                closeMenu(false, true, true);

            moveSuggestListToInitialPos();
            suggestionsSection.setVisibility(VISIBLE);

            fadeInBackground();

            searchInput.requestFocus();

            if(isTitleSet) {
                skipTextChangeEvent = true;
                searchInput.setText("");
            }

            menuView.hideIfRoomItems(true);

            Util.showSoftKeyboard(getContext(), searchInput);

            if(focusChangeListener !=null)
                focusChangeListener.onFocus();
        }else{

            transitionOutLeftSection(true);

            clearSuggestions(new OnSuggestionsClearListener() {
                @Override
                public void onCleared() {

                    suggestionsSection.setVisibility(View.INVISIBLE);
                }
            });

            fadeOutBackground();

            findViewById(R.id.search_bar).requestFocus();

            if(hostActivity !=null)
                Util.closeSoftKeyboard(hostActivity);

            menuView.showIfRoomItems(true);

            clearButton.setVisibility(View.INVISIBLE);

            if(searchInput.length()!=0)
                searchInput.setText("");

            if(isTitleSet) {
                skipTextChangeEvent = true;
                searchInput.setText(titleText);
            }

            if(focusChangeListener !=null) {
                focusChangeListener.onFocusCleared();
            }
        }
    }

    private void changeIcon(ImageView imageView, Drawable newIcon, boolean withAnim) {

        imageView.setImageDrawable(newIcon);
        if(withAnim) {
            ObjectAnimator fadeInVoiceInputOrClear = new ObjectAnimator().ofFloat(imageView, "alpha", 0.0f, 1.0f);
            fadeInVoiceInputOrClear.start();
        }else{
            imageView.setAlpha(1.0f);
        }
    }

    private void transitionInLeftSection(boolean withAnim){

        switch (leftActionMode){

            case LEFT_ACTION_MODE_SHOW_HAMBURGER_ENUM_VAL:{
                openMenuDrawable(menuBtnDrawable, withAnim);
                if(!isMenuOpen)
                    break;
            }break;
            case LEFT_ACTION_MODE_SHOW_SEARCH_ENUM_VAL:{

                leftAction.setImageDrawable(iconBackArrow);

                if(withAnim){
                    leftAction.setRotation(45);
                    leftAction.setAlpha(0.0f);
                    ObjectAnimator rotateAnim = ViewPropertyObjectAnimator.animate(leftAction).rotation(0).get();
                    ObjectAnimator fadeAnim = ViewPropertyObjectAnimator.animate(leftAction).alpha(1.0f).get();
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.setDuration(500);
                    animSet.playTogether(rotateAnim,fadeAnim);
                    animSet.start();
                }
            }break;
            case LEFT_ACTION_MODE_SHOW_HOME_ENUM_VAL:{
                //do nothing
            }break;
            case LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL:{

                leftAction.setImageDrawable(iconBackArrow);

                if(withAnim){

                    ObjectAnimator searchInputTransXAnim = ViewPropertyObjectAnimator.animate(searchInputParent).translationX(0).get();

                    leftAction.setScaleX(0.5f);
                    leftAction.setScaleY(0.5f);
                    leftAction.setAlpha(0.0f);
                    leftAction.setTranslationX(Util.dpToPx(8));
                    ObjectAnimator transXArrowAnim = ViewPropertyObjectAnimator.animate(leftAction).translationX(1.0f).get();
                    ObjectAnimator scaleXArrowAnim = ViewPropertyObjectAnimator.animate(leftAction).scaleX(1.0f).get();
                    ObjectAnimator scaleYArrowAnim = ViewPropertyObjectAnimator.animate(leftAction).scaleY(1.0f).get();
                    ObjectAnimator fadeArrowAnim = ViewPropertyObjectAnimator.animate(leftAction).alpha(1.0f).get();
                    transXArrowAnim.setStartDelay(150);
                    scaleXArrowAnim.setStartDelay(150);
                    scaleYArrowAnim.setStartDelay(150);
                    fadeArrowAnim.setStartDelay(150);

                    AnimatorSet animSet = new AnimatorSet();
                    animSet.setDuration(500);
                    animSet.playTogether(searchInputTransXAnim,transXArrowAnim, scaleXArrowAnim,scaleYArrowAnim,fadeArrowAnim);
                    animSet.start();
                }else{

                    searchInputParent.setTranslationX(0);
                }
            }
        }
    }

    private void transitionOutLeftSection(boolean withAnim){

        switch (leftActionMode){

            case LEFT_ACTION_MODE_SHOW_HAMBURGER_ENUM_VAL:{
                closeMenuDrawable(menuBtnDrawable, withAnim);
            }break;
            case LEFT_ACTION_MODE_SHOW_SEARCH_ENUM_VAL:{
                changeIcon(leftAction, iconSearch, withAnim);
            }break;

            case LEFT_ACTION_MODE_SHOW_HOME_ENUM_VAL:{
                //do nothing
            }break;
            case LEFT_ACTION_MODE_NO_LEFT_ACTION_ENUM_VAL:{

                leftAction.setImageDrawable(iconBackArrow);

                if(withAnim){
                    ObjectAnimator searchInputTransXAnim = ViewPropertyObjectAnimator.animate(searchInputParent)
                            .translationX(-Util.dpToPx(48 + 20 - 16)).get();

                    ObjectAnimator scaleXArrowAnim = ViewPropertyObjectAnimator.animate(leftAction).scaleX(0.5f).get();
                    ObjectAnimator scaleYArrowAnim = ViewPropertyObjectAnimator.animate(leftAction).scaleY(0.5f).get();
                    ObjectAnimator fadeArrowAnim = ViewPropertyObjectAnimator.animate(leftAction).alpha(0.5f).get();
                    scaleXArrowAnim.setDuration(300);
                    scaleYArrowAnim.setDuration(300);
                    fadeArrowAnim.setDuration(300);
                    scaleXArrowAnim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            //restore normal state
                            leftAction.setScaleX(1.0f);
                            leftAction.setScaleY(1.0f);
                            leftAction.setAlpha(1.0f);
                            leftAction.setVisibility(View.INVISIBLE);
                        }
                    });

                    AnimatorSet animSet = new AnimatorSet();
                    animSet.setDuration(350);
                    animSet.playTogether(scaleXArrowAnim, scaleYArrowAnim, fadeArrowAnim, searchInputTransXAnim);
                    animSet.start();
                }else{

                    leftAction.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * Sets the listener that will listen for query
     * changes as they are being typed.
     *
     * @param listener listener for query changes
     */
    public void setOnQueryChangeListener(OnQueryChangeListener listener){
        this.queryListener = listener;
    }

    /**
     * Sets the listener that will be called when
     * an action that completes the current search
     * session has occurred and the search lost focus.
     *
     * <p>When called, a client would ideally grab the
     * search or suggestion query from the callback parameter or
     * from {@link #getQuery() getquery} and perform the necessary
     * query against its data source.</p>
     *
     * @param listener listener for query completion
     */
    public void setOnSearchListener(OnSearchListener listener) {
        this.searchListener = listener;
    }

    /**
     * Sets the listener that will be called when the focus
     * of the search has changed.
     *
     * @param listener listener for search focus changes
     */
    public void setOnFocusChangeListener(OnFocusChangeListener listener){
        this.focusChangeListener = listener;
    }

    /**
     * Sets the listener that will be called when the
     * left/start menu (or navigation menu) is clicked.
     *
     * <p>Note that this is different from the overflow menu
     * that has a separate listener.</p>
     *
     * @param listener
     */
    public void setOnLeftMenuClickListener(OnLeftMenuClickListener listener){
        this.onMenuClickListener = listener;
    }

    /**
     * Sets the listener that will be called when the
     * left/start home action (back arrow) is clicked.
     *
     * @param listener
     */
    public void setOnHomeActionClickListener(OnHomeActionClickListener listener){
        this.onHomeActionClickListener = listener;
    }

    /**
     * Sets the listener that will be called when
     * an item in the overflow menu is clicked.
     *
     * @param listener listener to listen to menu item clicks
     */
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener){
        this.actionMenuItemListener = listener;
        //todo reset menu view listener
    }

    private void openMenuDrawable(final DrawerArrowDrawable drawerArrowDrawable, boolean withAnim){

        if(withAnim){
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    float value = (Float) animation.getAnimatedValue();
                    drawerArrowDrawable.setProgress(value);
                }
            });
            anim.setDuration(MENU_ICON_ANIM_DURATION);
            anim.start();
        }else{
            drawerArrowDrawable.setProgress(1.0f);
        }
    }

    private void closeMenuDrawable(final DrawerArrowDrawable drawerArrowDrawable, boolean withAnim) {

        if(withAnim) {
            ValueAnimator anim = ValueAnimator.ofFloat(1.0f, 0.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    float value = (Float) animation.getAnimatedValue();
                    drawerArrowDrawable.setProgress(value);
                }
            });
            anim.setDuration(MENU_ICON_ANIM_DURATION);
            anim.start();
        }else{
            drawerArrowDrawable.setProgress(0.0f);
        }
    }

    private void fadeOutBackground(){

        ValueAnimator anim = ValueAnimator.ofInt(BACKGROUND_DRAWABLE_ALPHA_SEARCH_ACTIVE, BACKGROUND_DRAWABLE_ALPHA_SEARCH_INACTIVE);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (Integer) animation.getAnimatedValue();
                backgroundDrawable.setAlpha(value);
            }
        });
        anim.setDuration(BACKGROUND_FADE__ANIM_DURATION);
        anim.start();
    }

    private void fadeInBackground(){

        ValueAnimator anim = ValueAnimator.ofInt(BACKGROUND_DRAWABLE_ALPHA_SEARCH_INACTIVE, BACKGROUND_DRAWABLE_ALPHA_SEARCH_ACTIVE);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (Integer) animation.getAnimatedValue();
                backgroundDrawable.setAlpha(value);
            }
        });
        anim.setDuration(BACKGROUND_FADE__ANIM_DURATION);
        anim.start();
    }

    /**
     * Set whether a touch outside of the
     * search bar's bounds will cause the search bar to
     * loos focus.
     *
     * @param enable true to dismiss on outside touch, false otherwise.
     */
    public void setDismissOnOutsideClick(boolean enable){

        dismissOnOutsideTouch = enable;

        suggestionsSection.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //todo check if this is called twice
                if (dismissOnOutsideTouch && isFocused)
                    setSearchFocusedInternal(false);

                return true;
            }
        });
    }

    private boolean isRTL(){

        Configuration config =  getResources().getConfiguration();
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.suggestions = this.suggestionsAdapter.getDataSet();
        if(!this.suggestionsAdapter.getDataSet().isEmpty())
            savedState.suggestObjectCreator = this.suggestionsAdapter.getDataSet().get(0).getCreator();
        savedState.isFocused = this.isFocused;
        savedState.query = getQuery();
        savedState.suggestionTextSize = this.suggestionsTextSizePx;
        savedState.searchHint = this.searchHint;
        savedState.dismissOnOutsideClick = this.dismissOnOutsideTouch;
        savedState.showSearchKey = this.showSearchKey;
        savedState.searchCanFocus = this.searchCanFocus;
        savedState.isTitleSet = this.isTitleSet;
        //savedState.leftMode = this.leftActionMode;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        this.isFocused = savedState.isFocused;
        this.isTitleSet = savedState.isTitleSet;

        setSuggestionItemTextSize(savedState.suggestionTextSize);
        setDismissOnOutsideClick(savedState.dismissOnOutsideClick);
        setShowSearchKey(savedState.showSearchKey);
        //setLeftActionMode(savedState.leftMode);
        setSearchHint(savedState.searchHint);

        if(this.isFocused) {

            backgroundDrawable.setAlpha(BACKGROUND_DRAWABLE_ALPHA_SEARCH_ACTIVE);
            skipTextChangeEvent = true;
            skipQueryFocusChangeEvent = true;

            suggestionsSection.setVisibility(VISIBLE);

            //restore suggestions list when suggestion section's height is fully set
            onSuggestionSecHeightListener = new OnSuggestionSecHeightSetListener() {
                @Override
                public void onSuggestionSecHeightSet() {
                    swapSuggestions(savedState.suggestions, false);
                    onSuggestionSecHeightListener = null;

                    //todo refactor move to a better location
                    transitionInLeftSection(false);
                }
            };

            clearButton.setVisibility((savedState.query.length() == 0) ? View.INVISIBLE : View.VISIBLE);

            leftAction.setVisibility(View.VISIBLE);

            Util.showSoftKeyboard(getContext(), searchInput);
        }
    }

    static class SavedState extends BaseSavedState {

        private Creator suggestObjectCreator;

        private List<? extends SearchSuggestion> suggestions = new ArrayList<>();
        private boolean isFocused;
        private String query;
        private int suggestionTextSize;
        private String searchHint;
        private boolean dismissOnOutsideClick;
        private boolean showSearchKey;
        private boolean searchCanFocus;
        private boolean isTitleSet;

        //private int leftMode;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);

            if (suggestObjectCreator != null)
                in.readTypedList(suggestions, suggestObjectCreator);
            isFocused = (in.readInt() != 0);
            query = in.readString();
            suggestionTextSize = in.readInt();
            searchHint = in.readString();
            dismissOnOutsideClick = (in.readInt() != 0);
            showSearchKey = (in.readInt() != 0);
            searchCanFocus = (in.readInt() != 0);
            isTitleSet = (in.readInt() != 0);
            //leftMode = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeTypedList(suggestions);
            out.writeInt(isFocused ? 1 : 0);
            out.writeString(query);
            out.writeInt(suggestionTextSize);
            out.writeString(searchHint);
            out.writeInt(dismissOnOutsideClick ? 1 : 0);
            out.writeInt(showSearchKey ? 1 : 0);
            out.writeInt(searchCanFocus ? 1 : 0);
            out.writeInt(isTitleSet ? 1 : 0);
            //out.writeInt(leftMode);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        //remove any ongoing animations to prevent leaks
        //todo investigate if correct
        ViewCompat.animate(suggestionListContainer).cancel();
    }
}
