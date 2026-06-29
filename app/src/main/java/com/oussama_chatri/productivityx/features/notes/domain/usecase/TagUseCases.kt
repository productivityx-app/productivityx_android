package com.oussama_chatri.productivityx.features.notes.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteFolder
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteTemplate
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import com.oussama_chatri.productivityx.features.notes.domain.repository.FolderRepository
import com.oussama_chatri.productivityx.features.notes.domain.repository.TagRepository
import com.oussama_chatri.productivityx.features.notes.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTagsUseCase @Inject constructor(private val repo: TagRepository) {
    operator fun invoke(): Flow<List<Tag>> = repo.observeTags()
}

class GetTagsUseCase @Inject constructor(private val repo: TagRepository) {
    suspend operator fun invoke(): Resource<List<Tag>> = repo.getTags()
}

class CreateTagUseCase @Inject constructor(private val repo: TagRepository) {
    suspend operator fun invoke(name: String, color: String? = null): Resource<Tag> =
        repo.createTag(name, color)
}

class UpdateTagUseCase @Inject constructor(private val repo: TagRepository) {
    suspend operator fun invoke(tagId: String, name: String, color: String? = null): Resource<Tag> =
        repo.updateTag(tagId, name, color)
}

class DeleteTagUseCase @Inject constructor(private val repo: TagRepository) {
    suspend operator fun invoke(tagId: String): Resource<Unit> = repo.deleteTag(tagId)
}

class RefreshTagsUseCase @Inject constructor(private val repo: TagRepository) {
    suspend operator fun invoke(): Resource<Unit> = repo.refreshTags()
}

class ObserveFoldersUseCase @Inject constructor(private val repo: FolderRepository) {
    operator fun invoke(): Flow<List<NoteFolder>> = repo.observeFolders()
}

class CreateFolderUseCase @Inject constructor(private val repo: FolderRepository) {
    suspend operator fun invoke(name: String, color: String? = null, parentFolderId: String? = null): Resource<NoteFolder> =
        repo.createFolder(name, color, parentFolderId)
}

class UpdateFolderUseCase @Inject constructor(private val repo: FolderRepository) {
    suspend operator fun invoke(folderId: String, name: String, color: String? = null): Resource<NoteFolder> =
        repo.updateFolder(folderId, name, color)
}

class DeleteFolderUseCase @Inject constructor(private val repo: FolderRepository) {
    suspend operator fun invoke(folderId: String): Resource<Unit> = repo.deleteFolder(folderId)
}

class ObserveTemplatesUseCase @Inject constructor(private val repo: TemplateRepository) {
    operator fun invoke(): Flow<List<NoteTemplate>> = repo.observeTemplates()
}

class CreateTemplateUseCase @Inject constructor(private val repo: TemplateRepository) {
    suspend operator fun invoke(name: String, content: String, icon: String? = null): Resource<NoteTemplate> =
        repo.createTemplate(name, content, icon)
}

class DeleteTemplateUseCase @Inject constructor(private val repo: TemplateRepository) {
    suspend operator fun invoke(templateId: String): Resource<Unit> = repo.deleteTemplate(templateId)
}
