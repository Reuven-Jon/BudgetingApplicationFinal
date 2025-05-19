package com.sample.budgetingapplicationfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sample.budgetingapplicationfinal.databinding.ItemBoardCellBinding

/**
 * Adapter to bind BoardCell data into the RecyclerView grid.
 *
 * Supports a movable "player marker" via [currentPosition].
 */
@Suppress("unused") // used by BoardFragment
class BoardAdapter(
    private val cells: List<BoardCell>
) : RecyclerView.Adapter<BoardAdapter.CellViewHolder>() {

    /**
     * Index where the avatar should appear.
     * Setting this will refresh both the old and new positions.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var currentPosition: Int = -1
        set(value) {
            val old = field
            field = value.coerceIn(-1, cells.lastIndex)
            if (old in cells.indices) notifyItemChanged(old)
            if (field in cells.indices) notifyItemChanged(field)
        }

    inner class CellViewHolder(val binding: ItemBoardCellBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CellViewHolder(
            ItemBoardCellBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val cell = cells[position]

        // 1) pick the color resource based on filled state
        val colorRes = if (cell.isFilled)
            R.color.boardCellFilled
        else
            R.color.boardCellDefault

        // 2) paint the background
        holder.binding.vCellBackground.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )

        // 3) hide the default label text (we’re using the ring‐logic instead)
        holder.binding.tvCellIndex.visibility = View.GONE

        // 4) show the avatar only on currentPosition
        holder.binding.ivPlayerMarker.visibility =
            if (position == currentPosition) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = cells.size
}
