package com.oussama_chatri.productivityx.features.notes.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveNotesUseCase @Inject constructor(private val repo: NoteRepository) {
    operator fun invoke(tagId: String? = null, pinnedOnly: Boolean = false): Flow<List<Note>> =
        repo.observeActiveNotes(tagId, pinnedOnly)
}

class ObserveTrashUseCase @Inject constructor(private val repo: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repo.observeTrash()
}

class GetNoteByIdUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String): Resource<Note> = repo.getNoteById(noteId)
}

class CreateNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(
        title: String? = null,
        content: String? = null,
        tagIds: Set<String>? = null,
        pinned: Boolean? = null
    ): Resource<Note> = repo.createNote(title, content, tagIds, pinned)
}

class UpdateNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(
        noteId: String,
        title: String? = null,
        content: String? = null,
        tagIds: Set<String>? = null,
        pinned: Boolean? = null
    ): Resource<Note> = repo.updateNote(noteId, title, content, tagIds, pinned)
}

class PinNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String): Resource<Note> = repo.pinNote(noteId)
}

class UnpinNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String): Resource<Note> = repo.unpinNote(noteId)
}

class SoftDeleteNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String): Resource<Note> = repo.softDeleteNote(noteId)
}

class RestoreNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String): Resource<Note> = repo.restoreNote(noteId)
}

class HardDeleteNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String): Resource<Unit> = repo.hardDeleteNote(noteId)
}

class AddTagToNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String, tagId: String): Resource<Note> =
        repo.addTagToNote(noteId, tagId)
}

class RemoveTagFromNoteUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: String, tagId: String): Resource<Note> =
        repo.removeTagFromNote(noteId, tagId)
}

class RefreshNotesUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(): Resource<Unit> = repo.refreshNotes()
}
