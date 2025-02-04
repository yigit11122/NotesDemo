package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.yigit.notesdemo.databinding.FragmentSavePopupBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.NoteDAO
import com.yigit.notesdemo.roomdb.NoteDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SavePopupFragment : Fragment() {

    private var _binding: FragmentSavePopupBinding? = null
    private val binding get() = _binding!!
    private lateinit var notes: Note
    private lateinit var title: String
    private lateinit var text: String

    private lateinit var noteDB: NoteDB
    private lateinit var noteDAO: NoteDAO

    private val mDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        noteDB = Room.databaseBuilder(requireContext(), NoteDB::class.java, "Note").build()
        noteDAO = noteDB.NoteDAO()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavePopupBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            title = SavePopupFragmentArgs.fromBundle(it).title
            text = SavePopupFragmentArgs.fromBundle(it).text
            notes = Note(title, text)
        }
        binding.saveButtonTrue.setOnClickListener { saveTrue(it) }
        binding.saveButtonFalse.setOnClickListener { saveFalse(it) }

    }

    fun saveTrue(view: View) {
        mDisposable.add(
            noteDAO.insert(notes).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponseForInsert)
        )
    }

    fun saveFalse(view: View) {
        val action =
            SavePopupFragmentDirections.actionSavePopupFragmentToAddNoteFragment(0, 0, title, text)
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun handleResponseForInsert() {
        val action = SavePopupFragmentDirections.actionSavePopupFragmentToDetailFragment(0)
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
    }
}