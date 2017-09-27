package my.edu.tarc.kusm_wa14student.communechat.components;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import my.edu.tarc.kusm_wa14student.communechat.model.Contact;

/**
 * Created by Xeosz on 27-Sep-17.
 */

public class ContactDBHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts";

    // Contacts Table Columns names
    private static final String KEY_ID = "uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_STATUS = "status";
    private static final String KEY_LAST_ONLINE = "last_online";
    private static final String KEY_PH_NO = "phone_number";

    public ContactDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // + KEY_IMAGE + " BLOB"
        String CREATE_CONTACTS_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + " ("
                        + KEY_ID + " INTEGER PRIMARY KEY, "
                        + KEY_USERNAME + " TEXT, "
                        + KEY_NICKNAME + " TEXT, "
                        + KEY_GENDER + " INTEGER, "
                        + KEY_STATUS + " TEXT, "
                        + KEY_LAST_ONLINE + " INTEGER, "
                        + KEY_PH_NO + " TEXT"
                        +");";
        sqLiteDatabase.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        // Create tables again
        onCreate(sqLiteDatabase);
    }

    // Adding new contact
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ID, contact.getUid());
        values.put(KEY_USERNAME, contact.getUsername());
        values.put(KEY_NICKNAME, contact.getNickname());
        values.put(KEY_GENDER, contact.getGender());
        values.put(KEY_STATUS, contact.getStatus());
        values.put(KEY_LAST_ONLINE, contact.getLast_online());
        //values.put(KEY_IMAGE, contact.getPhoneNumber());
        values.put(KEY_PH_NO, contact.getPhone_number());

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    public Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS,
                new String[] { KEY_ID, KEY_USERNAME, KEY_NICKNAME, KEY_GENDER, KEY_STATUS, KEY_LAST_ONLINE, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(
                Integer.parseInt(cursor.getString(0)),  //id
                cursor.getString(1),                    //username
                cursor.getString(2),                    //nickname
                Integer.parseInt(cursor.getString(3)),  //gender
                cursor.getString(4),                    //status
                Integer.parseInt(cursor.getString(5)),  //last online
                cursor.getString(6));                   //phone number
        // return contact
        return contact;
    }

    // Getting All Contacts
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setUid(Integer.parseInt(cursor.getString(0)));
                contact.setUsername(cursor.getString(1));
                contact.setNickname(cursor.getString(2));
                contact.setGender(Integer.parseInt(cursor.getString(3)));
                contact.setStatus(cursor.getString(4));
                contact.setLast_online(Integer.parseInt(cursor.getString(5)));
                contact.setPhone_number(cursor.getString(6));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  COUNT(*) FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        // return count
        return cursor.getInt(0);
    }
    // Updating single contact
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, contact.getUid());
        values.put(KEY_USERNAME, contact.getUsername());
        values.put(KEY_NICKNAME, contact.getNickname());
        values.put(KEY_GENDER, contact.getGender());
        values.put(KEY_STATUS, contact.getStatus());
        values.put(KEY_LAST_ONLINE, contact.getLast_online());
        //values.put(KEY_IMAGE, contact.getPhoneNumber());
        values.put(KEY_PH_NO, contact.getPhone_number());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getUid()) });
    }

    // Deleting single contact
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getUid()) });
        db.close();
    }

    public void clearDatabase() {
        String clearDBQuery = "DELETE FROM "+TABLE_CONTACTS;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(clearDBQuery);
    }
}
