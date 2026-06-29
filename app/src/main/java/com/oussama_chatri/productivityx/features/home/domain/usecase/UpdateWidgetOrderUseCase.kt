package com.oussama_chatri.productivityx.features.home.domain.usecase

import com.oussama_chatri.productivityx.features.home.domain.model.WidgetType
import javax.inject.Inject

class UpdateWidgetOrderUseCase @Inject constructor() {
    operator fun invoke(
        currentOrder: List<WidgetType>,
        usageCount: Map<WidgetType, Int>,
    ): List<WidgetType> {
        if (usageCount.values.sum() < 20) return currentOrder

        return currentOrder.sortedByDescending { usageCount[it] ?: 0 }
    }
}
