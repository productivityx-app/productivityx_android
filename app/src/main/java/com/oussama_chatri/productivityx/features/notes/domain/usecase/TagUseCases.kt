package com.oussama_chatri.productivityx.features.notes.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import com.oussama_chatri.productivityx.features.notes.domain.repository.TagRepository
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
