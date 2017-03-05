package com.pipis.ceidproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MyDBHandler extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "markersDB6.db";
	public static final String TABLE_MARKERS = "markers";
	
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_CATEGORY = "category";
	public static final String COLUMN_X = "x";
	public static final String COLUMN_Y = "y";
	
	/**
	 * Constructor
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public MyDBHandler(Context context, String name, CursorFactory factory, int version)
	{
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String CREATE_MARKERS_TABLE = 
				"CREATE TABLE " + TABLE_MARKERS + "("
	             + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," 
				 + COLUMN_TITLE  + " TEXT," 
	             + COLUMN_DESCRIPTION + " TEXT," 
				 + COLUMN_CATEGORY  + " TEXT,"
				 + COLUMN_X + " REAL,"
				 + COLUMN_Y + " REAL"
	             + ");";
		
		
	      db.execSQL(CREATE_MARKERS_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS);
	    onCreate(db);
	}
	

	/**
	 * Save Marker to database.
	 * @param marker : Marker we want to save.
	 * @param category : Category it belongs.
	 */
	public void saveMarkerToDB(Marker marker, String category)
	{
		ContentValues values = new ContentValues();
		
		LatLng point = marker.getPosition();
        
        values.put(COLUMN_TITLE, marker.getTitle());
        values.put(COLUMN_DESCRIPTION, marker.getSnippet());
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_X, point.latitude);
        values.put(COLUMN_Y, point.longitude);
        
        // open database
        SQLiteDatabase db = this.getWritableDatabase();
     
        // insert values
        db.insert(TABLE_MARKERS, null, values);
        
        // close db
        db.close();
           
	}
	
	/**
	 * Run an sql query and return a cursor.
	 * @return cursor
	 */
	public Cursor getCursor()
	{
		String query = "Select * FROM " + TABLE_MARKERS;
		
		// open db
		SQLiteDatabase db = this.getWritableDatabase();
		
		// select data
		Cursor cursor = db.rawQuery(query, null);
		
		return cursor;
	}
	
	/**
	 * Deletes marker from database.
	 * @param title : marker's title.
	 */
	public void deleteFromDB(String title)
	{	
		// markers id we want to delete
		int markersID;
		
		// open database
		SQLiteDatabase db = this.getWritableDatabase();
		
		// sql query to find marker selected by user
		Cursor cursor = db.query(TABLE_MARKERS, new String[] { COLUMN_ID }, COLUMN_TITLE + "=?",
	            new String[] { title }, null, null, null, null);
		
		
		// move cursor to first row
		cursor.moveToFirst();
		
		// get markers id
		markersID = (int) cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
		
		//delete row
		db.delete(TABLE_MARKERS, COLUMN_ID + "=" + markersID, null);

		// close db
		db.close();
	}
	
	
	public void update(String title, String description, String category, String oldTitle)
	{
		// markers id we want to delete
		int markersID;
		
		LatLng point;
		double x;
		double y;
		
		// open database
		SQLiteDatabase db = this.getWritableDatabase();
		
		
		// sql query to find marker selected by user
		Cursor cursor = db.query(TABLE_MARKERS, new String[] { COLUMN_ID, COLUMN_X, COLUMN_Y }, COLUMN_TITLE + "=?",
	            new String[] { oldTitle }, null, null, null, null);		
		
		// move cursor to first row
		cursor.moveToFirst();
		
		// get markers id
		markersID = (int) cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
		x = cursor.getDouble(cursor.getColumnIndex(COLUMN_X));
		y = cursor.getDouble(cursor.getColumnIndex(COLUMN_Y));
		point = new LatLng(x,y);
		
		ContentValues values = new ContentValues();
		
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
			
		db.update(TABLE_MARKERS, values, COLUMN_ID + "=" + markersID, null);		
	}	
}
