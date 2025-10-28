package ph.edu.comteq.notetakingapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.serialization.serializers.MutableStateFlowSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch


class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    //Show all notes OR notes that match the search query
    //val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    val allNotes: Flow<List<Note>> = searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            noteDao.getAllNotes()
        } else {
            noteDao.searchNotes(query)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }





    fun insert(note: Note) = viewModelScope.launch {
        noteDao.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        noteDao.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote(note)
    }

    val allNotesWithTags: Flow<List<NoteWithTags>> = noteDao.getNotesWithTags()

    suspend fun getNoteById(id: Int): Note?{
        return noteDao.getNoteById(id)
    }

    suspend fun getNoteWithTags(noteId: Int): NoteWithTags? {
        return noteDao.getNoteWithTags(noteId.toLong())
    }

    fun addTagToNote(noteId: Int, tagId: Int) = viewModelScope.launch {}

}