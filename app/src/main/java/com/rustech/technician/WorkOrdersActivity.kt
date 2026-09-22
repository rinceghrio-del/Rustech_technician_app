package com.rustech.technician

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class WorkOrdersActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: WorkOrderAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: TextView

    private var installListener: ListenerRegistration? = null
    private var repairListener: ListenerRegistration? = null
    private var latestInstalls: List<WorkOrder> = emptyList()
    private var latestRepairs: List<WorkOrder> = emptyList()

    // Only these statuses are "active" work for a technician — anything
    // completed/cancelled just disappears from their list automatically.
    private val activeStatuses = setOf("scheduled", "delayed")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_orders)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToLogin()
            return
        }

        findViewById<TextView>(R.id.techEmailLabel).text = currentUser.email ?: ""

        val list = findViewById<RecyclerView>(R.id.workOrderList)
        emptyState = findViewById(R.id.emptyState)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        adapter = WorkOrderAdapter(
            onComplete = { item -> confirmComplete(item) },
            onDelay = { item -> promptDelayReason(item) },
            onCancel = { item -> confirmCancel(item) }
        )
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        swipeRefresh.setOnRefreshListener { swipeRefresh.isRefreshing = false } // live listeners already keep it fresh

        findViewById<View>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            goToLogin()
        }

        subscribeToWorkOrders(currentUser.email ?: "")
    }

    private fun subscribeToWorkOrders(email: String) {
        installListener = db.collection("installRequests")
            .whereEqualTo("assignedTo", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load installs: " + error.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                latestInstalls = snapshot?.documents?.map { doc -> docToWorkOrder(doc.id, doc.data ?: emptyMap(), "install", "installRequests") } ?: emptyList()
                renderList()
            }

        repairListener = db.collection("repairRequests")
            .whereEqualTo("assignedTo", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load repairs: " + error.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                latestRepairs = snapshot?.documents?.map { doc -> docToWorkOrder(doc.id, doc.data ?: emptyMap(), "repair", "repairRequests") } ?: emptyList()
                renderList()
            }
    }

    private fun docToWorkOrder(id: String, data: Map<String, Any?>, type: String, collectionName: String): WorkOrder {
        return WorkOrder(
            id = id,
            collectionName = collectionName,
            type = type,
            name = data["name"] as? String ?: "",
            contact = data["contact"] as? String ?: "",
            address = data["address"] as? String ?: "",
            notes = data["notes"] as? String ?: "",
            email = data["email"] as? String ?: "",
            issue = data["issue"] as? String ?: "",
            status = data["status"] as? String ?: "",
            scheduledDate = data["scheduledDate"] as? com.google.firebase.Timestamp,
            assignedTo = data["assignedTo"] as? String ?: "",
            delayReason = data["delayReason"] as? String ?: ""
        )
    }

    private fun renderList() {
        val combined = (latestInstalls + latestRepairs)
            .filter { it.status in activeStatuses }
            .sortedBy { it.scheduledDate?.toDate()?.time ?: Long.MAX_VALUE }

        adapter.submitList(combined)
        emptyState.visibility = if (combined.isEmpty()) View.VISIBLE else View.GONE
    }

    // ---------- Actions ----------
    private fun confirmComplete(item: WorkOrder) {
        val label = if (item.type == "install") "na-install" else "naayos"
        AlertDialog.Builder(this)
            .setTitle("Kumpirmahin")
            .setMessage("Markahan na bang $label si ${item.displayName()}?")
            .setPositiveButton("Oo, tapos na") { _, _ -> updateStatus(item, "completed", null) }
            .setNegativeButton("Kanselahin", null)
            .show()
    }

    private fun confirmCancel(item: WorkOrder) {
        AlertDialog.Builder(this)
            .setTitle("I-cancel ang request?")
            .setMessage("Sigurado ka bang i-cancel ang trabaho para kay ${item.displayName()}?")
            .setPositiveButton("Oo, i-cancel") { _, _ -> updateStatus(item, "cancelled", null) }
            .setNegativeButton("Huwag muna", null)
            .show()
    }

    private fun promptDelayReason(item: WorkOrder) {
        val input = EditText(this)
        input.hint = "Hal. walang tao sa bahay, umuulan, kulang materyales..."

        AlertDialog.Builder(this)
            .setTitle("Bakit naantala?")
            .setView(input)
            .setPositiveButton("I-save") { _, _ ->
                val reason = input.text.toString().trim()
                updateStatus(item, "delayed", reason)
            }
            .setNegativeButton("Kanselahin", null)
            .show()
    }

    private fun updateStatus(item: WorkOrder, newStatus: String, delayReason: String?) {
        val updates = mutableMapOf<String, Any>("status" to newStatus)
        if (delayReason != null) updates["delayReason"] = delayReason

        db.collection(item.collectionName).document(item.id)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Na-update.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { err ->
                Toast.makeText(this, "Failed: " + err.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun goToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        installListener?.remove()
        repairListener?.remove()
    }
}
