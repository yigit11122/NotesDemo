package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.yigit.notesdemo.adapter.NoteAdapter
import com.yigit.notesdemo.databinding.FragmentHomeBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.NoteDAO
import com.yigit.notesdemo.roomdb.NoteDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteDB: NoteDB
    private lateinit var noteDAO: NoteDAO
    private lateinit var noteAdapter: NoteAdapter // Adapter'ı sınıf düzeyinde tut

    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDB = Room.databaseBuilder(requireContext(), NoteDB::class.java, "Note").build()
        noteDAO = noteDB.NoteDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddHome.setOnClickListener { newAdd(it) }
        binding.buttonInfoHome.setOnClickListener { info(it) }

        // RecyclerView'ı başlat
        binding.RecyclerViewNotes.layoutManager = LinearLayoutManager(requireContext())
        noteAdapter = NoteAdapter(requireContext(), mutableListOf()) // Boş liste ile başlat
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
        noteAdapter.updateNotes(notes.toMutableList()) // Adapter'daki listeyi güncelle
    }

    fun info(view: View) {
        val action = HomeFragmentDirections.actionHomeFragmentToInfoPopupFragment()
        Navigation.findNavController(view).navigate(action)
    }

    fun newAdd(view: View) {
        val action = HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(0, 0, "", "")
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mDisposable.clear()
    }
}