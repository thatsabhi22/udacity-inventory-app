package com.theleafapps.pro.udacityinventoryapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.theleafapps.pro.udacityinventoryapp.data.StockContract.StockEntry;

public class StockDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the stock data loader
     */
    private static final int STOCK_LOADER = 1;

    /**
     * Content URI for the existing stock (null if it's a new stock unit)
     */
    private Uri mCurrentStockUri;

    /**
     * EditText field to enter the stock unit name
     */
    TextView stockUnitNameEditText;

    /**
     * EditText field to enter the stock quantity
     */
    TextView stockUnitQuantityEditText;

    /**
     * EditText field to enter the stock unit price
     */
    TextView stockUnitPriceEditText;

    /**
     * EditText field to enter the supplier name
     */
    TextView supplierNameEditText;

    /**
     * EditText field to enter the supplier phone
     */
    TextView supplierPhoneEditText;

    /**
     * EditText field to enter the stock email
     */
    TextView supplierEmailEditText;

    /**
     * ImageView for the stock image
     */
    ImageView productImageView;

    /**
     * Boolean flag that keeps track of whether the stock has been edited (true) or not (false)
     */
    private boolean mStockHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new stock unit or editing an existing one.
        Intent intent = getIntent();
        mCurrentStockUri = intent.getData();

        // If the intent DOES NOT contain a stock unit content URI, then we know that we are
        // creating a new stock unit.
        if (mCurrentStockUri == null) {
            // This is a new stock unit, so change the app bar to say "Add a stock unit"
            setTitle(getString(R.string.detail_activity_title_new_stock_unit));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a stock unit that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing stock unit, so change app bar to say "Edit stock unit"
            setTitle(getString(R.string.detail_activity_title_edit_stock_unit));

            // Initialize a loader to read the stock unit data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(STOCK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        stockUnitNameEditText = (EditText) findViewById(R.id.stock_unit_name);
        stockUnitQuantityEditText = (EditText) findViewById(R.id.stock_unit_quantity);
        stockUnitPriceEditText = (EditText) findViewById(R.id.stock_unit_price);
        supplierNameEditText = (EditText) findViewById(R.id.supplier_name);
        supplierPhoneEditText = (EditText) findViewById(R.id.supplier_phone_number);
        supplierEmailEditText = (EditText) findViewById(R.id.supplier_email);
        productImageView = (ImageView) findViewById(R.id.product_image_view);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        stockUnitNameEditText.setOnTouchListener(mTouchListener);
        stockUnitQuantityEditText.setOnTouchListener(mTouchListener);
        stockUnitPriceEditText.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        supplierPhoneEditText.setOnTouchListener(mTouchListener);
        supplierEmailEditText.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_stock_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:

                // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mStockHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mStockHasChanged = true;
            return false;
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_NAME,
                StockEntry.COLUMN_PRICE,
                StockEntry.COLUMN_QUANTITY,
                StockEntry.COLUMN_SUPPLIER_NAME,
                StockEntry.COLUMN_SUPPLIER_PHONE,
                StockEntry.COLUMN_SUPPLIER_EMAIL,
                StockEntry.COLUMN_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentStockUri,         // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of stock unit attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_SUPPLIER_PHONE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_SUPPLIER_EMAIL);
            int productImageColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String stock_name = cursor.getString(nameColumnIndex);
            float stock_price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier_name = cursor.getString(supplierNameColumnIndex);
            String supplier_phone = cursor.getString(supplierPhoneColumnIndex);
            String supplier_email = cursor.getString(supplierEmailColumnIndex);
            String stock_image = cursor.getString(productImageColumnIndex);

            // Update the views on the screen with the values from the database
            stockUnitNameEditText.setText(stock_name);
            stockUnitQuantityEditText.setText(quantity);
            stockUnitPriceEditText.setText(String.valueOf(stock_price));
            supplierNameEditText.setText(supplier_name);
            supplierPhoneEditText.setText(supplier_phone);
            supplierEmailEditText.setText(supplier_email);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        stockUnitNameEditText.setText("");
        stockUnitQuantityEditText.setText("");
        stockUnitPriceEditText.setText("");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");
        supplierEmailEditText.setText("");
    }
}
