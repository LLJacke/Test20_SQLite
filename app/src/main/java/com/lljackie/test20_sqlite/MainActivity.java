package com.lljackie.test20_sqlite;

import android.content.ContentValues;
import android.content.DialogInterface;
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

import java.util.ArrayList;
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
                new String[]{Words.Word._ID, Words.Word.COLUMN_NAME_WORD,
                        Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textId, R.id.textViewWord, R.id.textViewMeaning, R.id.textViewSample});

        ListView list = (ListView) findViewById(R.id.lv_words);
        list.setAdapter(adapter);

    }

    private ArrayList<Map<String, String>> getAll() {
        return null;
    }


    //增加单词
    private void Insert(String strWord, String strMeaning, String strSample) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Words.Word.COLUMN_NAME_WORD, strWord);
        values.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
        values.put(Words.Word.COLUMN_NAME_SAMPLE, strSample);

        long newRowId;
        newRowId = db.insert(
                Words.Word.TABLE_NAME,
                null,
                values);
    }

    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater()
                .inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord=((EditText)tableLayout.findViewById(R.id.insert_word))
                                .getText().toString();
                        String strMeaning=((EditText)tableLayout.findViewById(R.id.insert_meaning))
                                .getText().toString();
                        String strSample=((EditText)tableLayout.findViewById(R.id.insert_sample))
                                .getText().toString();

                        //既可以使用Sql语句插入，也可以使用使用insert方法插入
                        // InsertUserSql(strWord, strMeaning, strSample);
                        Insert(strWord, strMeaning, strSample);

                        ArrayList<Map<String, String>> items=getAll();
                        setWordsListView(items);

                    }
                })
                //取消按钮及其动作
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
        String selection = Words.Word._ID + " = ?";

        String[] selectionArgs = {strId};

        db.delete(Words.Word.TABLE_NAME, selection, selectionArgs);
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
    private void UpdateUseSql(String strId,String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql="update words set word=?,meaning=?,sample=? where _id=?";
        db.execSQL(sql, new String[]{strWord, strMeaning, strSample,strId});
    }

    private void UpdateDialog(String strId, String strWord, String strMeaning, String strSample) {

    }

    private void SearchDialog() {

    }

}
