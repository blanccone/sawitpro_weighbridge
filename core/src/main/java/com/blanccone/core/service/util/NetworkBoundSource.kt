package com.technicaltest.core.service.util

import android.util.Log
import androidx.annotation.WorkerThread
import com.technicaltest.core.service.util.NetworkUtils.codeException
import com.technicaltest.core.service.util.NetworkUtils.handleException
import kotlinx.coroutines.flow.flow
import retrofit2.Response

abstract class NetworkBoundSource<API_RESPONSE> {

    /**
     * Convert DataSource from the remote end point as flow.
     */
    fun asFlow() = flow<Resource<API_RESPONSE>> {
        try {
            emit(Resource.Loading())
            val response = fetchFromRemote()

            if(response.isSuccessful){
                val data = response.body()
                emit(Resource.Success(data))
            }else{
                Log.e("ERROR", response.message())
                emit(
                    Resource.Error(
                    message = response.message(),
                    code = response.code()
                ))
            }
        }catch (e: Exception){
            Log.e("ERROR", e.message.toString())
            emit(
                Resource.Error(
                    message = e.handleException(),
                    code = e.codeException(),
                ))
        }
    }

    /**
     * Fetches [Response] from the remote end point.
     */
    @WorkerThread
    protected abstract suspend fun fetchFromRemote(): Response<API_RESPONSE>
}