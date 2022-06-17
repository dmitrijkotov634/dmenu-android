package com.dm.dmenu;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.dm.dmenu.databinding.MenuBinding;
import com.dm.dmenu.menu.Adapter;
import com.dm.dmenu.menu.Callback;
import com.dm.dmenu.menu.Item;

import java.util.ArrayList;
import java.util.List;

public class MenuService extends AccessibilityService implements Callback {

    private MenuBinding menuBinding;

    private List<Item> items;
    private Adapter adapter;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        items = new ArrayList<>();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.flags = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;

        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        setServiceInfo(info);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        menuBinding = MenuBinding.inflate(LayoutInflater.from(this));

        menuBinding.search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (adapter.getItemCount() > 0)
                    adapter.callOnClick(adapter.getPosition());
                return true;
            }

            return false;
        });

        menuBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Item> found = new ArrayList<>();

                String text = s.toString().toLowerCase();

                for (Item info : items) {
                    if (info.getTitle().toString().toLowerCase().contains(text))
                        found.add(info);
                }

                adapter = new Adapter(MenuService.this, found, MenuService.this);
                menuBinding.menu.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;

        wm.addView(menuBinding.getRoot(), lp);

        menuBinding.search.postDelayed(() -> {
            menuBinding.search.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(menuBinding.search, InputMethodManager.SHOW_IMPLICIT);
        }, 500);

        new Thread(() -> {
            PackageManager pm = getPackageManager();

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            for (ResolveInfo i : pm.queryIntentActivities(intent, 0)) {
                items.add(new Item(i.loadLabel(pm), new Intent()
                        .setClassName(i.activityInfo.applicationInfo.packageName,
                                i.activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
            }

            adapter = new Adapter(MenuService.this, items, MenuService.this);

            new Handler(Looper.getMainLooper()).post(() -> {
                menuBinding.menu.setLayoutManager(new LinearLayoutManager(MenuService.this, LinearLayoutManager.HORIZONTAL, false));
                menuBinding.menu.setAdapter(adapter);
            });
        }).start();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            return true;

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                adapter.left();
                return false;
            case KeyEvent.KEYCODE_VOLUME_UP:
                adapter.right();
                return false;
        }

        return true;
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onItemSelected(Item item) {
        startActivity(item.getIntent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableSelf();
        }
    }

    @Override
    public void scrollTo(int position) {
        menuBinding.menu.scrollToPosition(position);
    }
}
