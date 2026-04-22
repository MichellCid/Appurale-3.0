package com.example.appurale3.auth.presentation.busquedas_filtrado
/*
import com.example.appurale3.data.models.Activity
import com.example.appurale3.data.models.Routine


data class SearchUiState(
    val query: String = "",
    val routines: List<Routine> = emptyList(),
    val activities: List<Activity> = emptyList(),

    val selectedType: FilterType = FilterType.ALL,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),

    val showFilters: Boolean = false
) {
    val results: List<SearchItem>
        get() {
            val q = query.lowercase()

            val routineResults = routines
                .filter {
                    (it.name.contains(q, true) ||
                            it.category.contains(q, true)) &&
                            (selectedCategory == null || it.category == selectedCategory)
                }
                .map { SearchItem.RoutineItem(it) }

            val activityResults = activities
                .filter {
                    (it.name.contains(q, true) ||
                            it.category.contains(q, true)) &&
                            (selectedCategory == null || it.category == selectedCategory)
                }
                .map { SearchItem.ActivityItem(it) }

            return when (selectedType) {
                FilterType.ALL -> routineResults + activityResults
                FilterType.ROUTINES -> routineResults
                FilterType.ACTIVITIES -> activityResults
            }
        }
}


 */