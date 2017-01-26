package online.osslab.util.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import online.osslab.R;
import online.osslab.util.MenuPopupHelper;
import online.osslab.util.Util;
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public class MenuView extends LinearLayout {

    private final int HIDE_IF_ROOM_ITEMS_ANIM_DURATION = 400;
    private final int SHOW_IF_ROOM_ITEMS_ANIM_DURATION = 450;

    private final float ACTION_DIMENSION_PX;

    private int menu = -1;
    private MenuBuilder menuBuilder;
    private SupportMenuInflater menuInflater;
    private MenuPopupHelper menuPopupHelper;

    private MenuBuilder.Callback menuCallback;

    private int actionIconColor;
    private int overIconColor;

    //all menu items
    private List<MenuItemImpl> menuItems;

    //items that are currently presented as actions
    private List<MenuItemImpl> actionItems = new ArrayList<>();

    private List<MenuItemImpl> actionShowAlwaysItems = new ArrayList<>();

    private boolean hasOverflow = false;

    private OnVisibleWidthChanged onVisibleWidthChanged;

    private List<ObjectAnimator> anims = new ArrayList<>();

    public interface OnVisibleWidthChanged{
        void onVisibleWidthChanged(int newVisibleWidth);
    }

    public MenuView(Context context) {
        this(context, null);
    }

    public MenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ACTION_DIMENSION_PX = context.getResources().getDimension(R.dimen.square_button_size);
        init();
    }

    private void init(){
        menuBuilder = new MenuBuilder(getContext());
        menuPopupHelper = new MenuPopupHelper(getContext(), menuBuilder, this);

        actionIconColor = getResources().getColor(R.color.gray_active_icon);
        overIconColor = getResources().getColor(R.color.gray_active_icon);
    }

    public void setActionIconColor(int actionColor){
        this.actionIconColor = actionColor;
        refreshColors();
    }

    public void setOverflowColor(int overflowColor){
        this.overIconColor = overflowColor;
        refreshColors();
    }

    private void refreshColors(){

        for(int i=0; i<getChildCount(); i++){

            Util.setIconColor(((ImageView) getChildAt(i)).getDrawable(), actionIconColor);

            if(hasOverflow && i==getChildCount()-1)
                Util.setIconColor(((ImageView) getChildAt(i)).getDrawable(), overIconColor);

        }
    }

    /**
     * Sets the resource reference to the
     * menu defined in xml that will be used
     * in subsequent calls to {@link #reset(int availWidth) reset}
     *
     * @param menu a reference to a menu defined in
     *             resources.
     */
    public void resetMenuResource(int menu){

        this.menu = menu;
    }

    /**
     * Set the callback that will be called when menu
     * items a selected.
     *
     * @param menuCallback
     */
    public void setMenuCallback(MenuBuilder.Callback menuCallback){
        this.menuCallback = menuCallback;
    }

    /**
     * Resets the the view to fit into a new
     * available width.
     *
     * <p>This clears and then re-inflates the menu items
     * , removes all of its associated action views, and recreates
     * the menu and action items to fit in the new width.</p>
     *
     * @param availWidth the width available for the menu to use. If
     *                   there is room, menu items that are flagged with
     *                   android:showAsAction="ifRoom" or android:showAsAction="always"
     *                   will show as actions.
     */
    public void reset(int availWidth){

        if(menu ==-1)
            return;

        //clean view first
        removeAllViews();
        actionItems.clear();

        //reset menu
        menuBuilder.clearAll();
        getMenuInflater().inflate(menu, menuBuilder);

        int holdAllItemsCount;

        menuItems =  menuBuilder.getActionItems();
        menuItems.addAll(menuBuilder.getNonActionItems());

        holdAllItemsCount = menuItems.size();

        Collections.sort(menuItems, new Comparator<MenuItemImpl>() {
            @Override
            public int compare(MenuItemImpl lhs, MenuItemImpl rhs) {
                return ((Integer) lhs.getOrder()).compareTo(rhs.getOrder());
            }
        });

        List<MenuItemImpl> menuItems = filter(this.menuItems, new MenuItemImplPredicate() {
            @Override
            public boolean apply(MenuItemImpl menuItem) {
                return menuItem.requiresActionButton() || menuItem.requestsActionButton();
            }
        });

        int availItemRoom = availWidth/(int)ACTION_DIMENSION_PX;
        boolean addOverflowAtTheEnd = false;
        if(((menuItems.size()<holdAllItemsCount) || availItemRoom<menuItems.size())){
            addOverflowAtTheEnd = true;
            availItemRoom--;
        }

        ArrayList<Integer> actionMenuItems = new ArrayList<>();

        if(availItemRoom>0)
            for(int i=0; i<menuItems.size(); i++){

                final MenuItemImpl menuItem = menuItems.get(i);

                if(menuItem.getIcon()!=null){

                    ImageView action = getActionHolder();
                    action.setImageDrawable(Util.setIconColor(menuItem.getIcon(), actionIconColor));
                    addView(action);
                    actionItems.add(menuItem);

                    action.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(menuCallback !=null)
                                menuCallback.onMenuItemSelected(menuBuilder, menuItem);
                        }
                    });

                    actionMenuItems.add(menuItem.getItemId());

                    availItemRoom--;
                    if(availItemRoom==0)
                        break;
                }
            }

        if(addOverflowAtTheEnd){

            ImageView overflowAction = getOverflowActionHolder();
            overflowAction.setImageDrawable(Util.setIconColor(
                    getResources().getDrawable(R.drawable.ic_more_vert_black_24dp), overIconColor));
            addView(overflowAction);

            overflowAction.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    menuPopupHelper.show();
                }
            });

            menuBuilder.setCallback(menuCallback);

            hasOverflow = true;
        }

        for(int id: actionMenuItems)
            menuBuilder.removeItem(id);

        actionMenuItems.clear();

        if(onVisibleWidthChanged !=null)
            onVisibleWidthChanged.onVisibleWidthChanged(((int)ACTION_DIMENSION_PX * getChildCount())- (hasOverflow ? Util.dpToPx(8) : 0));
    }

    private ImageView getActionHolder(){
        return (ImageView)LayoutInflater.from(getContext()).inflate(R.layout.action_item_layout, this, false);
    }

    private ImageView getOverflowActionHolder(){
        return (ImageView)LayoutInflater.from(getContext()).inflate(R.layout.overflow_action_item_layout, this, false);
    }

    /**
     * Hides all the menu items flagged with "ifRoom"
     *
     * @param withAnim
     */
    public void hideIfRoomItems(boolean withAnim){

        if(menu ==-1)
            return;

        actionShowAlwaysItems.clear();
        cancelChildAnimListAndClear();

        List<MenuItemImpl> showAlwaysActionItems = filter(menuItems,new MenuItemImplPredicate() {
            @Override
            public boolean apply(MenuItemImpl menuItem) {
                return menuItem.requiresActionButton();
            }
        });

        int actionItemIndex;
        for(actionItemIndex=0;
            actionItemIndex< actionItems.size() && actionItemIndex<showAlwaysActionItems.size();
            actionItemIndex++){

            final MenuItemImpl actionItem = showAlwaysActionItems.get(actionItemIndex);

            if(actionItems.get(actionItemIndex).getItemId()!=showAlwaysActionItems.get(actionItemIndex).getItemId()){

                ImageView action = (ImageView)getChildAt(actionItemIndex);
                action.setImageDrawable(Util.setIconColor(actionItem.getIcon(), actionIconColor));

                action.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (menuCallback != null)
                            menuCallback.onMenuItemSelected(menuBuilder, actionItem);
                    }
                });

            }

            actionShowAlwaysItems.add(actionItem);
        }

        final int diff = actionItems.size()-actionItemIndex+(hasOverflow ?1:0);

        anims = new ArrayList<>();

        for(int i=0; i<actionItemIndex; i++) {
            final View currentChild = getChildAt(i);
            final float destTransX = ACTION_DIMENSION_PX * diff - (hasOverflow ? Util.dpToPx(8) : 0);
            anims.add(ViewPropertyObjectAnimator.animate(currentChild)
                    .setDuration(withAnim ? HIDE_IF_ROOM_ITEMS_ANIM_DURATION : 0)
                    .setInterpolator(new AccelerateInterpolator())
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentChild.setTranslationX(destTransX);
                        }
                    })
                    .translationXBy(destTransX).get());
        }

        for(int i=actionItemIndex; i<diff+actionItemIndex; i++){

            final View currentView = getChildAt(i);

            currentView.setClickable(false);

            if(i!=getChildCount()-1)
                anims.add(ViewPropertyObjectAnimator.animate(currentView).setDuration(withAnim ? HIDE_IF_ROOM_ITEMS_ANIM_DURATION : 0)
                        .addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {

                                currentView.setTranslationX(ACTION_DIMENSION_PX);
                            }
                        }).translationXBy(ACTION_DIMENSION_PX).get());

            anims.add(ViewPropertyObjectAnimator.animate(currentView).setDuration(withAnim ? HIDE_IF_ROOM_ITEMS_ANIM_DURATION : 0)
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentView.setScaleX(0.5f);
                        }
                    }).scaleX(.5f).get());
            anims.add(ViewPropertyObjectAnimator.animate(currentView).setDuration(withAnim ? HIDE_IF_ROOM_ITEMS_ANIM_DURATION : 0)
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentView.setScaleY(0.5f);
                        }
                    }).scaleY(.5f).get());
            anims.add(ViewPropertyObjectAnimator.animate(getChildAt(i)).setDuration(withAnim ? HIDE_IF_ROOM_ITEMS_ANIM_DURATION : 0)
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentView.setAlpha(0.0f);
                        }
                    }).alpha(0.0f).get());
        }

        final int actinItemsCount = actionItemIndex;
        if(!anims.isEmpty()){

            AnimatorSet animSet = new AnimatorSet();
            if(!withAnim)
                animSet.setDuration(0);
            animSet.playTogether(anims.toArray(new ObjectAnimator[anims.size()]));
            animSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    if(onVisibleWidthChanged !=null)
                        onVisibleWidthChanged.onVisibleWidthChanged(((int)ACTION_DIMENSION_PX * actinItemsCount));
                }
            });
            animSet.start();
        }

    }

    /**
     * Shows all the menu items that were hidden by hideIfRoomItems(boolean withAnim)
     *
     * @param withAnim
     */
    public void showIfRoomItems(boolean withAnim){


        if(menu ==-1)
            return;

        cancelChildAnimListAndClear();

        if(menuItems.isEmpty())
            return;

        anims = new ArrayList<>();

        for(int i=0; i<getChildCount(); i++){

            final View currentView = getChildAt(i);

            if(i< actionItems.size()){
                ImageView action = (ImageView)currentView;
                final MenuItem actionItem = actionItems.get(i);
                action.setImageDrawable(Util.setIconColor(actionItem.getIcon(), actionIconColor));

                action.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(menuCallback !=null)
                            menuCallback.onMenuItemSelected(menuBuilder, actionItem);
                    }
                });
            }

            //todo go over logic
            int animDuration = withAnim ?
                    SHOW_IF_ROOM_ITEMS_ANIM_DURATION
                    : 0;

            Interpolator interpolator = new DecelerateInterpolator();

            //todo check logic
            if(i> actionShowAlwaysItems.size()-1)
                interpolator = new LinearInterpolator();

            currentView.setClickable(true);
            anims.add(ViewPropertyObjectAnimator.animate(currentView)
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentView.setTranslationX(0);
                        }
                    })
                    .setInterpolator(interpolator)
                    .setDuration(animDuration)
                    .translationX(0).get());
            anims.add(ViewPropertyObjectAnimator.animate(currentView)
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentView.setScaleX(1.0f);
                        }
                    })
                    .setInterpolator(interpolator)
                    .setDuration(animDuration)
                    .scaleX(1.0f).get());
            anims.add(ViewPropertyObjectAnimator.animate(currentView)
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentView.setScaleY(1.0f);
                        }
                    })
                    .setInterpolator(interpolator)
                    .setDuration(animDuration)
                    .scaleY(1.0f).get());
            anims.add(ViewPropertyObjectAnimator.animate(currentView)
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            currentView.setAlpha(1.0f);
                        }
                    })
                    .setInterpolator(interpolator)
                    .setDuration(animDuration)
                    .alpha(1.0f).get());
        }

        AnimatorSet animSet = new AnimatorSet();

        //temporary, from laziness
        if(!withAnim)
            animSet.setDuration(0);
        animSet.playTogether(anims.toArray(new ObjectAnimator[anims.size()]));
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                if(onVisibleWidthChanged !=null)
                    onVisibleWidthChanged.onVisibleWidthChanged((getChildCount() * (int) ACTION_DIMENSION_PX)- (hasOverflow ? Util.dpToPx(8) : 0));
            }
        });
        animSet.start();

    }

    private interface MenuItemImplPredicate{

        boolean apply(MenuItemImpl menuItem);
    }

    private List<MenuItemImpl> filter(List<MenuItemImpl> target, MenuItemImplPredicate predicate) {
        List<MenuItemImpl> result = new ArrayList<>();
        for (MenuItemImpl element: target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    private MenuInflater getMenuInflater() {
        if (menuInflater == null) {
            menuInflater = new SupportMenuInflater(getContext());
        }
        return menuInflater;
    }

    public void setOnVisibleWidthChanged(OnVisibleWidthChanged listener){
        this.onVisibleWidthChanged = listener;
    }

    private void cancelChildAnimListAndClear(){

        for(ObjectAnimator animator: anims)
            animator.cancel();
        anims.clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        //clear anims if any to avoid leak
        cancelChildAnimListAndClear();
    }
}
