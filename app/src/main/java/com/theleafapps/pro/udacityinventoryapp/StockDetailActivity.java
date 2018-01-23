package com.theleafapps.pro.udacityinventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theleafapps.pro.udacityinventoryapp.data.StockContract.StockEntry;

import java.io.File;

public class StockDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Constant for image request
     */
    public static final int PICK_PHOTO_REQUEST = 20;
    /**
     * Permission Code for External Storage permission
     */
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 21;
    /**
     * Identifier for the stock data loader
     */
    private static final int STOCK_LOADER = 1;
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
     * Button for the increasing stock quantity
     */
    Button addQuantityButton;
    /**
     * Button for the Adding Image of the stock unit
     */
    Button addImageButton;
    /**
     * Button for the decreasing stock quantity
     */
    Button subtractQuantityButton;
    /**
     * Content URI for the existing stock (null if it's a new stock unit)
     */
    private Uri mCurrentStockUri;
    /**
     * Boolean flag that keeps track of whether the stock has been edited (true) or not (false)
     */
    private boolean mStockHasChanged = false;
    /**
     * URI of the stock unit image
     */
    private String mCurrentPhotoUri = "no images";
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

    public final static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

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
        addQuantityButton = (Button) findViewById(R.id.add_quantity_button);
        subtractQuantityButton = (Button) findViewById(R.id.subtract_quantity_button);
        addImageButton = (Button) findViewById(R.id.add_image_button);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        stockUnitNameEditText.setOnTouchListener(mTouchListener);
        stockUnitQuantityEditText.setOnTouchListener(mTouchListener);
        stockUnitPriceEditText.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        supplierPhoneEditText.setOnTouchListener(mTouchListener);
        supplierEmailEditText.setOnTouchListener(mTouchListener);

        addQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(stockUnitQuantityEditText.getText().toString());
                int increasedValue = currentValue + 1;
                stockUnitQuantityEditText.setText(String.valueOf(increasedValue));
            }
        });

        subtractQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(stockUnitQuantityEditText.getText().toString());
                if (currentValue > 0) {
                    int decreasedValue = currentValue - 1;
                    stockUnitQuantityEditText.setText(String.valueOf(decreasedValue));
                }
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPhotoProductUpdate(v);
            }
        });
    }

    /**
     * Get user input from editor and save stock unit into database.
     */
    private void saveStockUnit() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String name = stockUnitNameEditText.getText().toString().trim();
        int quantity = Integer.parseInt(stockUnitQuantityEditText.getText().toString().trim());
        float price = Float.parseFloat(stockUnitPriceEditText.getText().toString().trim());
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierPhone = supplierPhoneEditText.getText().toString().trim();
        String supplierEmail = supplierEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            stockUnitNameEditText.setError("The Product Name cannot be blank");
        } else if (price < 0.00) {
            stockUnitPriceEditText.setError("The Stock Unit Price Cannot be less than Zero");
        } else if (TextUtils.isEmpty(supplierName)) {
            supplierNameEditText.setError("The Supplier Name cannot be blank");
        } else if (TextUtils.isEmpty(supplierPhone)) {
            supplierPhoneEditText.setError("The Supplier Phone cannot be blank");
        } else if (TextUtils.isEmpty(supplierEmail)) {
            supplierEmailEditText.setError("Please Enter a Supplier's Email Id");
        } else if (!isValidEmail(supplierEmail)) {
            supplierEmailEditText.setError("Please Enter a Valid Email Id");
        } else {

            // Create a ContentValues object where column names are the keys,
            // and stock unit attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(StockEntry.COLUMN_NAME, name);
            values.put(StockEntry.COLUMN_QUANTITY, quantity);
            values.put(StockEntry.COLUMN_PRICE, price);
            values.put(StockEntry.COLUMN_SUPPLIER_NAME, supplierName);
            values.put(StockEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            values.put(StockEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);
            values.put(StockEntry.COLUMN_IMAGE, mCurrentPhotoUri);

            // Determine if this is a new or existing stock unit by checking if mCurrentStockUri is null or not
            if (mCurrentStockUri == null) {
                // This is a NEW stock unit, so insert a new stock unit into the provider,
                // returning the content URI for the new stock unit.
                Uri newUri = getContentResolver().insert(StockEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_stock_unit_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_stock_unit_successful),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Otherwise this is an EXISTING stock unit, so update the stock unit with content URI: mCurrentStockUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentStockUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentStockUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_stock_unit_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_stock_unit_successful),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
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
                saveStockUnit();
                break;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                break;
            // Respond to a click on the "Order More" menu option
            case R.id.action_order_more:
                // Pop up confirmation dialog for deletion
                orderMore();
                break;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the stock unit hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mStockHasChanged) {
                    NavUtils.navigateUpFromSameTask(StockDetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(StockDetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onPhotoProductUpdate(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //We are on M or above so we need to ask for runtime permissions
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                invokeGetPhoto();
            } else {
                // we are here if we do not all ready have permissions
                String[] permisionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permisionRequest, EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE);
            }
        } else {
            //We are on an older devices so we dont have to ask for runtime permissions
            invokeGetPhoto();
        }
    }

    private void invokeGetPhoto() {
        // invoke the image gallery using an implict intent.
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set the data and type.  Get all image types.
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //We got a GO from the user
            invokeGetPhoto();
        } else {
            Toast.makeText(this, R.string.err_external_storage_permissions, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                //If we are here, everything processed successfully and we have an Uri data
                Uri mProductPhotoUri = data.getData();
                mCurrentPhotoUri = mProductPhotoUri.toString();
                //Log.d(TAG, "Selected images " + mProductPhotoUri);

                productImageView.setImageURI(Uri.parse(mCurrentPhotoUri));
            }
        }
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new stock unit, hide the "Delete" as well as "Order More" menu item.
        if (mCurrentStockUri == null) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
            MenuItem orderMoreMenuItem = menu.findItem(R.id.action_order_more);
            orderMoreMenuItem.setVisible(false);
        }
        return true;
    }

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
            mCurrentPhotoUri = stock_image;

            // Update the views on the screen with the values from the database
            stockUnitNameEditText.setText(stock_name);
            stockUnitQuantityEditText.setText(String.valueOf(quantity));
            stockUnitPriceEditText.setText(String.valueOf(stock_price));
            supplierNameEditText.setText(supplier_name);
            supplierPhoneEditText.setText(supplier_phone);
            supplierEmailEditText.setText(supplier_email);

            if (TextUtils.equals(stock_image, getString(R.string.no_image))) {
                productImageView.setImageURI(Uri.parse(getString(R.string.no_image_url)));
            } else {
                productImageView.setImageURI(Uri.parse(stock_image));
            }
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

    /**
     * Prompt the user to confirm that they want to delete this stock unit.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the stock unit.
                deleteStockUnit();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the stock unit.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the stock unit.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the stock unit hasn't changed, continue with handling back button press
        if (!mStockHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Perform the deletion of the stock unit in the database.
     */
    private void deleteStockUnit() {
        // Only perform the delete if this is an existing stock unit.
        if (mCurrentStockUri != null) {
            // Call the ContentResolver to delete the stock unit at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentStockUri
            // content URI already identifies the stock unit that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentStockUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_stock_unit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_stock_unit_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    /**
     * Order More inventory menu item opens up the mail interface
     * to send email to the supplier email id
     */
    private void orderMore() {
        String stockUnitName = stockUnitNameEditText.getText().toString();
        String supplierEmail = supplierEmailEditText.getText().toString();
        String mailText = getString(R.string.mailText);
        composeEmail(new String[]{supplierEmail}, "Order for more" + stockUnitName, mailText);
    }

    /**
     * Prepares the Email intent to send email to supplier email id
     *
     * @param addresses recipient email address
     * @param subject   subject of the email
     * @param mailText  body of the email
     */
    public void composeEmail(String[] addresses, String subject, String mailText) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, mailText);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
