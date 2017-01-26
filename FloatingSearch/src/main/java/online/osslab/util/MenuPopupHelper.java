package online.osslab.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.MenuView;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.ListPopupWindow;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;

import java.util.ArrayList;

import online.osslab.R;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public class MenuPopupHelper implements AdapterView.OnItemClickListener, View.OnKeyListener,
        ViewTreeObserver.OnGlobalLayoutListener, PopupWindow.OnDismissListener,
        MenuPresenter {
    private static final String TAG = "MenuPopupHelper";
    static final int ITEM_LAYOUT = R.layout.abc_popup_menu_item_layout;
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final MenuBuilder menuBuilder;
    private final MenuAdapter menuAdapter;
    private final boolean overflowOnly;
    private final int popupMaxWidth;
    private final int popupStyleAttr;
    private final int popupStyleRes;
    private View anchorView;
    private ListPopupWindow popupWindow;
    private ViewTreeObserver treeObserver;
    private Callback presenterCallback;
    boolean isForceShowIcon;
    private ViewGroup measureParent;
    /**
     * Whether the cached content width value is valid.
     */
    private boolean hasContentWidth;
    /**
     * Cached content width from {@link #measureContentWidth}.
     */
    private int contentWidth;
    private int dropDownGravity = Gravity.NO_GRAVITY;

    public MenuPopupHelper(Context context, MenuBuilder menu) {
        this(context, menu, null, false, R.attr.popupMenuStyle);
    }

    public MenuPopupHelper(Context context, MenuBuilder menu, View anchorView) {
        this(context, menu, anchorView, false, R.attr.popupMenuStyle);
    }

    public MenuPopupHelper(Context context, MenuBuilder menu, View anchorView,
                           boolean overflowOnly, int popupStyleAttr) {
        this(context, menu, anchorView, overflowOnly, popupStyleAttr, 0);
    }

    public MenuPopupHelper(Context context, MenuBuilder menu, View anchorView,
                           boolean overflowOnly, int popupStyleAttr, int popupStyleRes) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        menuBuilder = menu;
        menuAdapter = new MenuAdapter(menuBuilder);
        this.overflowOnly = overflowOnly;
        this.popupStyleAttr = popupStyleAttr;
        this.popupStyleRes = popupStyleRes;
        final Resources res = context.getResources();
        popupMaxWidth = Math.max(res.getDisplayMetrics().widthPixels / 2,
                res.getDimensionPixelSize(R.dimen.abc_config_prefDialogWidth));
        this.anchorView = anchorView;
        // Present the menu using our context, not the menu builder's context.
        menu.addMenuPresenter(this, context);
    }

    public float offsetX;

    public float offsetY;

    public void setOffsetX(float x) {
        this.offsetX = x;
    }

    public void setOffsetY(float y) {
        this.offsetY = y;
    }

    public void setAnchorView(View anchor) {
        anchorView = anchor;
    }

    public void setForceShowIcon(boolean isForceShow) {
        isForceShowIcon = isForceShow;
    }

    public void setGravity(int gravity) {
        dropDownGravity = gravity;
    }

    public int getGravity() {
        return dropDownGravity;
    }

    public void show() {
        if (!tryShow()) {
            throw new IllegalStateException("MenuPopupHelper cannot be used without an anchor");
        }
    }

    public ListPopupWindow getPopup() {
        return popupWindow;
    }

    public boolean tryShow() {
        popupWindow = new ListPopupWindow(context, null, popupStyleAttr, popupStyleRes);
        popupWindow.setOnDismissListener(this);
        popupWindow.setOnItemClickListener(this);
        popupWindow.setAdapter(menuAdapter);
        popupWindow.setModal(true);
        View anchor = anchorView;
        if (anchor != null) {
            final boolean addGlobalListener = treeObserver == null;
            treeObserver = anchor.getViewTreeObserver(); // Refresh to latest
            if (addGlobalListener) treeObserver.addOnGlobalLayoutListener(this);
            popupWindow.setAnchorView(anchor);
            popupWindow.setDropDownGravity(dropDownGravity);
        } else {
            return false;
        }
        if (!hasContentWidth) {
            contentWidth = measureContentWidth();
            hasContentWidth = true;
        }
        popupWindow.setContentWidth(contentWidth);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);

        int vertOffset = -anchorView.getHeight() + Util.dpToPx(4);
        int horizontalOffset = -contentWidth + anchorView.getWidth();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            vertOffset = -anchorView.getHeight() - Util.dpToPx(4);
            horizontalOffset = -contentWidth + anchorView.getWidth() - Util.dpToPx(8);
        }
        popupWindow.setVerticalOffset(vertOffset);
        popupWindow.setHorizontalOffset(horizontalOffset);
        popupWindow.show();
        popupWindow.getListView().setOnKeyListener(this);
        return true;
    }

    public void dismiss() {
        if (isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void onDismiss() {
        popupWindow = null;
        menuBuilder.close();
        if (treeObserver != null) {
            if (!treeObserver.isAlive()) treeObserver = anchorView.getViewTreeObserver();
            treeObserver.removeGlobalOnLayoutListener(this);
            treeObserver = null;
        }
    }

    public boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuAdapter adapter = menuAdapter;
        adapter.builder.performItemAction(adapter.getItem(position), 0);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
            dismiss();
            return true;
        }
        return false;
    }

    private int measureContentWidth() {
        // Menus don't tend to be long, so this is more sane than it looks.
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;
        final ListAdapter adapter = menuAdapter;
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            if (measureParent == null) {
                measureParent = new FrameLayout(context);
            }
            itemView = adapter.getView(i, itemView, measureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            final int itemWidth = itemView.getMeasuredWidth();
            if (itemWidth >= popupMaxWidth) {
                return popupMaxWidth;
            } else if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }
        return maxWidth;
    }

    @Override
    public void onGlobalLayout() {
        if (isShowing()) {
            final View anchor = anchorView;
            if (anchor == null || !anchor.isShown()) {
                dismiss();
            } else if (isShowing()) {
                // Recompute window size and position
                popupWindow.show();
            }
        }
    }

    @Override
    public void initForMenu(Context context, MenuBuilder menu) {
        // Don't need to do anything; we added as a presenter in the constructor.
    }

    @Override
    public MenuView getMenuView(ViewGroup root) {
        throw new UnsupportedOperationException("MenuPopupHelpers manage their own views");
    }

    @Override
    public void updateMenuView(boolean cleared) {
        hasContentWidth = false;
        if (menuAdapter != null) {
            menuAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setCallback(Callback callback) {
        presenterCallback = callback;
    }

    @Override
    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (subMenu.hasVisibleItems()) {
            MenuPopupHelper subPopup = new MenuPopupHelper(context, subMenu, anchorView);
            subPopup.setCallback(presenterCallback);
            boolean preserveIconSpacing = false;
            final int count = subMenu.size();
            for (int i = 0; i < count; i++) {
                MenuItem childItem = subMenu.getItem(i);
                if (childItem.isVisible() && childItem.getIcon() != null) {
                    preserveIconSpacing = true;
                    break;
                }
            }
            subPopup.setForceShowIcon(preserveIconSpacing);
            if (subPopup.tryShow()) {
                if (presenterCallback != null) {
                    presenterCallback.onOpenSubMenu(subMenu);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        // Only care about the (sub)menu we're presenting.
        if (menu != menuBuilder) return;
        dismiss();
        if (presenterCallback != null) {
            presenterCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    @Override
    public boolean flagActionItems() {
        return false;
    }

    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return null;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
    }

    private class MenuAdapter extends BaseAdapter {
        private MenuBuilder builder;
        private int index = -1;

        public MenuAdapter(MenuBuilder builder) {
            this.builder = builder;
            findExpandedIndex();
        }

        public int getCount() {
            ArrayList<MenuItemImpl> items = overflowOnly ?
                    builder.getNonActionItems() : builder.getVisibleItems();
            if (index < 0) {
                return items.size();
            }
            return items.size() - 1;
        }

        public MenuItemImpl getItem(int position) {
            ArrayList<MenuItemImpl> items = overflowOnly ?
                    builder.getNonActionItems() : builder.getVisibleItems();
            if (index >= 0 && position >= index) {
                position++;
            }
            return items.get(position);
        }

        public long getItemId(int position) {
            // Since a menu item's ID is optional, we'll use the position as an
            // ID for the item in the AdapterView
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(ITEM_LAYOUT, parent, false);
            }
            MenuView.ItemView itemView = (MenuView.ItemView) convertView;
            if (isForceShowIcon) {
                ((ListMenuItemView) convertView).setForceShowIcon(true);
            }
            itemView.initialize(getItem(position), 0);
            return convertView;
        }

        void findExpandedIndex() {
            final MenuItemImpl expandedItem = menuBuilder.getExpandedItem();
            if (expandedItem != null) {
                final ArrayList<MenuItemImpl> items = menuBuilder.getNonActionItems();
                final int count = items.size();
                for (int i = 0; i < count; i++) {
                    final MenuItemImpl item = items.get(i);
                    if (item == expandedItem) {
                        index = i;
                        return;
                    }
                }
            }
            index = -1;
        }

        @Override
        public void notifyDataSetChanged() {
            findExpandedIndex();
            super.notifyDataSetChanged();
        }
    }
}