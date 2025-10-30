package ph.edu.comteq.notetakingapp

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class NoteWithTags(
    @Embedded
    val note: Note,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value=NoteTagCrossRef::class,
            parentColumn = "note_id",
            entityColumn = "tag_id")
    )

    val tags: List<Tag>
)
