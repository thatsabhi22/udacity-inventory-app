package com.theleafapps.pro.udacityinventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by aviator on 19/01/18.
 */

public class StockContract implements BaseColumns {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.theleafapps.pro.stock";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.theleafapps.pro.stock/stock/ is a valid path for
     * looking at stock data. content://com.theleafapps.pro.stock/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_STOCK = "stock";

    /**
     * To prevent someone from accidentally instantiating the contract class,
     * give it an empty constructor.
     */
    public StockContract() {
    }

    public static final class StockEntry implements BaseColumns {

        /**
         * The content URI to access the stock data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCK);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of stock.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single stock entry.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        /**
         * Name of database table for pets
         */
        public static final String TABLE_NAME = "stock";

        /**
         * Unique ID number for the stock (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the stock unit.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_NAME = "name";

        /**
         * Price of the stock unit.
         * <p>
         * Type: FLOAT
         */
        public static final String COLUMN_PRICE = "price";

        /**
         * Quantity of the stock unit.
         * <p>
         * Type: INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * Name of the supplier of the stock unit.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /**
         * Contact number of the supplier of the stock unit.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

        /**
         * Email ID of the supplier of the stock unit.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Image URL of the stock unit image.
         * <p>
         * Type: TEXT
         */
        public static final String COLUMN_IMAGE = "image";

        /**
         * The sql statement for creating the stock table into the stock database
         */
        public static final String CREATE_TABLE_STOCK = "CREATE TABLE " +
                StockContract.StockEntry.TABLE_NAME + "(" +
                StockContract.StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StockContract.StockEntry.COLUMN_NAME + " TEXT NOT NULL," +
                StockContract.StockEntry.COLUMN_PRICE + " TEXT NOT NULL," +
                StockContract.StockEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                StockContract.StockEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL," +
                StockContract.StockEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL," +
                StockContract.StockEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL," +
                StockEntry.COLUMN_IMAGE + " TEXT NOT NULL" + ");";
    }
}
