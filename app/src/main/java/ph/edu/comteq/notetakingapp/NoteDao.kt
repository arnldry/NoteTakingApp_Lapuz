package ph.edu.comteq.notetakingapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {
    @Insert
    suspend fun insertNote(note: Note): Long
    @Update
    suspend fun updateNote(note: Note)
    @Delete
    suspend fun deleteNote(note: Note)


    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Note?

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes WHERE title " +
            "LIKE '%'  || :searchQeury || '%' " +
            "OR content LIKE '%'  || :searchQeury || '%' ORDER BY id DESC")
    fun searchNotes(searchQeury: String): Flow<List<Note>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotetagCrossRef(noteTagCrossRef: NoteTagCrossRef)

    //Disconnecti a note
    @Delete
    suspend fun deleteNotetagCrossRef(noteTagCrossRef: NoteTagCrossRef)

    //get all notes with their tags
    @Transaction
    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getNotesWithTags(): Flow<List<NoteWithTags>>

    //get a note with its tags
    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteWithTags(noteId: Long): NoteWithTags?





}