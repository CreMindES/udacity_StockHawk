package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.StockProvider;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static java.security.AccessController.getContext;

public class StockActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_STOCK_URI    = "stock_uri";
    public static final String ARG_STOCK_SYMBOL = "stock_symbol";

    private static final int CURSOR_LOADER_ID = 100;

    private Intent intent;
    private String name;
    private String stockExchange;
    private String symbol;
    float price;
    float changeAbsolute;
    float changePercent;
    String history;

    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatShort;
    private DecimalFormat dollarFormatSign;
    private DecimalFormat percentageFormatSign;

    @BindView( R.id.activity_stock_name )
    TextView stockNameTextView;
    @BindView( R.id.activity_stock_exchange)
    TextView stockExchangeTextView;
    @BindView( R.id.activity_stock_price )
    TextView priceTextView;
    @BindView( R.id.activity_stock_price_change )
    TextView priceChangeTextView;
    @BindView( R.id.activity_stock_history_title )
    TextView historyTitleTextView;
    @BindView( R.id.activity_stock_linechart )
    LineChartView lineChartView;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_stock );
        ButterKnife.bind(this);

        intent = getIntent();
        if( !intent.hasExtra( "stock_uri" ) ) {
            // TODO: error
        }

        symbol = intent.getStringExtra( ARG_STOCK_SYMBOL );
        setTitle( symbol );

        Uri stockUri = Contract.Quote.makeUriForStock( symbol );

        setTitle( symbol );

        dollarFormat      = (DecimalFormat) NumberFormat.getCurrencyInstance( Locale.US );
        dollarFormatShort = (DecimalFormat) NumberFormat.getCurrencyInstance( Locale.US );
        dollarFormatSign  = (DecimalFormat) NumberFormat.getCurrencyInstance( Locale.US );
        dollarFormatShort.setMinimumFractionDigits( 0 );
        dollarFormatSign.setPositivePrefix( "+$" );
        percentageFormatSign = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormatSign.setMaximumFractionDigits( 2 );
        percentageFormatSign.setMinimumFractionDigits( 2 );
        percentageFormatSign.setPositivePrefix( "+" );

        getSupportLoaderManager().initLoader( CURSOR_LOADER_ID, null, this );

        lineChartView.setContentDescription( getString(R.string.activity_stock_cont_desc_graph) );
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch(id) {
            case CURSOR_LOADER_ID: {
                return new CursorLoader( this,
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        Contract.Quote.COLUMN_SYMBOL + " = \"" + symbol + "\"",
                        null, null );
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch( loader.getId() ) {
            case CURSOR_LOADER_ID: {
                if( data == null ) {
                    // TODO: error
                }
                data.moveToFirst();

                name           = data.getString( Contract.Quote.POSITION_NAME );
                stockExchange  = data.getString( Contract.Quote.POSITION_STOCKEXCHANGE );
                price          = data.getFloat ( Contract.Quote.POSITION_PRICE );
                changeAbsolute = data.getFloat ( Contract.Quote.POSITION_ABSOLUTE_CHANGE );
                changePercent  = data.getFloat ( Contract.Quote.POSITION_PERCENTAGE_CHANGE ) / 100;
                history        = data.getString( Contract.Quote.POSITION_HISTORY );

                stockNameTextView.setText( name );
                stockExchangeTextView.setText( stockExchange );
                priceTextView.setText( dollarFormat.format( price ) );
                priceChangeTextView.setText(
                    dollarFormatSign.format(changeAbsolute) + " (" +
                    percentageFormatSign.format(changePercent) + ")"
                );


                updateLineChart();

                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateLineChart() {
        
        // parse data
        String[] rawDataArray = history.split( "\n" );
        SimpleDateFormat dataFormat = new SimpleDateFormat("MMM dd");

        LineSet dataset = new LineSet();
        float max = 0;
        float min = Float.MAX_VALUE;
        for( int i = rawDataArray.length-1; i >= 0; --i ) {
            String rawDataPoint = rawDataArray[i];
            String[] rawPoint = rawDataPoint.split(",");
            Timestamp ts;
            float price;
            try {
                ts = new Timestamp( Long.parseLong( rawPoint[0] ) );
                price = Float.parseFloat( rawPoint[1] );

                // limit the number of points
                if( dataset.size() < 7 ) {
                    dataset.addPoint(dataFormat.format(ts), price);
                    // update min and max
                    if (min > price) {
                        min = price;
                    }
                    if (max < price) {
                        max = price;
                    }
                }
            } catch ( NumberFormatException e ) {
                Timber.e( e );
            }
        }

        lineChartView.setAxisBorderValues( (int) Math.floor( min ), (int) Math.ceil( max ) );

        dataset.setColor( ContextCompat.getColor( this, R.color.white ) )
            .setDotsColor( ContextCompat.getColor( this, R.color.white ) );

        lineChartView.setYAxis(true)
                .setLabelsColor( ContextCompat.getColor( this, R.color.white ) )
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsFormat( dollarFormatShort )
                .setStep( (int) Math.ceil( ( max - min ) / 7 ) );

        lineChartView
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setXAxis(true)
                .setLabelsColor( ContextCompat.getColor( this, R.color.white ) );

        lineChartView.setBackgroundColor( ContextCompat.getColor( this, R.color.material_teal_a700 ) );
        lineChartView.setGrid( ChartView.GridType.HORIZONTAL, new Paint( Paint.ANTI_ALIAS_FLAG ) );

        lineChartView.addData( dataset );
        lineChartView.show();
    }
}
