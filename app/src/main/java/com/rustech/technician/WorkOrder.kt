package com.rustech.technician

import com.google.firebase.Timestamp

/**
 * One install or repair request assigned to the logged-in technician.
 * "type"/"collection" tell us which Firestore collection ("installRequests"
 * or "repairRequests") this came from, since the admin dashboard keeps them
 * separate but this app shows both together.
 */
data class WorkOrder(
    val id: String = "",
    val collectionName: String = "",   // "installRequests" or "repairRequests"
    val type: String = "",             // "install" or "repair" — for display only
    val name: String = "",             // install requests
    val contact: String = "",          // install requests
    val address: String = "",          // install requests
    val notes: String = "",            // install requests
    val email: String = "",            // repair requests
    val issue: String = "",            // repair requests
    val status: String = "",           // pending | scheduled | delayed | completed | cancelled
    val scheduledDate: Timestamp? = null,
    val assignedTo: String = "",
    val delayReason: String = ""
) {
    /** What to show as the customer/contact name on the card. */
    fun displayName(): String = if (type == "install") name.ifBlank { contact } else email

    /** Address for installs, issue description for repairs. */
    fun displayDetail(): String = if (type == "install") address.ifBlank { notes } else issue

    /** Contact info line shown under the detail. */
    fun displayContact(): String = if (type == "install") contact else email
}
