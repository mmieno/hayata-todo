package com.example.hayatatodolist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.hayatatodolist.R
import com.example.hayatatodolist.model.Todo

interface TablerowListener {
    fun yoteiChanged(index: Int, isChecked: Boolean)
    fun jissekiChanged(index: Int, isChecked: Boolean)
    fun kakuninChanged(index: Int, isChecked: Boolean)
    fun yoteiHeaderClicked(index: Int)
    fun jissekiHeaderClicked(index: Int)
    fun kakuninHeaderClicked(index: Int)
}

class TableRowAdapter(private var todoList: ArrayList<Todo>, private val listener: TablerowListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(i: Int) : Int{
        return if (todoList[i].no == 1) 0 else 1
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View
        return if (viewType == 0) {
            v = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.table_header_layout, viewGroup, false)
            HeaderViewHolder(v)
        } else {
            v = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.table_row_layout, viewGroup, false)
            RowViewHolder(v)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        //val holder
        val holder: RowViewHolder
        if (todoList[i].no == 1) {
            holder = viewHolder as HeaderViewHolder
            holder.tvCaption.text = "■" + todoList[i].listTitle
            holder.yoteiHeader.setOnClickListener{
                listener.yoteiHeaderClicked(
                    holder.index,
                )
            }
            holder.jissekiHeader.setOnClickListener{
                listener.jissekiHeaderClicked(
                    holder.index,
                )
            }
            holder.kakuninHeader.setOnClickListener{
                listener.kakuninHeaderClicked(
                    holder.index,
                )
            }
        } else {
            holder = viewHolder as RowViewHolder
        }

        holder.index = i
        holder.listId = todoList[i].listId
        holder.tvNo.text = todoList[i].no.toString()
        holder.tvTitle.text = todoList[i].title
        holder.tvYotei.isChecked = todoList[i].yotei
        holder.tvJisseki.isChecked = todoList[i].jisseki
        holder.tvKakunin.isChecked = todoList[i].kakunin

        holder.tvYotei.setOnCheckedChangeListener { buttonView, isChecked -> //set your object's last status
            listener.yoteiChanged(
                holder.index,
                isChecked
            )
        }

        holder.tvJisseki.setOnCheckedChangeListener { buttonView, isChecked -> //set your object's last status
            listener.jissekiChanged(
                holder.index,
                isChecked
            )
        }

        holder.tvKakunin.setOnCheckedChangeListener { buttonView, isChecked -> //set your object's last status
            listener.kakuninChanged(
                holder.index,
                isChecked
            )
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    open class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var index: Int = 0
        var listId: Int = 0
        val tvNo: TextView = itemView.findViewById(R.id.tv_no)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvYotei: CheckBox = itemView.findViewById(R.id.tv_yotei)
        val tvJisseki: CheckBox = itemView.findViewById(R.id.tv_jisseki)
        val tvKakunin: CheckBox = itemView.findViewById(R.id.tv_kakunin)
    }

    class HeaderViewHolder(itemView: View) : RowViewHolder(itemView) {
        val tvCaption: TextView = itemView.findViewById(R.id.caption)
        val yoteiHeader: TextView = itemView.findViewById(R.id.yotei_header)
        val jissekiHeader: TextView = itemView.findViewById(R.id.jisseki_header)
        val kakuninHeader: TextView = itemView.findViewById(R.id.kakunin_header)
    }
}