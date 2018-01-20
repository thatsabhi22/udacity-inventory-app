package com.theleafapps.pro.udacityinventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.theleafapps.pro.udacityinventoryapp.data.StockContract.StockEntry;

/**
 * Created by aviator on 19/01/18.
 */

public class StockProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = StockProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the stock table
     */
    private static final int STOCK = 100;

    /**
     * URI matcher code for the content URI for a single stock entry in the stock table
     */
    private static final int STOCK_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.theleafapps.pro.stock/stock" will map to the
        // integer code {@link #STOCK}. This URI is used to provide access to MULTIPLE rows
        // of the stock table.
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK, STOCK);

        // The content URI of the form "content://com.theleafapps.pro.stock/stock/#" will map to the
        // integer code {@link #STOCK_ID}. This URI is used to provide access to ONE single row
        // of the stock table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.theleafapps.pro.stock/stock/3" matches, but
        // "content://com.theleafapps.pro.stock/stock" (without a number at the end) doesn't match.
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK + "/#", STOCK_ID);
    }

    /**
     * Database helper object
     */
    private StockDbHelper stockDbHelper;

    @Override
    public boolean onCreate() {
        stockDbHelper = new StockDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = stockDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                // For the STOCK code, query the stock table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the stock table.
                cursor = database.query(StockEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case STOCK_ID:
                // For the STOCK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.theleafapps.pro.stock/stock/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the stock table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(StockEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return StockEntry.CONTENT_LIST_TYPE;
            case STOCK_ID:
                return StockEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return insertStockEntry(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a stock into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertStockEntry(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(StockEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Stock Unit requires a name");
        }

        float price = values.getAsFloat(StockEntry.COLUMN_PRICE);
        if (price == 0) {
            throw new IllegalArgumentException("Stock Unit requires a price");
        }

        int quantity = values.getAsInteger(StockEntry.COLUMN_QUANTITY);
        if (quantity == 0) {
            throw new IllegalArgumentException("Stock Unit requires a quantity");
        }

        String supplierName = values.getAsString(StockEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier Name is required");
        }

        String supplierEmail = values.getAsString(StockEntry.COLUMN_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Supplier Email is required");
        }

        String supplierPhone = values.getAsString(StockEntry.COLUMN_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Supplier Phone is required");
        }

        // Get writeable database
        SQLiteDatabase database = stockDbHelper.getWritableDatabase();

        // Insert the new stock entry with the given values
        long id = database.insert(StockEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify All listeners that the data has changed for the stock content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
