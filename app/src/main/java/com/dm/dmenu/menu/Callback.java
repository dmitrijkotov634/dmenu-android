package com.dm.dmenu.menu;

public interface Callback {
    void onItemSelected(Item item);

    void scrollTo(int position);
}
