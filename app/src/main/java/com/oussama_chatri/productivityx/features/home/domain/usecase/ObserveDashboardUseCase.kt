package com.oussama_chatri.productivityx.features.home.domain.usecase

import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDashboardUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    operator fun invoke(): Flow<DashboardSummary> = repository.observeDashboard()
}
