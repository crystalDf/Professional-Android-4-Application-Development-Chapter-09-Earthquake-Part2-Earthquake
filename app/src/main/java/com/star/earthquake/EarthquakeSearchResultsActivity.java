package com.star.earthquake;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;

public class EarthquakeSearchResultsActivity extends ListActivity {

    private static final String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";

    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String query = "0";
            
            if (bundle != null) {
                query = bundle.getString(QUERY_EXTRA_KEY);
            }

            String[] projection = {
                    EarthquakeProvider.KEY_ID,
                    EarthquakeProvider.KEY_SUMMARY
            };

            String selection = EarthquakeProvider.KEY_SUMMARY
                    + " LIKE \"%" + query + "%\"";

            String[] selectionArgs = null;

            String sortOrder = EarthquakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";

            return new CursorLoader(EarthquakeSearchResultsActivity.this,
                    EarthquakeProvider.CONTENT_URI, projection, selection,
                    selectionArgs, sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            simpleCursorAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            simpleCursorAdapter.swapCursor(null);
        }
    };

    private static final int LOADER = 0;

    private SimpleCursorAdapter simpleCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        simpleCursorAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null,
                new String[] { EarthquakeProvider.KEY_SUMMARY },
                new int[] { android.R.id.text1 }, 0);

        setListAdapter(simpleCursorAdapter);

        getLoaderManager().initLoader(LOADER, null, loaderCallbacks);

        parseIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }

    private void parseIntent(Intent intent) {

        if (intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);

            Bundle args = new Bundle();
            args.putString(QUERY_EXTRA_KEY, searchQuery);

            getLoaderManager().restartLoader(LOADER, args, loaderCallbacks);
        }
    }

}
