package com.yigit.notesdemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.yigit.notesdemo.databinding.ItemHomeNoteBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.NoteDAO
import com.yigit.notesdemo.roomdb.NoteDB
import com.yigit.notesdemo.view.HomeFragmentDirections
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class NoteAdapter(context: Context, private var noteList: MutableList<Note>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val noteDB: NoteDB = Room.databaseBuilder(context, NoteDB::class.java, "Note").build()
    private val noteDAO: NoteDAO = noteDB.NoteDAO()
    private val mDisposable = CompositeDisposable()

    class NoteViewHolder(val binding: ItemHomeNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemHomeNoteBinding =
            ItemHomeNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(itemHomeNoteBinding)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.binding.textViewNoteItem.text = note.title

        holder.itemView.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(id = note.id)
            Navigation.findNavController(it).navigate(action)
        }

        holder.binding.buttonDeleteItem.setOnClickListener {
            mDisposable.add(
                noteDAO.delete(note)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        removeNote(position)
                    }, {
                        it.printStackTrace()
                    })
            )
        }
    }

    private fun removeNote(position: Int) {
        noteList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, noteList.size)
    }
}
