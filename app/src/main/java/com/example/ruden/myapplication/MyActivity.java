package com.example.ruden.myapplication;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MyActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.ruden.myapplication.MESSAGE";
    public final static String SAVED_MESSAGE_KEY = "com.example.ruden.myapplication.SAVED_MESSAGE";
    public static final int PICK_CONTACT_REQUEST = 1;  // The request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        if (message.isEmpty()) {
            SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
            String savedMessage = prefs.getString(SAVED_MESSAGE_KEY, "");
            editText.setText(savedMessage, TextView.BufferType.EDITABLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVED_MESSAGE_KEY, message);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(" " + ab.getTitle());
        }
        // Inflate the menu items for use in the action bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                openSearch();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    private void openSearch() {
        //Do something here.
        startActivity(new Intent(Settings.ACTION_SEARCH_SETTINGS));
    }

    private void openSettings() {
        //Do something here.
        startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

    /** Called when the user clicks the Add Contact button */
    public void addContact(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        //pickContactIntent.setType(Email.CONTENT_TYPE); // Show user only contacts with email
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData(); // has the uri for picked contact
                String id = contactUri.getLastPathSegment();
                ContentResolver resolver = getContentResolver();
                String name = "", phone = "", email = "";

                Cursor contactCursor = resolver.query(contactUri, null, null, null, null);
                if (contactCursor.moveToFirst()) {
                    name = contactCursor.getString(contactCursor.getColumnIndex(Contacts.DISPLAY_NAME));
                }

                Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] { id }, null);
                if (phoneCursor.moveToFirst()) {
                    phone = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
                }

                Cursor emailCursor = resolver.query(Email.CONTENT_URI, null, Email.CONTACT_ID + "=?", new String[]{id}, null);
                if (emailCursor.moveToFirst()) {
                    email = emailCursor.getString(emailCursor.getColumnIndex(Email.ADDRESS));
                }

                String message = "name: " + name + "\r\nemail: " + email + "\r\nphone: " + phone;
                new AlertDialog.Builder(this)
                        .setTitle(name)
                        .setMessage(message)
                        .setPositiveButton("ok", null)
                        .show();

                final Toast notifier = Toast.makeText(this, name + ", email: " + email + ", phone: " + phone, Toast.LENGTH_LONG);
                notifier.show();
                new CountDownTimer(9000, 1000)
                {
                    public void onTick(long millisUntilFinished) {notifier.show();}
                    public void onFinish() {notifier.show();
                    }
                }.start();
            }
        }
    }

}
