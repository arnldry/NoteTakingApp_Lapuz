package ph.edu.comteq.notetakingapp

import android.content.ContentQueryMap
import android.os.Bundle
import android.provider.CalendarContract.EventDays.query
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import kotlinx.coroutines.launch
import ph.edu.comteq.notetakingapp.ui.theme.NoteTakingAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteTakingAppTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "notes_list"
                ) {

                    composable("notes_list") {
                        NotesListScreenWithSearch(
                            viewModel = viewModel,
                            onAddNote = {
                                navController.navigate("note_edit/new")
                            },
                            onEditNote = { noteId ->
                                navController.navigate("note_edit/$noteId")
                            }
                        )
                    }
                    composable(
                        route = "note_edit/{noteId}",
                        arguments = listOf(
                            navArgument("noteId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val noteIdString = backStackEntry.arguments?.getString("noteId")
                        val noteId =
                            if (noteIdString == "new") null else noteIdString?.toIntOrNull()

                        NoteEditScreen(
                            noteId = noteId,
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

// Separate the main screen into its own composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesListScreenWithSearch(
    viewModel: NoteViewModel,
    onAddNote: () -> Unit,
    onEditNote: (Int) -> Unit // NEW: Pass the edit action down
){
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())
    // Use notesWithTags instead of plain notes
    val notesWithTags by viewModel.allNotesWithTags.collectAsState(initial = emptyList())

    val filteredNotes = if (isSearchActive) {
        // In search mode, filter locally based on current query
        notesWithTags.filter {
            it.note.title.contains(searchQuery, ignoreCase = true) ||
                    it.note.content.contains(searchQuery, ignoreCase = true) ||
                    it.tags.any { tag -> tag.name.contains(searchQuery, ignoreCase = true) }
        }
    } else {
        notesWithTags
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if(isSearchActive){
                // Search mode: Show the SearchBar
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
                                if (!shouldExpand) {
                                    isSearchActive = false
                                    searchQuery = ""
                                    viewModel.clearSearch()
                                }
                            },
                            placeholder = {Text("Search notes...")},
                            leadingIcon = {
                                IconButton(onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                    viewModel.clearSearch()
                                }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Close search"
                                    )
                                }
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        viewModel.clearSearch()
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
                    onExpandedChange = { shouldExpand ->
                        if (!shouldExpand) {
                            isSearchActive = false
                            searchQuery = ""
                            viewModel.clearSearch()
                        }
                    }
                ) {
                    // content shown inside the search view
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        if (filteredNotes.isEmpty()) {
                            item {
                                Text(
                                    text = "No notes found",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            items(
                                items = filteredNotes,
                                key = { noteWithTags -> noteWithTags.note.id }
                            ) { note ->
                                NoteCard(
                                    note = note.note,
                                    tags = note.tags,
                                    onClick = { onEditNote(note.note.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Normal Mode
                TopAppBar(
                    title = { Text("Notes") },
                    actions = {
                        IconButton(onClick = {isSearchActive = true}) {
                            Icon(Icons.Filled.Search, "Search")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Filled.Add, "Add Note")
            }
        }
    ) { innerPadding ->
        NotesListScreen(
            notes = filteredNotes,
            onEditNote = onEditNote,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun NotesListScreen(
    notes: List<NoteWithTags>,
    onEditNote: (Int) -> Unit, // NEW: Click handler
    modifier: Modifier = Modifier){

    //plain notes
    //  val notes by viewModel.allNotes.collectAsState(initial = emptyList())


    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(
            items = notes,
            key = { noteWithTags -> noteWithTags.note.id }
        ) { note ->
            NoteCard(
                note = note.note,
                tags = note.tags,
                onClick = { onEditNote(note.note.id) }
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    tags: List<Tag> = emptyList(),
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateTime(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (note.category.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = note.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Title
            Text(
                text = note.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Content preview
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // NEW: Show tags
            if (note.content.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(
    tag: Tag,
    onRemove: (() -> Unit)? = null
) {
    Surface(
        color = Color(android.graphics.Color.parseColor(tag.color)).copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            1.dp,
            Color(android.graphics.Color.parseColor(tag.color))
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color(android.graphics.Color.parseColor(tag.color))
            )

            onRemove?.let {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove tag",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { it() },
                    tint = Color(android.graphics.Color.parseColor(tag.color))
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Int?,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit) {

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()
    val tagList by viewModel.allTags.collectAsState(initial = emptyList())
    val categoryOptions = tagList.map { it.name }.distinct()


    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNoteById(noteId)?.let { note ->
                title = note.title
                content = note.content
                selectedCategories = note.category.split(",").map {
                    it.trim() }.filter { it.isNotEmpty() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            "Back")
                    }
                },
                actions = {
                    if (noteId != null) {
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.getNoteById(noteId)?.let { noteToDelete ->
                                    viewModel.delete(noteToDelete)
                                    onNavigateBack()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                        }
                    }

                    IconButton(onClick = {
                        val noteToSave = Note(
                            id = noteId ?: 0,
                            title = title,
                            content = content,
                            category = selectedCategories.joinToString(","),
                        )

                        scope.launch {
                            if (noteId == null) {
                                viewModel.insert(noteToSave)
                            } else {
                                viewModel.update(noteToSave)
                            }
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Save Note")
                    }
                }
            )
        }
    )  { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategories.joinToString(", "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categoryOptions.forEach { categoryName ->
                        val isSelected = selectedCategories.contains(categoryName)
                        DropdownMenuItem(
                            text = { Text(categoryName) },
                            onClick = {
                                selectedCategories = if (isSelected) {
                                    selectedCategories - categoryName
                                } else {
                                    selectedCategories + categoryName
                                }
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected")
                                }
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}