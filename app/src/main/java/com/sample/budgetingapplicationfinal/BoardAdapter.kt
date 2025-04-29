package com.sample.budgetingapplicationfinal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sample.budgetingapplicationfinal.databinding.ItemBoardCellBinding
import java.util.Locale


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

    // Bind each cell’s appearance based on whether it’s filled or empty
    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val cell = cells[position]
        // Determine tint color for the cell background
        val backgroundColor = if (cell.isFilled)
            R.color.boardCellFilled else R.color.boardCellDefault
        holder.binding.vCellBackground.setBackgroundTintList(
            ContextCompat.getColorStateList(holder.itemView.context, backgroundColor)
        )
        // Determine text color: dark on empty, neutral on filled
        val textColor = if (cell.isFilled)
            R.color.backgroundColor else R.color.buttonTextColor
        holder.binding.tvCellIndex.setTextColor(
            ContextCompat.getColor(holder.itemView.context, textColor)
        )
        // Display the 1-based index
        val indexText = holder.itemView.context
            .getString(R.string.cell_index, position + 1)
        holder.binding.tvCellIndex.text = indexText


    }

    override fun getItemCount(): Int = cells.size
}