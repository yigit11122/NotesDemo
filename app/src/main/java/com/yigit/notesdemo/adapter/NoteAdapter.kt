package com.yigit.notesdemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.yigit.notesdemo.R
import com.yigit.notesdemo.databinding.ItemHomeNoteBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import com.yigit.notesdemo.view.HomeFragmentDirections
import com.yigit.notesdemo.view.NoteArguments
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class NoteAdapter(context: Context, private var noteList: MutableList<Note>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val noteDAO: NoteDAO = App.noteDB.NoteDAO()
    private val mDisposable = CompositeDisposable()

    class NoteViewHolder(val binding: ItemHomeNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            ItemHomeNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun getItemCount(): Int = noteList.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.binding.textViewNoteItem.text = note.title

        when (note.priority) {
            0 -> holder.binding.textViewNoteItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_priority_low, 0, 0, 0
            )

            1 -> holder.binding.textViewNoteItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_priority_medium, 0, 0, 0
            )

            2 -> holder.binding.textViewNoteItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_priority_high, 0, 0, 0
            )
        }

        holder.itemView.setOnClickListener {
            val noteArguments = NoteArguments(
                edit = 0,
                id = note.id,
                title = note.title,
                text = note.text,
                priority = note.priority
            )
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(noteArguments)
            Navigation.findNavController(it).navigate(action)
        }

        holder.binding.buttonDeleteItem.setOnClickListener {
            val alert = AlertDialog.Builder(holder.itemView.context)
            alert.setTitle("delete note")
            alert.setMessage("Are you sure you want to delete this note?")

            alert.setPositiveButton("Yes") { dialog, which ->

                mDisposable.add(
                    noteDAO.delete(note).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ removeNote(position) }, { it.printStackTrace() })
                )
                Toast.makeText(
                    holder.itemView.context, "Your note has been deleted", Toast.LENGTH_SHORT
                ).show()


            }
            alert.setNegativeButton("No") { dialog, which ->
                Toast.makeText(
                    holder.itemView.context, "Your note has not been deleted", Toast.LENGTH_SHORT
                ).show()

            }
            alert.show()
        }
    }

    private fun removeNote(position: Int) {
        noteList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, noteList.size)
    }

    fun updateNotes(newNotes: MutableList<Note>) {
        noteList.clear()
        noteList.addAll(newNotes)
        notifyDataSetChanged()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mDisposable.clear()
    }
}