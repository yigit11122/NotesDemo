package com.yigit.notesdemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import java.io.File

class NoteAdapter(context: Context, private var noteList: MutableList<Note>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val noteDAO: NoteDAO = App.noteDB.NoteDAO()
    private val mDisposable = CompositeDisposable()
    private val editingStates = HashMap<Int, Boolean>()

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

        val isEditing = editingStates.getOrDefault(position, false)
        if (isEditing) {
            holder.binding.editLayout.visibility = View.VISIBLE
            holder.binding.textViewNoteItem.visibility = View.GONE
            holder.binding.buttonDeleteItem.visibility = View.GONE
            holder.binding.editTextNoteItem.setText(note.title)
        } else {
            holder.binding.editLayout.visibility = View.GONE
            holder.binding.textViewNoteItem.visibility = View.VISIBLE
            holder.binding.buttonDeleteItem.visibility = View.VISIBLE
        }

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
            alert.setTitle("Notu Sil")
            alert.setMessage("Bu notu silmek istediğinizden emin misiniz?")

            alert.setPositiveButton("Evet") { _, _ ->
                val imageMatch = Regex("\\[\\[image:(.+?)\\]\\]").find(note.text)
                if (imageMatch != null) {
                    val imagePath = imageMatch.groupValues[1]
                    try {
                        File(imagePath).delete()
                    } catch (e: Exception) {
                    }
                }
                mDisposable.add(
                    noteDAO.delete(note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            removeNote(position)
                            Toast.makeText(
                                holder.itemView.context, "Not silindi!", Toast.LENGTH_SHORT
                            ).show()
                        }, { it.printStackTrace() })
                )
            }
            alert.setNegativeButton("Hayır") { _, _ ->
                Toast.makeText(
                    holder.itemView.context, "Not silinmedi.", Toast.LENGTH_SHORT
                ).show()
            }
            alert.show()
        }

        holder.itemView.setOnLongClickListener {
            editingStates[position] = true
            notifyItemChanged(position)
            holder.binding.editTextNoteItem.requestFocus()
            val imm =
                holder.itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.binding.editTextNoteItem, InputMethodManager.SHOW_IMPLICIT)
            true
        }
        holder.binding.buttonSaveEdit.setOnClickListener {
            val newTitle = holder.binding.editTextNoteItem.text.toString()
            noteList[position].title = newTitle
            val updatedNote = noteList[position]
            mDisposable.add(
                noteDAO.update(updatedNote).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                        editingStates[position] = false
                        notifyItemChanged(position)
                    }, { it.printStackTrace() })
            )
        }
        holder.binding.buttonCancelEdit.setOnClickListener {
            editingStates[position] = false
            notifyItemChanged(position)
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