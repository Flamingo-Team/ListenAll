package com.example.wenhai.listenall.utils

import android.content.Context
import com.example.wenhai.listenall.data.bean.DaoMaster
import com.example.wenhai.listenall.data.bean.DaoSession


object DAOUtil {

    const val TAG = "DAOUtil"
    private const val DATABASE_NAME = "ListenAll.db"

    @JvmStatic
    private var daoSession: DaoSession? = null

    @JvmStatic
    fun getSession(context: Context): DaoSession {
        if (daoSession == null) {
            synchronized(DAOUtil::class.java) {
                if (daoSession == null) {
                    val devHelper = DaoMaster.DevOpenHelper(context, DATABASE_NAME)
                    val daoMaster = DaoMaster(devHelper.writableDb)
                    daoSession = daoMaster.newSession()
                }
            }
        }
        return daoSession !!

    }
}