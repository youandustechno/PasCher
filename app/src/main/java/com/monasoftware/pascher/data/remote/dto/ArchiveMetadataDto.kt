package com.monasoftware.pascher.data.remote.dto

data class ArchiveMetadataDto(
    val files: List<ArchiveFileDto>,
    val metadata: ArchiveItemMetadata?
)
data class ArchiveFileDto(val name: String, val format: String?, val source: String?)
data class ArchiveItemMetadata(val licenseurl: String?)