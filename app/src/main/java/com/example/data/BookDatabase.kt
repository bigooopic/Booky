package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val pagesJson: String, // JSON array of BookPage
    val createdAt: Long = System.currentTimeMillis()
)

data class BookPage(
    val pageNumber: Int,
    val text: String,
    val imageData: String? = null // Base64 image data or drawing
)

object BookPageConverter {
    fun pagesToJson(pages: List<BookPage>): String {
        val array = JSONArray()
        for (page in pages) {
            val obj = JSONObject()
            obj.put("pageNumber", page.pageNumber)
            obj.put("text", page.text)
            if (page.imageData != null) {
                obj.put("imageData", page.imageData)
            } else {
                obj.put("imageData", JSONObject.NULL)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToPages(json: String): List<BookPage> {
        val list = mutableListOf<BookPage>()
        if (json.isEmpty()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    BookPage(
                        pageNumber = obj.optInt("pageNumber", i + 1),
                        text = obj.optString("text", ""),
                        imageData = if (obj.isNull("imageData")) null else obj.optString("imageData")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)
}

@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}
