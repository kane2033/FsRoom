package com.focusstart.fsroom.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.focusstart.fsroom.room.dao.ContactDao
import com.focusstart.fsroom.room.entity.Contact

@Database(entities = [Contact::class], version = 1)
abstract class ContactsDatabase : RoomDatabase() {

    companion object {
        private const val NAME = "contacts_db"

        fun getInstance(context: Context) = Room.databaseBuilder(
            context,
            ContactsDatabase::class.java,
            NAME
        ).build()
    }

    abstract fun userDao(): ContactDao
}