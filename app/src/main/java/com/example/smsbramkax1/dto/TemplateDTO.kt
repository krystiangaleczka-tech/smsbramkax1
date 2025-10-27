package com.example.smsbramkax1.dto

import kotlinx.serialization.Serializable

@Serializable
data class TemplateRequestDTO(
    val name: String,
    val content: String,
    val category: String? = null
)

@Serializable
data class TemplateResponseDTO(
    val id: Long,
    val name: String,
    val content: String,
    val variables: List<String>,
    val category: String?,
    val createdAt: Long,
    val updatedAt: Long?
)

@Serializable
data class RenderTemplateRequestDTO(
    val templateId: Long,
    val variables: Map<String, String>
)

@Serializable
data class RenderTemplateResponseDTO(
    val renderedContent: String,
    val templateId: Long,
    val variables: Map<String, String>
)