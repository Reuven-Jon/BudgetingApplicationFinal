package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sample.budgetingapplicationfinal.databinding.ActivityExpenseBinding
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class ExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseBinding
    private val auth by lazy { Firebase.auth }
    private val db: DatabaseReference by lazy {
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("expenses")
    }

    // temporarily hold the captured image
    private var capturedBitmap: Bitmap? = null

    // storage folder: /users/{uid}/expenses/
    private val storageRef by lazy {
        Firebase.storage.reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("expenses")
    }

    // camera intent launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            capturedBitmap = result.data?.extras?.get("data") as? Bitmap
            expenseDialog
                ?.findViewById<ImageView>(R.id.capturedImagePreview)
                ?.apply {
                    setImageBitmap(capturedBitmap)
                    visibility = View.VISIBLE
                }
        }
    }

    private var adapter: FirebaseRecyclerAdapter<Expense, ExpenseViewHolder>? = null
    private var expenseDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Auth guard
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // 2) Hamburger menu (unchanged)
        binding.menuButton.setOnClickListener { view ->
            PopupMenu(this, view).apply {
                menuInflater.inflate(R.menu.menu_expense, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.nav_income -> {
                            startActivity(Intent(this@ExpenseActivity, IncomeActivity::class.java))
                            true
                        }
                        R.id.nav_bar_chart -> {
                            startActivity(Intent(this@ExpenseActivity, BarChartActivity::class.java))
                            true
                        }
                        R.id.nav_board_game -> {
                            startActivity(
                                Intent(this@ExpenseActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "board")
                            )
                            true
                        }
                        R.id.nav_login -> {
                            startActivity(
                                Intent(this@ExpenseActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "login")
                            )
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }

        // 3) RecyclerView + FirebaseUI
        setupRecyclerView()

        // 4) Swipe-to-refresh
        binding.swipeContainerExpense.setOnRefreshListener {
            adapter?.notifyDataSetChanged()
            binding.swipeContainerExpense.isRefreshing = false
        }

        // 5) Sum total
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                snapshot.children.forEach {
                    it.getValue(Expense::class.java)?.let { exp ->
                        total += exp.amount
                    }
                }
                binding.totalExpenseAmount.text = String.format("R%,.2f", total)
                binding.emptyView.visibility = if (total == 0.0) View.VISIBLE else View.GONE
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 6) FAB → add dialog
        binding.addExpenseFab.setOnClickListener { showExpensePopup() }
    }

    private fun setupRecyclerView() {
        val options = FirebaseRecyclerOptions.Builder<Expense>()
            .setQuery(db, Expense::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Expense, ExpenseViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ExpenseViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_expense_card, parent, false)
                )

            override fun onBindViewHolder(
                holder: ExpenseViewHolder,
                position: Int,
                model: Expense
            ) {
                holder.tvCat.text    = model.category
                holder.tvAmt.text    = String.format("R%,.2f", model.amount)
                holder.tvDate.text   = model.date
                holder.tvPeriod.text = model.period
                holder.tvSource.text = model.source

                // load photo if URL present
                if (!model.photoUrl.isNullOrBlank()) {
                    holder.photoView.visibility = View.VISIBLE
                    Glide.with(holder.itemView)
                        .load(model.photoUrl)
                        .into(holder.photoView)
                } else {
                    holder.photoView.visibility = View.GONE
                }

                holder.btnInfo.setOnClickListener {
                    AlertDialog.Builder(this@ExpenseActivity)
                        .setTitle("Expense Details")
                        .setMessage("""
                            Category: ${model.category}
                            Amount:   R${model.amount}
                            Date:     ${model.date}
                            Period:   ${model.period}
                            Source:   ${model.source}
                        """.trimIndent())
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }

        binding.expenseRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpenseActivity)
            adapter = this@ExpenseActivity.adapter
        }
        adapter?.startListening()
    }

    private fun showExpensePopup() {
        val popup = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_expense, null, false)

        expenseDialog = AlertDialog.Builder(this)
            .setView(popup)
            .create()

        val etCat      = popup.findViewById<EditText>(R.id.expenseCategoryInput)
        val etAmt      = popup.findViewById<EditText>(R.id.expenseAmountInput)
        val spinner    = popup.findViewById<Spinner>(R.id.spinnerPeriod)
        val etSource   = popup.findViewById<EditText>(R.id.expenseSourceInput)
        val btnCapture = popup.findViewById<Button>(R.id.capturePhotoButton)
        val btnSave    = popup.findViewById<Button>(R.id.submitExpenseButton)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        )

        btnCapture.setOnClickListener {
            cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        btnSave.setOnClickListener {
            val cat    = etCat.text.toString().trim()
            val amt    = etAmt.text.toString().toDoubleOrNull()
            val period = spinner.selectedItem as String
            val src    = etSource.text.toString().trim()

            if (cat.isEmpty())             { etCat.error = "Required"; return@setOnClickListener }
            if (amt == null || amt <= 0.0) { etAmt.error = "Positive amount"; return@setOnClickListener }
            if (src.isEmpty())             { etSource.error = "Required"; return@setOnClickListener }

            // if we have an image, upload it first
            capturedBitmap?.let { bmp ->
                val baos = ByteArrayOutputStream().apply {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
                }
                val data    = baos.toByteArray()
                val fileRef = storageRef.child("${System.currentTimeMillis()}.jpg")

                fileRef.putBytes(data)
                    .addOnSuccessListener {
                        fileRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                saveExpense(cat, amt, period, src, uri.toString())
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Couldn't fetch URL", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                    }

                return@setOnClickListener
            }

            // no photo => just save
            saveExpense(cat, amt, period, src, null)
        }

        expenseDialog?.window?.setBackgroundDrawable(ColorDrawable(0))
        expenseDialog?.show()
    }

    private fun saveExpense(
        category: String,
        amount: Double,
        period: String,
        source: String,
        photoUrl: String?
    ) {
        val entry = Expense(
            category = category,
            amount   = amount,
            date     = LocalDate.now().toString(),
            period   = period,
            source   = source,
            photoUrl = photoUrl
        )

        db.push()
            .setValue(entry)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                expenseDialog?.dismiss()
                capturedBitmap = null
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
            }
    }

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCat     = view.findViewById<TextView>(R.id.expenseCategory)
        val tvAmt     = view.findViewById<TextView>(R.id.expenseAmount)
        val tvDate    = view.findViewById<TextView>(R.id.expenseDate)
        val tvPeriod  = view.findViewById<TextView>(R.id.expensePeriod)
        val tvSource  = view.findViewById<TextView>(R.id.expenseSource)
        val photoView = view.findViewById<ImageView>(R.id.expensePhoto)
        val btnInfo   = view.findViewById<ImageButton>(R.id.expenseInfoButton)
    }
}
