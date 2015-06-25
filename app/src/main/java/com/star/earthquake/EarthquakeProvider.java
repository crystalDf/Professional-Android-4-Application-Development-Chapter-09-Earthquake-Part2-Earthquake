package com.star.earthquake;


import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public class EarthquakeProvider extends ContentProvider {

    public static final Uri CONTENT_URI =
            Uri.parse("content://com.star.earthquakeprovider/earthquakes");

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_LOCATION_LAT = "latitude";
    public static final String KEY_LOCATION_LON = "longitude";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_LINK = "link";

    private EarthquakeDatabaseHelper earthquakeDatabaseHelper;

    private static final int QUAKES = 1;
    private static final int QUAKE_ID = 2;
    private static final int SEARCH = 3;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.star.earthquakeprovider", "earthquakes", QUAKES);
        uriMatcher.addURI("com.star.earthquakeprovider", "earthquakes/#", QUAKE_ID);
        uriMatcher.addURI("com.star.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        uriMatcher.addURI("com.star.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        uriMatcher.addURI("com.star.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
        uriMatcher.addURI("com.star.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
    }

    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;

    static {
        SEARCH_PROJECTION_MAP = new HashMap<>();
        SEARCH_PROJECTION_MAP.put("_id", KEY_ID + " AS " + "_id");
        SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1,
                KEY_SUMMARY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
    }

    @Override
    public boolean onCreate() {
        earthquakeDatabaseHelper = new EarthquakeDatabaseHelper(getContext(),
                EarthquakeDatabaseHelper.DATABASE_NAME, null,
                EarthquakeDatabaseHelper.DATABASE_VERSION);

        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                return "vnd.android.cursor.dir/vnd.star.earthquake";
            case QUAKE_ID:
                return "vnd.android.cursor.item/vnd.star.earthquake";
            case SEARCH:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase sqLiteDatabase = earthquakeDatabaseHelper.getWritableDatabase();

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        sqLiteQueryBuilder.setTables(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE);

        System.out.println("hahaha" + uri);

        switch (uriMatcher.match(uri)) {
            case QUAKE_ID:
                sqLiteQueryBuilder.appendWhere(KEY_ID + " = " + uri.getPathSegments().get(1));
                break;
            case SEARCH:
                sqLiteQueryBuilder.appendWhere(KEY_SUMMARY + " LIKE \"%" +
                        uri.getPathSegments().get(1) + "%\"");
                sqLiteQueryBuilder.setProjectionMap(SEARCH_PROJECTION_MAP);
                break;
            default:
                break;
        }

        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = KEY_DATE;
        } else {
            orderBy = sortOrder;
        }

        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection,
                selectionArgs, null, null, orderBy);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase sqLiteDatabase = earthquakeDatabaseHelper.getWritableDatabase();

        long rowId = sqLiteDatabase.insert(earthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                "quake", values);

        if (rowId > 0) {
            Uri insertedUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(insertedUri, null);
            return insertedUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase sqLiteDatabase = earthquakeDatabaseHelper.getWritableDatabase();

        int count;

        switch (uriMatcher.match(uri)) {
            case QUAKES:
                count = sqLiteDatabase.delete(earthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                        selection, selectionArgs);
                break;
            case QUAKE_ID:
                count = sqLiteDatabase.delete(earthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                        KEY_ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ")" : ""), selectionArgs);
                break;
            default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase sqLiteDatabase = earthquakeDatabaseHelper.getWritableDatabase();

        int count;

        switch (uriMatcher.match(uri)) {
            case QUAKES:
                count = sqLiteDatabase.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                        values, selection, selectionArgs);
                break;
            case QUAKE_ID:
                count = sqLiteDatabase.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE,
                        values, KEY_ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ?
                                        " AND (" + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = "EarthquakeProvider";

        private static final String DATABASE_NAME = "earthquakes.db";
        private static final int DATABASE_VERSION = 1;
        private static final String EARTHQUAKE_TABLE = "earthquakes";

        private static final String CREATE_TABLE = "CREATE TABLE " + EARTHQUAKE_TABLE + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DATE + " INTEGER, " +
                KEY_DETAILS + " TEXT, " +
                KEY_SUMMARY + " TEXT, " +
                KEY_LOCATION_LAT + " FLOAT, " +
                KEY_LOCATION_LON + " FLOAT, " +
                KEY_MAGNITUDE + " FLOAT, " +
                KEY_LINK + " TEXT" +
                ");";

        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + EARTHQUAKE_TABLE;

        public EarthquakeDatabaseHelper(Context context, String name,
                                        SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " +
                    oldVersion + " to " + newVersion + ", which will destroy all old data");

            db.execSQL(DROP_TABLE);
            onCreate(db);
        }
    }

}
