package com.practice.photoalaram;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.net.Uri;


public class DatabaseProvider extends ContentProvider{

    public static final String LOG_TAG = "DBProvider_MonsteR";
    public static final String DATABASE_NAME = "alarm.db";
    public static final String TABLE_NAME = "alarm";
    public static final int DATABASE_VERSION = 1;

    private static final String CREATE_DATABASE = "CREATE TABLE IF NOT EXISTS  " + TABLE_NAME +
            "(" + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Alarm.Columns.ALARM_HOUR +
            " INTEGER NOT NULL, " + Alarm.Columns.ALARM_MINUTES + " INTEGER NOT NULL, " +
            Alarm.Columns.ALARM_TONE + " TEXT NOT NULL, " + Alarm.Columns.ALARM_IMAGE
            + " TEXT NOT NULL, " + Alarm.Columns.MESSAGE + " TEXT, " + Alarm.Columns.REPEAT +
            " INTEGER, " + Alarm.Columns.DAYS_OF_WEEK + " INTEGER, " + Alarm.Columns.IS_ENABLED +
            " INTEGER);";

    private PhotoAlarmDBHelper dbHelper;
    //private final Context context = getContext();

    public DatabaseProvider() {
    }

    public static Cursor getCursor(ContentResolver contentResolver){
        return contentResolver.query(Alarm.Columns.CONTENT_URI, Alarm.Columns.QUERY_COLUMNS, null, null, null);
    }
    public static CursorLoader getCursorLoader(Context context){
        return new CursorLoader(
                context, Alarm.Columns.CONTENT_URI,
                Alarm.Columns.QUERY_COLUMNS,
                null,
                null,
                Alarm.Columns.DEFAULT_SORT_ORDER);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new PhotoAlarmDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        //TODO Try matching URI to patterns of alarm with id and without id instead of using exception
        try {
            String segment = uri.getPathSegments().get(1);
            //int newId = Integer.parseInt(segment);
            qb.appendWhere("_id=");
            qb.appendWhere(segment);
        }
        catch(IndexOutOfBoundsException e){

        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if(ret == null){
            Log.e(LOG_TAG, "Query to database base failed");
        }else{
            ret.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return ret;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        if(initialValues == null){
            values = new ContentValues();
        }else{
            values = initialValues;
        }
        if(!values.containsKey(Alarm.Columns.ALARM_HOUR)){
            throw new SQLException("Time for alarm is not passed");
        }
        if(!values.containsKey(Alarm.Columns.ALARM_MINUTES)){
            values.put(Alarm.Columns.ALARM_MINUTES, 0);
        }
        if(!values.containsKey(Alarm.Columns.ALARM_IMAGE)){
            values.put(Alarm.Columns.ALARM_IMAGE, Alarm.Columns.DEFAULT_IMAGE_URI.toString());
        }
        if(!values.containsKey(Alarm.Columns.ALARM_TONE)){
            values.put(Alarm.Columns.ALARM_TONE, "");
        }
        if(!values.containsKey(Alarm.Columns.MESSAGE)){
            values.put(Alarm.Columns.MESSAGE, "No message set");
        }
        if(!values.containsKey(Alarm.Columns.REPEAT)){
            values.put(Alarm.Columns.REPEAT, 0);
        }
        if(!values.containsKey(Alarm.Columns.DAYS_OF_WEEK)){
            values.put(Alarm.Columns.DAYS_OF_WEEK, 0);
        }
        if(!values.containsKey(Alarm.Columns.IS_ENABLED)){
            values.put(Alarm.Columns.IS_ENABLED, 1);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, Alarm.Columns.MESSAGE, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + uri);
        }else {
            Log.v(LOG_TAG, "Added alarm rowId = " + rowId);
        }
        Uri newUri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(LOG_TAG, "Deleting Alarm Uri " + uri);
        String segement = uri.getPathSegments().get(1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(TextUtils.isEmpty(selection)){
            selection = Alarm.Columns._ID + "=" + segement;
        }
        Log.i(LOG_TAG, "Deleting Alarm selection " + selection);
        db.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues initialValues, String selection, String[] selectionArgs) {
        Log.i(LOG_TAG, "Updating Alarm DB uri " + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values;
        if(initialValues == null){
            values = new ContentValues();
        }else{
            values = initialValues;
        }
        if(!values.containsKey(Alarm.Columns.ALARM_HOUR)){
            throw new SQLException("Time for alarm is not passed");
        }
        if(!values.containsKey(Alarm.Columns.ALARM_MINUTES)){
            values.put(Alarm.Columns.ALARM_MINUTES, 0);
        }
        if(!values.containsKey(Alarm.Columns.ALARM_IMAGE)){
            values.put(Alarm.Columns.ALARM_IMAGE, Alarm.Columns.DEFAULT_IMAGE_URI.toString());
        }
        if(!values.containsKey(Alarm.Columns.ALARM_TONE)){
            values.put(Alarm.Columns.ALARM_TONE, "");
        }
        if(!values.containsKey(Alarm.Columns.MESSAGE)){
            values.put(Alarm.Columns.MESSAGE, "No message set");
        }
        if(!values.containsKey(Alarm.Columns.REPEAT)){
            values.put(Alarm.Columns.REPEAT, 0);
        }
        if(!values.containsKey(Alarm.Columns.DAYS_OF_WEEK)){
            values.put(Alarm.Columns.DAYS_OF_WEEK, 0);
        }
        if(!values.containsKey(Alarm.Columns.IS_ENABLED)){
            values.put(Alarm.Columns.IS_ENABLED, 1);
        }
        String segement = uri.getPathSegments().get(1);
        if(TextUtils.isEmpty(selection)){
            selection = Alarm.Columns._ID + "=" + segement;
        }
        db.update(TABLE_NAME,values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    private class PhotoAlarmDBHelper extends SQLiteOpenHelper{

		public PhotoAlarmDBHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG,"On create on DBHelper");
			// TODO Auto-generated method stub
			db.execSQL(CREATE_DATABASE);
		}

        @Override
        public synchronized void close() {
            super.close();
        }

        @Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			Log.i(LOG_TAG, "Database version changed sqlite will be upgraded and changes will be lost");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
        }

    }
}
	

