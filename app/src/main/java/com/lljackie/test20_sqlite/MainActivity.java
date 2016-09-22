package com.lljackie.test20_sqlite;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lljackie.test20_sqlite.Words.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WordsDBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list = (ListView) findViewById(R.id.lv_words);
        registerForContextMenu(list);


        mDbHelper = new WordsDBHelper(this);

        //在列表显示全部单词
        ArrayList<Map<String, String>> items = getAll();
        setWordsListView(items);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                //查找
                SearchDialog();
                return true;
            case R.id.action_insert:
                //新增单词
                InsertDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        TextView textId = null;
        TextView textWord = null;
        TextView textMeaning = null;
        TextView textSample = null;

        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;

        switch (item.getItemId()) {
            case R.id.action_delete:
                //删除单词
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textId = (TextView) itemView.findViewById(R.id.textId);
                if (textId != null) {
                    String strId = textId.getText().toString();
                    DeleteDialog(strId);
                }
                break;
            case R.id.action_update:
                //修改单词
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textId = (TextView) itemView.findViewById(R.id.textId);
                textWord = (TextView) itemView.findViewById(R.id.textViewWord);
                textMeaning = (TextView) itemView.findViewById(R.id.textViewMeaning);
                textSample = (TextView) itemView.findViewById(R.id.textViewSample);
                if (textId != null && textWord != null && textMeaning != null && textSample != null) {
                    String strId = textId.getText().toString();
                    String strWord = textWord.getText().toString();
                    String strMeaning = textMeaning.getText().toString();
                    String strSample = textSample.getText().toString();
                    UpdateDialog(strId, strWord, strMeaning, strSample);
                }
                break;
        }
        return true;
    }

    private void setWordsListView(ArrayList<Map<String, String>> items) {
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Word._ID, Word.COLUMN_NAME_WORD,
                        Word.COLUMN_NAME_MEANING, Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textId, R.id.textViewWord, R.id.textViewMeaning, R.id.textViewSample});

        ListView list = (ListView) findViewById(R.id.lv_words);
        list.setAdapter(adapter);

    }

    private ArrayList<Map<String, String>> getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                Word._ID,
                Word.COLUMN_NAME_WORD,
                Word.COLUMN_NAME_MEANING,
                Word.COLUMN_NAME_SAMPLE
        };

        //排序
        String sortOrder =
                Word.COLUMN_NAME_WORD + " DESC";


        Cursor c = db.query(
                Word.TABLE_NAME,                          // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return ConvertCursor2List(c);
    }


    private ArrayList<Map<String, String>> ConvertCursor2List(Cursor cursor) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put(Word._ID, String.valueOf(cursor.getInt(0)));
            map.put(Word.COLUMN_NAME_WORD, cursor.getString(1));
            map.put(Word.COLUMN_NAME_MEANING, cursor.getString(2));
            map.put(Word.COLUMN_NAME_SAMPLE, cursor.getString(3));
            result.add(map);
        }
        return result;    }

    //增加单词
    private void Insert(String strWord, String strMeaning, String strSample) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Word.COLUMN_NAME_WORD, strWord);
        values.put(Word.COLUMN_NAME_MEANING, strMeaning);
        values.put(Word.COLUMN_NAME_SAMPLE, strSample);

        db.insert(
                Word.TABLE_NAME,
                null,
                values);
    }


    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater()
                .inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord = ((EditText) tableLayout.findViewById(R.id.insert_word))
                                .getText().toString();
                        String strMeaning = ((EditText) tableLayout.findViewById(R.id.insert_meaning))
                                .getText().toString();
                        String strSample = ((EditText) tableLayout.findViewById(R.id.insert_sample))
                                .getText().toString();

                        Insert(strWord, strMeaning, strSample);

                        ArrayList<Map<String, String>> items = getAll();
                        setWordsListView(items);

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();

    }

    //删除单词
    private void Delete(String strId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String selection = Word._ID + " = ?";

        String[] selectionArgs = {strId};

        db.delete(Word.TABLE_NAME, selection, selectionArgs);
    }

    private void DeleteDialog(final String strId) {
        new AlertDialog.Builder(this).setTitle("删除单词").setMessage("是否真的删除单词?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Delete(strId);
                        setWordsListView(getAll());
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();

    }

    //使用Sql语句更新单词
    private void UpdateUseSql(String strId, String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "update words set word=?,meaning=?,sample=? where _id=?";
        db.execSQL(sql, new String[]{strWord, strMeaning, strSample, strId});
    }

    private void UpdateDialog(final String strId, String strWord, String strMeaning, String strSample) {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        ((EditText) tableLayout.findViewById(R.id.insert_word)).setText(strWord);
        ((EditText) tableLayout.findViewById(R.id.insert_meaning)).setText(strMeaning);
        ((EditText) tableLayout.findViewById(R.id.insert_sample)).setText(strSample);
        new AlertDialog.Builder(this)
                .setTitle("修改单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strNewWord = ((EditText) tableLayout.findViewById(R.id.insert_word)).getText().toString();
                        String strNewMeaning = ((EditText) tableLayout.findViewById(R.id.insert_meaning)).getText().toString();
                        String strNewSample = ((EditText) tableLayout.findViewById(R.id.insert_sample)).getText().toString();

                        UpdateUseSql(strId, strNewWord, strNewMeaning, strNewSample);
                        setWordsListView(getAll());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    //查找单词
    private ArrayList<Map<String, String>> SearchUseSql(String strWordSearch) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "select * from words where word like ? order by word desc";
        Cursor c = db.rawQuery(sql, new String[]{"%" + strWordSearch + "%"});

        return ConvertCursor2List(c);
    }

    private void SearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.searchterm, null);
        new AlertDialog.Builder(this)
                .setTitle("查找单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String txtSearchWord = ((EditText) tableLayout.findViewById(R.id.search)).getText().toString();

                        ArrayList<Map<String, String>> items = null;
                        items = SearchUseSql(txtSearchWord);

                        if (items.size() > 0) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("result", items);
                            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else
                            Toast.makeText(MainActivity.this, "没有找到", Toast.LENGTH_LONG).show();


                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();


    }

}
