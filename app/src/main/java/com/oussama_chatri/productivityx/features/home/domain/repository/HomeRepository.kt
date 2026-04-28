package com.oussama_chatri.productivityx.features.home.domain.repository

import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeDashboard(): Flow<DashboardSummary>
}
