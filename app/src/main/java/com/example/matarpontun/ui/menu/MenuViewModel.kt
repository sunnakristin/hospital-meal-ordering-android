package com.example.matarpontun.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.remote.dto.FoodTypeDto
import com.example.matarpontun.data.remote.dto.MenuDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** State for the menu content area (below the food type selector). */
sealed class MenuUiState {
    object Idle : MenuUiState()
    object Loading : MenuUiState()
    data class Success(val menu: MenuDetailDto) : MenuUiState()
    object NoMenu : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}

class MenuViewModel : ViewModel() {

    private val _foodTypes = MutableStateFlow<List<FoodTypeDto>>(emptyList())
    val foodTypes: StateFlow<List<FoodTypeDto>> = _foodTypes

    private val _menuState = MutableStateFlow<MenuUiState>(MenuUiState.Idle)
    val menuState: StateFlow<MenuUiState> = _menuState

    init {
        loadFoodTypes()
    }

    /** Fetches the available food types to populate the pill selector. */
    private fun loadFoodTypes() {
        viewModelScope.launch {
            try {
                _foodTypes.value = AppContainer.api.getFoodTypes()
            } catch (e: Exception) {
                // Non-critical — screen will show empty selector
            }
        }
    }

    /** Loads today's menu for [foodTypeId] and updates [menuState]. */
    fun loadMenu(foodTypeId: Long) {
        _menuState.value = MenuUiState.Loading
        viewModelScope.launch {
            try {
                val menu = AppContainer.api.getMenuForFoodType(foodTypeId)
                _menuState.value = MenuUiState.Success(menu)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _menuState.value = MenuUiState.NoMenu
                } else {
                    _menuState.value = MenuUiState.Error(e.message ?: "Failed to load menu")
                }
            } catch (e: Exception) {
                _menuState.value = MenuUiState.Error(e.message ?: "Failed to load menu")
            }
        }
    }
}
