package eu.tutorials.patientmanagementapp.Model

data class YogaItem(
    val category_description: String,
    val category_name: String,
    val id: Int,
    val poses: List<Pose>
)