package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utility;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.StockActivity;

import timber.log.Timber;

/**
 * Created by cremindes on 21/05/17.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {

        return new RemoteViewsFactory() {
            private Cursor cursor = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if( cursor != null ) { cursor.close(); }

                final long token = Binder.clearCallingIdentity();

                try {
                    cursor = getContentResolver().query(
                            Contract.Quote.URI,
                            Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                            null,
                            null,
                            null
                    );
                } finally {
                    Binder.restoreCallingIdentity(token);
                }

            }

            @Override
            public void onDestroy() {
                if( cursor != null && !cursor.isClosed() ) {
                    cursor.close();
                }
            }

            @Override
            public int getCount() {
                return cursor != null ? cursor.getCount() : 0;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews remoteViewRow = new RemoteViews( getPackageName(), R.layout.stock_widget_item );
                if( !cursor.moveToPosition(position) ) {
                    Timber.e( "cursor.moveToPosition failed" );
                    return remoteViewRow;
                }

                String symbol       = cursor.getString( Contract.Quote.POSITION_SYMBOL );
                float price         = cursor.getFloat ( Contract.Quote.POSITION_PRICE  );
                float changeAbs     = cursor.getFloat ( Contract.Quote.POSITION_ABSOLUTE_CHANGE );
                float changePercent = cursor.getFloat ( Contract.Quote.POSITION_PERCENTAGE_CHANGE ) / 100;

                String priceString         = Utility.formatAsDollar( price );
                String changeAbsString     = Utility.formatAsDollarSign( changeAbs );
                String changePercentString = Utility.formatAsPercentageSign( changePercent );

                remoteViewRow.setTextViewText( R.id.stock_widget_item_symbol, symbol );
                remoteViewRow.setTextViewText( R.id.stock_widget_item_price , priceString );

                if( PrefUtils.getDisplayMode( getApplicationContext() ).equals(
                        getString( R.string.pref_display_mode_absolute_key) ) ) {
                    remoteViewRow.setTextViewText( R.id.stock_widget_item_change , changeAbsString );
                } else {
                    remoteViewRow.setTextViewText( R.id.stock_widget_item_change , changePercentString );
                }

                if( changeAbs <= 0 ) {
                    remoteViewRow.setInt( R.id.stock_widget_change_icon, "setBackgroundColor",
                        ContextCompat.getColor( getApplicationContext(), R.color.material_red_700 ) );
                } else {
                    remoteViewRow.setInt( R.id.stock_widget_change_icon, "setBackgroundColor",
                        ContextCompat.getColor( getApplicationContext(), R.color.material_green_700 ) );
                }

                Intent intent = new Intent();
                intent.putExtra( StockActivity.ARG_STOCK_SYMBOL, symbol );
                remoteViewRow.setOnClickFillInIntent( R.id.stock_widget_item, intent );

                return remoteViewRow;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

        };

    }
}
