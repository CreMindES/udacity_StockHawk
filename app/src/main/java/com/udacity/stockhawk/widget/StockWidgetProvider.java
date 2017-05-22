package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent( context, StockWidgetRemoteViewsService.class );
        intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId );
        intent.setData ( Uri.parse( intent.toUri(Intent.URI_INTENT_SCHEME ) ) );

        // Construct the RemoteViews object
        RemoteViews widgetViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget);

        // Click on the widget title bar
        Intent intentMainAcitivity = new Intent( context, MainActivity.class );
        PendingIntent pendingMainIntent = PendingIntent.getActivity(
                context, 0, intentMainAcitivity, 0 );
        widgetViews.setOnClickPendingIntent( R.id.widget_title_bar, pendingMainIntent );

        // Click on stock item
        Intent intentStockDetail = new Intent( context, StockActivity.class );
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intentStockDetail, PendingIntent.FLAG_UPDATE_CURRENT);
        widgetViews.setPendingIntentTemplate( R.id.widget_listview, pendingIntent );

        // set up adatper
        widgetViews.setRemoteAdapter( R.id.widget_listview, intent );

        // Instruct the com.udacity.stockhawk.widget manager
        // to update the com.udacity.stockhawk.widget
        appWidgetManager.updateAppWidget( appWidgetId, widgetViews );
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate( context, appWidgetManager, appWidgetIds );
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first com.udacity.stockhawk.widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last com.udacity.stockhawk.widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if( intent.getAction().equals( QuoteSyncJob.APPWIDGET_UPDATE) ) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, StockWidgetProvider.class));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
        }
    }
}

