package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;
import com.squareup.okhttp.internal.Util;

/**
 * Created by max on 6/5/2016.
 */
public class StockWidgetRemoteViewService extends RemoteViewsService {
    static final int INDEX_QUOTES_ID = 0;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null)
                    data.close();

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{
                                QuoteColumns._ID,
                                QuoteColumns.SYMBOL,
                                QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE,
                                QuoteColumns.CHANGE,
                                QuoteColumns.ISUP
                        },
                        QuoteColumns.ISCURRENT + " = ? ",
                        new String[] {"1"},
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position))
                    return null;

                RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget_collection_item);

                view.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex(
                        StockDetailActivity.TAG_SYMBOL)));

                if(data.getInt(data.getColumnIndex(QuoteColumns.ISUP)) == 1){
                    view.setInt(R.id.change, "setBackgroundResource",R.drawable.percent_change_pill_green);
                }else {
                    view.setInt(R.id.change, "setBackgroundResource",R.drawable.percent_change_pill_red);
                }

                if(Utils.showPercent)
                    view.setTextViewText(R.id.change,data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                else
                    view.setTextViewText(R.id.change,data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));

                Intent fillIntenet = new Intent();
                fillIntenet.putExtra(StockDetailActivity.TAG_SYMBOL, data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
                view.setOnClickFillInIntent(R.id.widget_list_item,fillIntenet);

                return view;
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
                if(data != null && data.moveToPosition(position))
                    return data.getLong(INDEX_QUOTES_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
