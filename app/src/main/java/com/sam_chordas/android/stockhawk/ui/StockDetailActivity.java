package com.sam_chordas.android.stockhawk.ui;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;

import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by max on 5/22/2016.
 */
public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;
    private LineChartView lineChartView;
    public static final String TAG_SYMBOL = "symbol";
    private Animation animate;
    private LineSet lineSet;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString(TAG_SYMBOL,intent.getStringExtra(TAG_SYMBOL));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(TAG_SYMBOL)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        showChart(mCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void showChart(Cursor data){
        lineSet = new LineSet();
        animate = new Animation();
        float minPrice = Float.MAX_VALUE;
        float maxPrice = Float.MIN_VALUE;

        for (data.moveToFirst();!data.isAfterLast();data.moveToNext()){
            String tag = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
            float price = Float.parseFloat(tag);
            minPrice = Math.min(minPrice,price);
            maxPrice = Math.max(maxPrice,price);
            lineSet.addPoint(tag,price);
        }

        lineSet.setColor(this.getResources().getColor(R.color.primary_light))
                .setDotsColor(this.getResources().getColor(R.color.light_green))
                .setThickness(2)
                .setDashed(new float[]{10f , 10f});

        lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(this.getResources().getColor(R.color.light_blue))
                .setXAxis(false)
                .setYAxis(false)
                .setAxisBorderValues(Math.round(Math.max(0f, minPrice - 5f)), Math.round(maxPrice + 5f))
                .addData(lineSet);

        lineChartView.show(animate);


    }
}
