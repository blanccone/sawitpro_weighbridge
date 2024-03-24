package com.technicaltest.core.service.util

import com.technicaltest.core.service.util.NetworkUtils.handleException
import kotlinx.coroutines.flow.flow

abstract class DatabaseBoundSource<LOCAL_RESPONSE_TYPE, MAPPED_RESPONSE_TYPE>(
    private val queryType: String
) {

    fun asFlow() = flow<Resource<MAPPED_RESPONSE_TYPE>> {

        // Emit Loading State
        emit(Resource.Loading())

        try {
            val localResponse = fetchFromLocal()
            val errorMessage = getErrorMessage()
            when {
                shouldEmitSuccess(localResponse) -> emit(Resource.Success(postProcess(localResponse)))
                errorMessage != null -> emit(Resource.Error(message = errorMessage))
                else -> emit(Resource.Error(message = "Gagal melakukan query data"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(message = e.handleException()))
        }
    }

    private fun shouldEmitSuccess(localResponse: LOCAL_RESPONSE_TYPE?): Boolean {
        return when (queryType) {
            QUERY_SELECT_MULTIPLE, QUERY_INSERT_MULTIPLE -> localResponse is Collection<*> && localResponse.isNotEmpty()
            QUERY_SELECT_SINGLE -> localResponse != null
            QUERY_INSERT_SINGLE -> localResponse is Long && localResponse >= 0
            else -> localResponse is Int && localResponse > 0
        }
    }

    private fun getErrorMessage(): String? {
        return when (queryType) {
            QUERY_SELECT_SINGLE, QUERY_SELECT_MULTIPLE -> "Gagal menampilkan data"
            QUERY_INSERT_SINGLE, QUERY_INSERT_MULTIPLE -> "Gagal menyimpan data"
            QUERY_UPDATE -> "Gagal memperbarui data"
            QUERY_DELETE -> "Gagal menghapus data atau tidak ada data yang dihapus"
            else -> null
        }
    }

    protected abstract suspend fun fetchFromLocal(): LOCAL_RESPONSE_TYPE

    protected abstract suspend fun postProcess(originalData: LOCAL_RESPONSE_TYPE): MAPPED_RESPONSE_TYPE

    companion object {
        const val QUERY_INSERT_MULTIPLE = "insert_multiple"
        const val QUERY_INSERT_SINGLE = "insert"
        const val QUERY_UPDATE = "update"
        const val QUERY_DELETE = "delete"
        const val QUERY_SELECT_SINGLE = "select_single"
        const val QUERY_SELECT_MULTIPLE = "select_multiple"
    }
}