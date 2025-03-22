package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.yigit.notesdemo.adapter.NoteAdapter
import com.yigit.notesdemo.databinding.FragmentHomeBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteDAO: NoteDAO
    private lateinit var noteAdapter: NoteAdapter

    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDAO = App.noteDB.NoteDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddHome.setOnClickListener { newAdd(it) }
        binding.buttonInfoHome.setOnClickListener { info(it) }

        binding.RecyclerViewNotes.layoutManager = LinearLayoutManager(requireContext())
        noteAdapter = NoteAdapter(requireContext(), mutableListOf())
        binding.RecyclerViewNotes.adapter = noteAdapter

        getData()
    }

    private fun getData() {
        mDisposable.add(
            noteDAO.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(notes: List<Note>) {
        noteAdapter.updateNotes(notes.toMutableList())
    }

    private fun info(view: View) {
        val action = HomeFragmentDirections.actionHomeFragmentToInfoPopupFragment()
        Navigation.findNavController(view).navigate(action)
    }

    private fun newAdd(view: View) {
        val noteArguments = NoteArguments(
            edit = 0,
            id = null,
            title = null,
            text = null,
            priority = 0
        )
        val action = HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(noteArguments)
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mDisposable.clear()
    }
}