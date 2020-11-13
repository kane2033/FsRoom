package com.focusstart.fsroom.room.dao

import androidx.room.*
import com.focusstart.fsroom.room.entity.Contact

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts")
    suspend fun getAll(): List<Contact>

    @Query("SELECT * FROM contacts ORDER BY name")
    suspend fun getAllByName(): List<Contact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()
}