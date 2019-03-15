package ca.benoitmignault.mesapprentissagesandroid;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class InformationDATA extends SQLiteOpenHelper {

    private String INFODB = "INFORMATION_LOG_DB_SQLITE";
    private String USERINFO = "INFORMATION_USER";
    private static final String DATABASE_NAME = "quizwinBD";    // Database Name
    private static final String TABLE_NAME = "user";   // Table Name
    private static final int DATABASE_Version = 1;   // Database Version
    private static final String UID="_id";     // Column I (Primary Key)
    private static final String EMAIL = "email";    //Column 2
    private static final String FULLNAME = "fullname";    //Column 3
    private static final String AGE = "age";    //Column 4
    private static final String GENDER = "gender";    //Column 5
    private static final String CITY = "city";    //Column 6
    private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
            " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+EMAIL+" VARCHAR(50) ,"+ FULLNAME+" VARCHAR(50),"
                +AGE+" INTEGER,"+GENDER+" VARCHAR(10) ,"+CITY+" VARCHAR(50) );";
    private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
    private Context context;

    public InformationDATA(Context context){
        super(context,DATABASE_NAME,null, DATABASE_Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Log.d(INFODB, "Execution error -> " +e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        try {
            Log.d(INFODB, "La table -> " +TABLE_NAME+ " est détruite !!!");
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }catch (Exception e) {
            Log.d(INFODB, "Execution error -> " +e.getMessage());
        }
    }

    // procédure pour faire un update sur un user avec son id préalablement abtonu avec une information précise
    public void updateItem(User oneUserToUpdate, int idUser) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(EMAIL, oneUserToUpdate.getEmail());
        newValues.put(FULLNAME, oneUserToUpdate.getFullName());
        newValues.put(AGE, oneUserToUpdate.getAge());
        newValues.put(GENDER, oneUserToUpdate.getGender());
        newValues.put(CITY, oneUserToUpdate.getCity());
        String whereClause = UID + " = ?";
        String whereArgs[] = {Integer.toString(idUser)};
        Log.d(USERINFO, "Info USEr -> " +idUser);
        int rowModify = db.update(TABLE_NAME, newValues, whereClause, whereArgs);
        Log.d(USERINFO, "Info USEr -> " +rowModify);
    }

    public int getId(String email){
        SQLiteDatabase db = getReadableDatabase();
        // mon select
        String[] projection = {InformationDATA.UID};
        // mon where
        String selection = InformationDATA.EMAIL + " = ?";
        String[] selectionArgs = {email};
        // ensemble de ma requête
        Cursor cursor = db.query(
                InformationDATA.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        Log.d(INFODB, "Nombre d'élément pour un getID -> "+cursor.getCount());
        int idUser = 0;
        if (cursor.moveToFirst()){
            idUser = cursor.getInt(cursor.getColumnIndexOrThrow(InformationDATA.UID));
        }
        cursor.close();
        return idUser;
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void onRequestInformation(){
        SQLiteDatabase db = getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                InformationDATA.UID,
                InformationDATA.EMAIL,
                InformationDATA.FULLNAME,
                InformationDATA.AGE,
                InformationDATA.CITY,
                InformationDATA.GENDER
        };
        String selection = InformationDATA.EMAIL + " = ?";
        String[] selectionArgs = {};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = InformationDATA.EMAIL + " DESC";

        Cursor cursor = db.query(
                InformationDATA.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        // Si il y a un seul élément en retour -> cursor.moveToFirst();
        Log.d(INFODB, "Nombre d'élément pour le select ALL -> "+cursor.getCount());

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow(InformationDATA.EMAIL));
            String fullname = cursor.getString(cursor.getColumnIndexOrThrow(InformationDATA.FULLNAME));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(InformationDATA.AGE));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow(InformationDATA.GENDER));
            String city = cursor.getString(cursor.getColumnIndexOrThrow(InformationDATA.CITY));
            long itemId = cursor.getInt(cursor.getColumnIndexOrThrow(InformationDATA.UID));
            Log.d(INFODB, "Email -> "+email+" fullname -> "+fullname+" age -> "+age+" gender -> "+gender+" city -> "+city);
            itemIds.add(itemId);
        }
        cursor.close();
    }

    public boolean addUser(String email, String fullname, int age, String gender, String city){
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(EMAIL, email);
        values.put(FULLNAME, fullname);
        values.put(AGE, age);
        values.put(GENDER, gender);
        values.put(CITY, city);

        // Insert the new row, returning the primary key value of the new row
        long row = db.insert(TABLE_NAME, null, values);

        Log.d(INFODB, "La table -> " + row);
        db.close();
        return true;
    }
/*
    @Override
    protected void onDestroy() {
        this.close();
        super.onDestroy();
    }*/
}
