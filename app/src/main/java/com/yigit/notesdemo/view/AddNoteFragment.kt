package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.yigit.notesdemo.databinding.FragmentAddNoteBinding
import com.yigit.notesdemo.roomdb.NoteDAO
import com.yigit.notesdemo.roomdb.NoteDB
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AddNoteFragment : Fragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val edit = AddNoteFragmentArgs.fromBundle(it).edit
            val id = AddNoteFragmentArgs.fromBundle(it).id
            val title = AddNoteFragmentArgs.fromBundle(it).title
            val text = AddNoteFragmentArgs.fromBundle(it).text

            binding.editTextTitleAddNote.setText(title)
            binding.editTextWriteAddNote.setText(text)

        }


        binding.saveAddNote.setOnClickListener { saveButton(it) }
        binding.chevronLeftAddNote.setOnClickListener { backButton(it) }
    }

    private fun backButton(view: View) {
        val action = AddNoteFragmentDirections.actionAddNoteFragmentToHomeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun saveButton(view: View) {

        val title = binding.editTextTitleAddNote.text.toString()
        val text = binding.editTextWriteAddNote.text.toString()

        if (title != "" && text != "") {
            val action =
                AddNoteFragmentDirections.actionAddNoteFragmentToSavePopupFragment(title, text)
            Navigation.findNavController(requireView()).navigate(action)
        } else {
            Toast.makeText(
                requireContext(), "Please fill in the Title and Text sections", Toast.LENGTH_LONG
            ).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
    }
}