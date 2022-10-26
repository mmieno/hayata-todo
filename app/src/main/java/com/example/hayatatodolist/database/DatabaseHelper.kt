package com.example.hayatatodolist.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class DatabaseHelper(context: Context, databaseName:String, factory: SQLiteDatabase.CursorFactory?, version: Int): SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    // コンテキスト
    private val mContext: Context = context
    // private定数の定義
    companion object {
        // DB名
        private const val DATABASE_NAME = "hayatatodo.db"
        // 生成時のDBバージョン番号
        private const val DATABASE_VERSION = 11
        // リストマスタのDDL
        private const val SQL_CREATE_LIST_MST =
            "CREATE TABLE LIST_MST (" +
                    " ID NUMERIC" +
                    ",TITLE TEXT" +
                    ")"

        // リストマスタ明細のDDL
        private const val SQL_CREATE_LIST_MST_DETAIL =
            "CREATE TABLE LIST_MST_DETAIL (" +
                    " LIST_ID NUMERIC" +
                    ",NO NUMERIC" +
                    ",TITLE TEXT" +
                    ",YOTEI_DEFAULT NUMERIC" +
                    ")"

        // リストトランザクションのDDL
        private const val SQL_CREATE_LIST_TRAN =
            "CREATE TABLE LIST_TRAN (" +
                    " LIST_ID NUMERIC" +
                    ",NO NUMERIC" +
                    ",YOTEI NUMERIC" +
                    ",JISSEKI NUMERIC" +
                    ",KAKUNIN NUMERIC" +
                    ")"

        // 完了リストのDDL
        private const val SQL_CREATE_COMPLETE_LIST =
            "CREATE TABLE COMPLETE_LIST (" +
                    " LIST_ID NUMERIC" +
                    ")"

        // 最新日付のDDL
        private const val SQL_CREATE_LATEST_DATE =
            "CREATE TABLE LATEST_DATE (" +
                    " LATEST_DATE TEXT" +
                    ")"

        // テーブル削除DDL
        private const val SQL_DROP_LIST_MST = "DROP TABLE IF EXISTS LIST_MST"
        private const val SQL_DROP_LIST_MST_DETAIL = "DROP TABLE IF EXISTS LIST_MST_DETAIL"
        private const val SQL_DROP_LIST_TRAN = "DROP TABLE IF EXISTS LIST_TRAN"
        private const val SQL_DROP_COMPLETE_LIST = "DROP TABLE IF EXISTS COMPLETE_LIST"
        private const val SQL_DROP_LATEST_DATE = "DROP TABLE IF EXISTS LATEST_DATE"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_LIST_MST)
        db?.execSQL(SQL_CREATE_LIST_MST_DETAIL)
        db?.execSQL(SQL_CREATE_LIST_TRAN)
        db?.execSQL(SQL_CREATE_COMPLETE_LIST)
        db?.execSQL(SQL_CREATE_LATEST_DATE)

        // リストマスタの初期データ挿入
        this.insertListMst(db)

        // リストマスタ明細の初期データ挿入
        this.insertListMstDetail(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL(SQL_DROP_LIST_MST)
        db?.execSQL(SQL_DROP_LIST_MST_DETAIL)
        db?.execSQL(SQL_DROP_LIST_TRAN)
        db?.execSQL(SQL_DROP_COMPLETE_LIST)
        db?.execSQL(SQL_DROP_LATEST_DATE)
        onCreate(db)
    }

    private fun insertListMst(db: SQLiteDatabase?) {

        val sb = StringBuilder()

        // リストマスタの初期データ挿入
        sb.append("INSERT INTO LIST_MST VALUES")

        try {
            // CSVファイル読込
            val file = mContext.resources.assets.open("data/list_mst.csv")
            val fileReader = BufferedReader(InputStreamReader(file))
            for (line in fileReader.readLines()) {
                val rowData = line.split(",").toTypedArray()
                sb.append(" (${rowData[0]}, '${rowData[1]}'),")
            }
            file.close()

            // INSERT実行
            val sql = sb.substring(0, sb.length -1).toString()
            val stmt = db?.compileStatement(sql)
            stmt?.executeInsert()
        } catch (exception: Exception) {
            Log.e("insertListMst", exception.toString());
        }
    }

    private fun insertListMstDetail(db: SQLiteDatabase?) {

        val sb = StringBuilder()

        // リストマスタ明細の初期データ挿入
        sb.append("INSERT INTO LIST_MST_DETAIL VALUES")

        try {
            // CSVファイル読込
            val file = mContext.resources.assets.open("data/list_mst_detail.csv")
            val fileReader = BufferedReader(InputStreamReader(file))
            for (line in fileReader.readLines()) {
                val rowData = line.split(",").toTypedArray()
                sb.append(" (${rowData[0]}, ${rowData[1]}, '${rowData[2]}', ${rowData[3]}),")
            }
            file.close()

            // INSERT実行
            val sql = sb.substring(0, sb.length -1).toString()
            val stmt = db?.compileStatement(sql)
            stmt?.executeInsert()
        } catch (exception: Exception) {
            Log.e("insertListMstDetail", exception.toString());
        }
    }
}