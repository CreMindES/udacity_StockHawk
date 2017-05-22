package com.udacity.stockhawk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by cremindes on 01/05/17.
 */

public class Utility {

    static private DecimalFormat formatDollar =
            (DecimalFormat) NumberFormat.getCurrencyInstance( Locale.US );
    static private DecimalFormat formatDollarSign =
            (DecimalFormat) NumberFormat.getCurrencyInstance( Locale.US );
    static private DecimalFormat formatPercentageSign =
            (DecimalFormat) NumberFormat.getPercentInstance( Locale.getDefault() );

    static public void init() {
        formatDollarSign.setPositivePrefix( "+$" );
        formatPercentageSign.setMaximumFractionDigits( 2 );
        formatPercentageSign.setMinimumFractionDigits( 2 );
        formatPercentageSign.setPositivePrefix( "+" );
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
    public static boolean isNetworkAvailable( Context c ) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    static public String formatAsDollar( float num ) {
        return formatDollar.format( num );
    }
    static public String formatAsDollarSign( float num ) {
        return formatDollarSign.format( num );
    }
    static public String formatAsPercentageSign( float num ) {
        return formatPercentageSign.format( num );
    }
}
