package com.sia.credigo.network

import com.sia.credigo.model.Mail
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*

interface MailApi {
    @POST("mails/createMail")
    suspend fun createMail(@Body body: Mail): Response<BaseResponse<Mail>>

    @GET("mails/getAllMail")
    suspend fun getAllMails(): Response<BaseResponse<List<Mail>>>

    @GET("mails/getMailById/{id}")
    suspend fun getMailById(@Path("id") id: Long): Response<BaseResponse<Mail>>

    @GET("mails/getUserMails/{userId}")
    suspend fun getUserMails(@Path("userId") userId: Int): Response<BaseResponse<List<Mail>>>

    @PUT("mails/updateMail/{id}")
    suspend fun updateMail(@Path("id") id: Long, @Body body: Mail): Response<BaseResponse<Mail>>

    @DELETE("mails/deleteMail/{id}")
    suspend fun deleteMail(@Path("id") id: Long): Response<BaseResponse<Void>>

    @GET("mails/getUserMails/me")
    suspend fun getCurrentUserMails(): Response<BaseResponse<List<Mail>>>
}
