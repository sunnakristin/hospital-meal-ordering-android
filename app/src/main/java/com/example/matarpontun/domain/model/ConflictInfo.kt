package com.example.matarpontun.domain.model

/** Describes a single meal-slot conflict detected when placing an order. */
data class ConflictInfo(
    val slot: String,
    val originalMeal: String,
    val matchedRestriction: String,
    /** The auto-assigned replacement meal name, or null if manual intervention is required. */
    val replacementMeal: String?
)
