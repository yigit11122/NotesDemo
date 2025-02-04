package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.yigit.notesdemo.databinding.FragmentDetailBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.NoteDAO
import com.yigit.notesdemo.roomdb.NoteDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteDB: NoteDB
    private lateinit var noteDAO: NoteDAO

    private val mDisposable = CompositeDisposable()
    private var selectedNote: Note? = null
    private var noteId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        noteDB = Room.databaseBuilder(requireContext(), NoteDB::class.java, "Note").build()
        noteDAO = noteDB.NoteDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            noteId = DetailFragmentArgs.fromBundle(it).id

        }

        noteId?.let { id ->
            mDisposable.add(
                noteDAO.findById(id).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForFind)
            )
        }

        binding.chevronLeftDetail.setOnClickListener { backButton(it) }
        binding.modeDetail.setOnClickListener { modeButton(it) }
        binding.chevronLeftDetail.setOnClickListener { backDetailButton(it) }
    }

    private fun backDetailButton(view: View) {
        val action = DetailFragmentDirections.actionDetailFragmentToHomeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun modeButton(view: View) {
        selectedNote?.let { note ->
            val action = DetailFragmentDirections.actionDetailFragmentToAddNoteFragment2(
                1, note.id, note.title, note.text
            )
            Navigation.findNavController(view).navigate(action)
        }
    }

    private fun backButton(view: View) {
        val action = DetailFragmentDirections.actionDetailFragmentToHomeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun handleResponseForFind(note: Note) {
        binding.DetailTitle.text = note.title
        binding.DetailWrite.text = note.text
        selectedNote = note
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}
