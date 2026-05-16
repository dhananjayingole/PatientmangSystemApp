package eu.tutorials.patientmanagementapp.Model

import com.google.gson.annotations.SerializedName

data class Category(

    @SerializedName("id")
    val id: Int,

    @SerializedName("category_name")
    val name: String,

    @SerializedName("category_description")
    val description: String?,

    @SerializedName("poses")
    val poses: List<Pose>?
)

data class Pose(

    @SerializedName("id")
    val id: Int,

    @SerializedName("english_name")
    val englishName: String?,

    @SerializedName("sanskrit_name")
    val sanskritName: String?,

    @SerializedName("translation_name")
    val translationName: String?,

    @SerializedName("pose_description")
    val poseDescription: String?,

    @SerializedName("pose_benefits")
    val poseBenefits: String?,

    @SerializedName("url_png")
    val urlPng: String?,

    @SerializedName("url_svg")
    val urlSvg: String?,

    @SerializedName("category_name")
    val categoryName: String?,

    @SerializedName("level")
    val level: String?
)