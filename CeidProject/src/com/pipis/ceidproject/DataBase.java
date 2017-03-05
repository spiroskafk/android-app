package com.pipis.ceidproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase extends SQLiteOpenHelper {

	private static final String DATA = "Spuro";
	private static final int BASE = 1;

	public DataBase(Context con) {
		super(con,DATA,null,BASE);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE STOIXEIA(id INTEGER PRIMARY KEY AUTOINCREMENT,TITLOS TXT);");
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL("DROP TABLE IF EXIST STOIXEIA");
		onCreate(db);
	}
}
