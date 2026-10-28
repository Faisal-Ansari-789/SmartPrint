package com.example.SmartPrint;

import android.net.Uri;

public class PrintItem {
    public static final double BW = 2.0, COLOR = 8.0, DUPLEX_DISCOUNT = 0.5;

    public Uri uri;
    public String name;
    public int pages = 1;
    public boolean color = false;
    public boolean duplex = false;
    public int copies = 1;

    public PrintItem(Uri uri, String name, int pages) {
        this.uri = uri; this.name = name; this.pages = pages;
    }

    public double price() {
        double rate = color ? COLOR : BW;
        if (duplex) rate -= DUPLEX_DISCOUNT;
        return rate * pages * copies;
    }
}