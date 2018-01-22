package com.theleafapps.pro.udacityinventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.theleafapps.pro.udacityinventoryapp.data.StockContract.StockEntry;

public class StockListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the stock data loader
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
                Intent intent = new Intent(StockListActivity.this, StockDetailActivity.class);
                startActivity(intent);
                //insertStock();
            }
        });

        final ListView stockListView = (ListView) findViewById(R.id.stock_list_view);

        View emptyView = findViewById(R.id.stock_empty_view);

        stockListView.setEmptyView(emptyView);

        //Initialize the loader
        getLoaderManager().initLoader(STOCK_LOADER, null, this);

        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to {@link StockDetailActivity}
                Intent intent = new Intent(StockListActivity.this, StockDetailActivity.class);

                // Form the content URI that represents the specific stock unit that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link StockEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.theleafapps.pro.stock/stock/2"
                // if the stock with ID 2 was clicked on.
                Uri currentStockUri = ContentUris.withAppendedId(StockEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentStockUri);

                // Launch the {@link EditorActivity} to display the data for the current stock unit.
                startActivity(intent);
            }
        });

        // Setup an Adapter to create a list item for each row of stock data in the Cursor.
        // There is no stock data yet (until the loader finishes) so pass in null for the Cursor.
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
        // Update {@link StockCursorAdapter} with this new cursor containing updated stock unit data
        stockAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        stockAdapter.swapCursor(null);
    }

    /**
     * Helper method to insert hardcoded stock data into the database. For debugging purposes only.
     */
    private void insertStock() {
        // Create a ContentValues object where column names are the keys,
        // and Stock unit attributes are the values.
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
