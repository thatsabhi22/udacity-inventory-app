package com.theleafapps.pro.udacityinventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theleafapps.pro.udacityinventoryapp.data.StockContract.StockEntry;

/**
 * Created by aviator on 19/01/18.
 */

/**
 * {@link StockCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of stock data as its data source. This adapter knows
 * how to create list items for each row of stock data in the {@link Cursor}.
 */
public class StockCursorAdapter extends CursorAdapter {

    private final StockListActivity stockListActivity;

    /**
     * Constructs a new {@link StockCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public StockCursorAdapter(StockListActivity context, Cursor c) {
        super(context, c,0);
        this.stockListActivity = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.single_stock_item, parent, false);
    }

    /**
     * This method binds the stock data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current stock can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView stock_name_tv = (TextView) view.findViewById(R.id.stock_unit_name);
        TextView stock_quantity_tv = (TextView) view.findViewById(R.id.stock_unit_quantity);
        TextView stock_unit_price = (TextView) view.findViewById(R.id.stock_unit_price);
        ImageView stock_unit_image_view = (ImageView) view.findViewById(R.id.stock_image);
        Button saleButton = (Button)view.findViewById(R.id.sale_button);

        // Find the columns of pet attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(StockEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_IMAGE);

        // Read the pet attributes from the Cursor for the current pet
        final int stockId = cursor.getInt(idColumnIndex);
        String stockUnitName = cursor.getString(nameColumnIndex);
        float stockPrice = cursor.getFloat(priceColumnIndex);
        final int stockQuantity = cursor.getInt(quantityColumnIndex);
        String stockImageUri = cursor.getString(imageColumnIndex);

        // Update the TextViews with the attributes for the current pet
        stock_name_tv.setText(stockUnitName);
        stock_quantity_tv.setText(String.valueOf(stockQuantity));
        stock_unit_price.setText(String.valueOf(stockPrice));
        stock_unit_image_view.setImageURI(Uri.parse(stockImageUri));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockListActivity.clickOnViewItem(stockId);
            }
        });

        saleButton.setOnClickListener(new View.OnClickListener() {

            int updatedQuantity = stockQuantity - 1;
            @Override
            public void onClick(View v) {
                stockListActivity.clickOnSale(stockId,
                        updatedQuantity);
            }
        });
    }
}
