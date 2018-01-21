package com.theleafapps.pro.udacityinventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.theleafapps.pro.udacityinventoryapp.data.StockContract.StockEntry;

public class StockListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int STOCK_LOADER = 0;

    /**
     * Cursor Adapter for the ListView
     */
    StockCursorAdapter stockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(StockListActivity.this, StockDetailActivity.class);
//                startActivity(intent);
                insertPet();
            }
        });

        final ListView stockListView = (ListView) findViewById(R.id.stock_list_view);

        View emptyView = findViewById(R.id.stock_empty_view);

        stockListView.setEmptyView(emptyView);

        //Initialize the loader
        getLoaderManager().initLoader(STOCK_LOADER, null, this);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        stockAdapter = new StockCursorAdapter(this, null);
        stockListView.setAdapter(stockAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_NAME,
                StockEntry.COLUMN_PRICE,
                StockEntry.COLUMN_QUANTITY,
                StockEntry.COLUMN_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                StockEntry.CONTENT_URI,         // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        stockAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        stockAdapter.swapCursor(null);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_NAME, "Pen");
        values.put(StockEntry.COLUMN_PRICE, 150.00);
        values.put(StockEntry.COLUMN_QUANTITY, 10);
        values.put(StockEntry.COLUMN_SUPPLIER_NAME, "Camlin");
        values.put(StockEntry.COLUMN_SUPPLIER_PHONE, "+9999999999");
        values.put(StockEntry.COLUMN_SUPPLIER_EMAIL, "john@supplier.com");
        values.put(StockEntry.COLUMN_IMAGE, "android.resource://com.theleafapps.pro.udacityinventoryapp/drawable/pen");

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link StockEntry#CONTENT_URI} to indicate that we want to insert
        // into the stock database table.
        Uri newUri = getContentResolver().insert(StockEntry.CONTENT_URI, values);
    }

    public void clickOnViewItem(int id) {
        Intent intent = new Intent(this, StockDetailActivity.class);
        intent.putExtra("itemId", id);
        startActivity(intent);
    }

    public void clickOnSale(long id, int quantity) {

        ContentValues values = new ContentValues();
        values.put(StockEntry._ID, id);
        values.put(StockEntry.COLUMN_QUANTITY, quantity);
        String selection = StockEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        if (quantity >= 0) {
            int rowsAffected = getContentResolver().update(StockEntry.CONTENT_URI, values, selection, selectionArgs);
        }

    }
}
