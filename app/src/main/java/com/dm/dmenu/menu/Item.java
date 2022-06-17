package com.dm.dmenu.menu;

import android.content.Intent;

public class Item {
    private final CharSequence title;
    private final Intent intent;

    public Item(CharSequence title, Intent intent) {
        this.title = title;
        this.intent = intent;
    }

    public CharSequence getTitle() {
        return title;
    }

    public Intent getIntent() {
        return intent;
    }
}
