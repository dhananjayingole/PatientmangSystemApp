package eu.tutorials.patientmanagementapp.api

import eu.tutorials.patientmanagementapp.Model.Category
import eu.tutorials.patientmanagementapp.Model.Pose
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YogaApi {
    // Get all categories (each category contains poses)
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    // Get all poses directly
    @GET("poses")
    suspend fun getAllPoses(): Response<List<Pose>>

    // Get pose by ID
    @GET("poses")
    suspend fun getPoseById(@Query("id") id: Int): Response<List<Pose>>
}