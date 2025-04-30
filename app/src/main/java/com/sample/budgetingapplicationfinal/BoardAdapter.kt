package com.sample.budgetingapplicationfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sample.budgetingapplicationfinal.databinding.ItemBoardCellBinding

/**
 * Adapter to bind BoardCell data into the RecyclerView grid.
 */
class BoardAdapter(
    private val cells: List<BoardCell>
) : RecyclerView.Adapter<BoardAdapter.CellViewHolder>() {

    inner class CellViewHolder(val binding: ItemBoardCellBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val binding = ItemBoardCellBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CellViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val cell = cells[position]

        // 1) tint the background
        val bgColorRes = if (cell.isFilled)
            R.color.boardCellFilled else R.color.boardCellDefault
        holder.binding.vCellBackground.setBackgroundTintList(
            ContextCompat.getColorStateList(holder.itemView.context, bgColorRes)
        )

        // 2) hide the index text entirely
        holder.binding.tvCellIndex.visibility = View.GONE
    }

    override fun getItemCount(): Int = cells.size
}
