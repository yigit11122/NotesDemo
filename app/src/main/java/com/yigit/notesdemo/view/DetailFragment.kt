package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.yigit.notesdemo.databinding.FragmentDetailBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteDAO: NoteDAO
    private val mDisposable = CompositeDisposable()
    private var selectedNote: Note? = null
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDAO = App.noteDB.NoteDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val noteData = DetailFragmentArgs.fromBundle(bundle).noteData
            noteId = noteData?.id // NoteData'dan id'yi alıyoruz
            noteId?.let { id ->
                mDisposable.add(
                    noteDAO.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponseForFind)
                )
            }
        }

        binding.chevronLeftDetail.setOnClickListener { backButton() }
        binding.modeDetail.setOnClickListener { modeButton(it) }
    }

    private fun backButton() {
        val action = DetailFragmentDirections.actionDetailFragmentToHomeFragment()
        Navigation.findNavController(binding.root).navigate(action)
    }

    private fun modeButton(view: View) {
        selectedNote?.let { note ->
            val noteArguments = NoteArguments(
                edit = 1,
                id = note.id,
                title = note.title,
                text = note.text,
                priority = note.priority
            )
            val action = DetailFragmentDirections.actionDetailFragmentToAddNoteFragment(noteArguments)
            Navigation.findNavController(view).navigate(action)
        }
    }

    private fun handleResponseForFind(note: Note) {
        binding.DetailTitle.text = note.title
        binding.DetailWrite.text = note.text
        binding.textViewPriority.text = when (note.priority) {
            0 -> "Priority: Low"
            1 -> "Priority: Medium"
            2 -> "Priority: High"
            else -> "Priority: Uncertain"
        }
        selectedNote = note
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}