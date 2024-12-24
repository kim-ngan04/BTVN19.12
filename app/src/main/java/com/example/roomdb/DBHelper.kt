package com.example.roomdb

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

@Database(entities = arrayOf(StudentModel::class), version = 1)
abstract class StudentDB : RoomDatabase() {
    abstract fun studentDAO(): StudentDAO
}

@Dao
interface StudentDAO {
    @Query("SELECT * FROM StudentModel")
    fun getAll(): List<StudentModel>

    @Query("SELECT * FROM StudentModel WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<StudentModel>

    @Insert
    fun insert(vararg users: StudentModel)
    @Update
    fun update(user: StudentModel)
    @Delete
    fun delete(user: StudentModel)

    @Query("DELETE FROM StudentModel WHERE uid = :id")
    fun deleteById(id: Int)
}