package com.example.hayatatodolist

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hayatatodolist.adapter.TableRowAdapter
import com.example.hayatatodolist.adapter.TablerowListener
import com.example.hayatatodolist.database.DatabaseHelper
import com.example.hayatatodolist.model.Todo
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random


class MainActivity : AppCompatActivity(), TablerowListener {

    private lateinit var tableRecyclerView : RecyclerView
    private var todoList = ArrayList<Todo>()
    private var completedList = ArrayList<Int>()
    private lateinit var tableRowAdapter: TableRowAdapter
    private val _dbhelper = DatabaseHelper(this@MainActivity,"hayatatodo.db", null, 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onStart() {
//        super.onStart()
//
//        this.init()
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        this.init()
    }

    override fun onDestroy() {
        // DatabaseHelperオブジェクトの解放
        _dbhelper.close()

        // アクティビティの終了
        super.onDestroy()
    }

    override fun yoteiChanged(index:Int, isChecked: Boolean) {
        val todo = todoList[index]
        todo.yotei = isChecked
        this.updateTran(todo.listId, todo.no, "YOTEI" ,todo.yotei)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun jissekiChanged(index:Int, isChecked: Boolean) {
        val todo = todoList[index]
        todo.jisseki = isChecked
        this.updateTran(todo.listId, todo.no, "JISSEKI" ,todo.jisseki)

        if (isChecked) {
            // その日に該当のリストを初めてコンプリートした場合
            if (!completedList.contains(todo.listId) && this.isCompleteGroup(todo.listId)) {
                // トーストを表示
                toastMake("「" + todo.listTitle + "」\nComplete!!", this@MainActivity)
                // 完了リストに追加
                completedList.add(todo.listId)
                insertCompleteList(todo.listId)
            }
        }
    }

    override fun kakuninChanged(index:Int, isChecked: Boolean) {
        val todo = todoList[index]
        todo.kakunin = isChecked
        this.updateTran(todo.listId, todo.no, "KAKUNIN" ,todo.kakunin)
    }

    override fun yoteiHeaderClicked(index: Int) {
        val listId = todoList[index].listId
        for (todo in todoList) {
            if (todo.listId == listId && !todo.yotei) {
                todo.yotei = true
                this.updateTran(todo.listId, todo.no, "YOTEI" ,todo.yotei )
            }
        }
        // リストをrecyclerviewにバインドする
        this.bindTodoList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun jissekiHeaderClicked(index: Int) {
        val listId = todoList[index].listId
        for (todo in todoList) {
            if (todo.listId == listId && todo.yotei && !todo.jisseki) {
                todo.jisseki = true
                this.updateTran(todo.listId, todo.no, "JISSEKI" ,todo.jisseki )
            }
        }
        // リストをrecyclerviewにバインドする
        this.bindTodoList()

        // その日に該当のリストを初めてコンプリートした場合
        if (!completedList.contains(listId)) {
            // トースト表示
            toastMake("「" + todoList[index].listTitle + "」\nComplete!!", this@MainActivity)
            // 完了リストに追加
            completedList.add(listId)
            insertCompleteList(listId)
        }
    }

    override fun kakuninHeaderClicked(index: Int) {
        val listId = todoList[index].listId
        for (todo in todoList) {
            if (todo.listId == listId && todo.jisseki && !todo.kakunin) {
                todo.kakunin = true
                this.updateTran(todo.listId, todo.no, "KAKUNIN" ,todo.kakunin )
            }
        }
        // リストをrecyclerviewにバインドする
        this.bindTodoList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        setContentView(R.layout.activity_main)

        // 現在日時を表示
        val tv = findViewById<View>(R.id.date) as TextView
        val currentDate = this.getCurrentDate()
        tv.text = currentDate

        try {
            // 最終使用日から日付が変わった場合
            if (currentDate != getLatestDate()) {
                toastMake("今日も元気にがんばろう！", this@MainActivity)
                // 当日日付を保存
                this.insertLatestDate( currentDate)
                // リストデータの洗い替え
                this.deleteTran()
                this.insertTran()
                // 完了リスト配列の初期化
                this.deleteCompleteList()
            }

            // リストデータ取得
            val cursor = this.getListData()

            todoList.clear()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val todo = Todo(cursor.getInt(1),cursor.getString(0),cursor.getInt(2),cursor.getString(3),
                        cursor.getInt(4) == 1, cursor.getInt(5) == 1, cursor.getInt(6) == 1, defaultYotei = true)

                    todoList.add(todo)

                    cursor.moveToNext()
                }
            }

            // リストをrecyclerviewにバインドする
            this.bindTodoList()

            // 完了リスト取得
            val cursor2 = this.getCompleteListData()

            completedList.clear()
            if (cursor2.count > 0) {
                cursor2.moveToFirst()
                while (!cursor2.isAfterLast) {
                    completedList.add(cursor2.getInt(0))

                    cursor2.moveToNext()
                }
            }
        } catch (exception: Exception) {
            Log.e("init", exception.toString());
        }
    }

    private fun bindTodoList() {
        tableRecyclerView = findViewById(R.id.table_recycler_view)
        tableRowAdapter = TableRowAdapter(todoList, this)

        tableRecyclerView.layoutManager = LinearLayoutManager(this)
        tableRecyclerView.adapter = tableRowAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate() :String {
        val date = LocalDateTime.now()
        val dtformat = DateTimeFormatter.ofPattern("yyyy年M月d日")
        val fdate = dtformat.format(date)

        return fdate
    }

    private fun insertTran() {
        val db = _dbhelper.writableDatabase
        try {
            val sb = StringBuilder()
            sb.append("INSERT INTO LIST_TRAN (LIST_ID ,NO ,YOTEI, JISSEKI, KAKUNIN)")
            sb.append("SELECT LIST_ID ,NO, YOTEI_DEFAULT, 0, 0 FROM LIST_MST_DETAIL")

            val stmt = db.compileStatement(sb.toString())
            stmt.executeInsert()
        }catch(exception: Exception) {
            Log.e("insertTran", exception.toString());
        }
    }

    private fun insertCompleteList(listId: Int) {
        val db = _dbhelper.writableDatabase
        try {
            val sb = StringBuilder()
            sb.append("INSERT INTO COMPLETE_LIST (LIST_ID) VALUES (${listId})" )

            val stmt = db.compileStatement(sb.toString())
            stmt.executeInsert()
        }catch(exception: Exception) {
            Log.e("insertCompleteList", exception.toString());
        }
    }

    private fun updateTran(listId:Int, no:Int ,col: String,isChecked: Boolean) {
        val db = _dbhelper.writableDatabase
        try {
            val values = ContentValues()
            values.put(col, isChecked)
            val whereClauses = "LIST_ID = ? AND NO = ?"
            val whereArgs = arrayOf(listId.toString(), no.toString())
            db.update("LIST_TRAN", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateTran", exception.toString());
        }
    }

    private fun deleteTran() {
        val db = _dbhelper.writableDatabase
        try {
            val stmt = db.compileStatement("DELETE FROM LIST_TRAN")
            stmt.executeUpdateDelete()
        } catch (exception: Exception) {
            Log.e("deleteTran", exception.toString());
        }
    }

    private fun deleteCompleteList() {
        val db = _dbhelper.writableDatabase
        try {
            val stmt = db.compileStatement("DELETE FROM COMPLETE_LIST")
            stmt.executeUpdateDelete()
        } catch (exception: Exception) {
            Log.e("deleteCompleteList", exception.toString());
        }
    }

    private fun getListData(): Cursor {
        val db = _dbhelper.readableDatabase
        val sb = StringBuilder()
        sb.append("SELECT")
        sb.append("    A.TITLE AS LIST_TITLE, C.LIST_ID, C.NO, B.TITLE AS TODO_TITLE, C.YOTEI, C.JISSEKI, C.KAKUNIN ")
        sb.append("FROM LIST_MST A ")
        sb.append("INNER JOIN LIST_MST_DETAIL B ")
        sb.append(" ON B.LIST_ID = A.ID ")
        sb.append("INNER JOIN LIST_TRAN C ")
        sb.append(" ON C.LIST_ID = B.LIST_ID ")
        sb.append("AND C.NO = B.NO ")
        sb.append("ORDER BY ")
        sb.append("    C.LIST_ID, C.NO")

        return db.rawQuery(sb.toString(), null)
    }

    private fun getCompleteListData(): Cursor {
        val db = _dbhelper.readableDatabase
        return db.rawQuery("SELECT LIST_ID FROM COMPLETE_LIST", null)
    }

    private fun getLatestDate(): String {
        val db = _dbhelper.readableDatabase
        var ret = ""
        val cursor = db.rawQuery("SELECT LATEST_DATE FROM LATEST_DATE", null)

        if (cursor.count > 0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                ret = cursor.getString(0)

                cursor.moveToNext()
            }
        }
        return ret
    }

    private fun insertLatestDate(date: String) {
        val db = _dbhelper.writableDatabase
        try {
            var stmt = db.compileStatement("DELETE FROM LATEST_DATE")
            stmt.executeUpdateDelete()

            stmt = db.compileStatement("INSERT INTO LATEST_DATE (LATEST_DATE) VALUES ('${date}')")
            stmt.executeInsert()
        }catch(exception: Exception) {
            Log.e("insertLatestDate", exception.toString());
        }
    }

    private fun isCompleteGroup(listId: Int) :Boolean {
        var isComplete = true
        for (todo in todoList) {
            if (todo.listId != listId) {
                continue
            } else if (todo.yotei && !todo.jisseki) {
                isComplete = false
                break
            }
        }
        return isComplete
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toastMake(msg: String?, cont: Context?) {
        val toast = Toast(cont)
        val inflater = layoutInflater

        // relative_layout:custom_toast.xmlのRelativeLayoutに付けたID
        val viewGroup = findViewById<ViewGroup>(R.id.relative_layout)
        val view: View = inflater.inflate(R.layout.custom_toast, viewGroup)

        // メッセージ設定
        val textView = view.findViewById<TextView>(R.id.message)
        textView.text = msg

        // 画像設定
        val imageView = view.findViewById<ImageView>(R.id.toast_image)
        val image = this.getBitmapFromAsset();
        imageView.setImageBitmap(image)

        // 非推奨
        toast.setView(view)

        // 表示時間
        toast.duration = Toast.LENGTH_SHORT
        // 位置調整
        toast.setGravity(Gravity.CENTER, 0, 0)

        toast.show()
    }

    private fun getBitmapFromAsset(): Bitmap? {
        val assetManager = assets
        var istr: InputStream? = null
        try {
            val list = assetManager.list("image")
            val randomIndex = Random.nextInt(list!!.size)
            istr = assetManager.open("image/" + list[randomIndex])
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return BitmapFactory.decodeStream(istr)
    }
}