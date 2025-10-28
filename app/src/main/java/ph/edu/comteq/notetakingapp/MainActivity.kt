package ph.edu.comteq.notetakingapp

import android.content.ContentQueryMap
import android.os.Bundle
import android.provider.CalendarContract.EventDays.query
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import org.tensorflow.lite.support.label.Category
import ph.edu.comteq.notetakingapp.ui.theme.NoteTakingAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()




    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteTakingAppTheme {
                var searchQuery by remember { mutableStateOf("") }
                var isSearchActive by remember { mutableStateOf(false) }
                val notes by viewModel.allNotes.collectAsState(emptyList())






                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    topBar = {
                        if (isSearchActive) {
                            // SEARCH MODE: Show the SearchBar
                            SearchBar(
                                modifier = Modifier.fillMaxWidth(),
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = searchQuery,
                                        onQueryChange = {
                                            searchQuery = it
                                            viewModel.updateSearchQuery(it)
                                        },
                                        onSearch = {},
                                        expanded = true,
                                        onExpandedChange = { shouldExpand ->
                                            // This is called when the system wants to change expanded state
                                            if (!shouldExpand) {
                                                // User wants to collapse/exit search
                                                isSearchActive = false
                                                searchQuery = ""
                                                viewModel.clearSearchQuery()
                                            }
                                        },
                                        placeholder = {Text("Search notes...")},
                                        leadingIcon = {
                                            IconButton(onClick = {
                                                isSearchActive = false
                                                searchQuery = ""
                                                viewModel.clearSearchQuery()
                                            }) {
                                                Icon(
                                                    Icons.Default.ArrowBack,
                                                    contentDescription = "Close search"
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = {
                                                    searchQuery = ""
                                                    viewModel.clearSearchQuery()
                                                }) {
                                                    Icon(
                                                        Icons.Default.Clear,
                                                        contentDescription = "Clear search"
                                                    )
                                                }
                                            }
                                        }
                                    )
                                },

                                expanded = true,
                                onExpandedChange = {
                                    if(it){
                                        isSearchActive = false
                                        searchQuery = ""
                                        viewModel.clearSearchQuery()
                                    }
                                }
                            ) {
                                // Content shown INSIDE the search view
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    if (notes.isEmpty()) {
                                        item {
                                            Text(
                                                text = "No notes found",
                                                modifier = Modifier.padding(16.dp),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    } else {
                                        items(notes) { note ->
                                            NoteCard(note = note)
                                        }
                                    }
                                }
                            }

                        } else {
                            // NORMAL MODE: Show regular TopAppBar
                            TopAppBar(
                                title = { Text("Notes") },
                                actions = {
                                    IconButton(onClick = { isSearchActive = true }) {
                                        Icon(Icons.Filled.Search, "Search")
                                    }
                                }
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {/*TODO*/}) {
                            Icon(Icons.Filled.Add, "Add note")
                        }
                    }
                )



                { innerPadding ->
                    NotesListScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NotesListScreen(viewModel: NoteViewModel, modifier: Modifier = Modifier){
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())
    val notesWithTags by viewModel.allNotesWithTags.collectAsState(initial = emptyList())

    LazyColumn(modifier = modifier) {
        items(notesWithTags) { note ->
            NoteCard(note = note.note, tags = note.tags)


        }
    }
}

@Composable
fun NoteCard(
    tags: List<Tag> = emptyList(),
    note: Note, modifier: Modifier = Modifier){
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    ){
        Column(
            modifier = Modifier
                .padding(16.dp))
        {
            Text(
                text = DateUtils.formatDate(note.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )


            Text(
                text = note.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if(tags.isEmpty()){
                tags.forEach { tag ->
                    Text(text = tag.name)
                }

            }
        }
    }
}









//@Preview(showBackground = true)
//@Composable
//fun NotesListScreenPreview() {
//    NoteTakingAppTheme {
//        NotesListScreen()
//    }
//}