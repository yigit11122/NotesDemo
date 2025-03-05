package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.yigit.notesdemo.R
import com.yigit.notesdemo.databinding.FragmentAddNoteBinding
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AddNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteDAO: NoteDAO
    private var edit: Int = 0
    private var selectedPriority: Int = 0

    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDAO = App.noteDB.NoteDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argümanları al
        arguments?.let {
            edit = AddNoteFragmentArgs.fromBundle(it).edit
            val id = AddNoteFragmentArgs.fromBundle(it).id
            val title = AddNoteFragmentArgs.fromBundle(it).title
            val text = AddNoteFragmentArgs.fromBundle(it).text
            selectedPriority = AddNoteFragmentArgs.fromBundle(it).priority

            binding.editTextTitleAddNote.setText(title)
            binding.editTextWriteAddNote.setText(text)
            updatePriorityUI()
        }

        // Öncelik seçimi
        binding.radioGroupPriority.setOnCheckedChangeListener { _, checkedId ->
            selectedPriority = when (checkedId) {
                R.id.radioLow -> 0
                R.id.radioMedium -> 1
                R.id.radioHigh -> 2
                else -> 0
            }
        }

        binding.saveAddNote.setOnClickListener { saveButton(it) }
        binding.chevronLeftAddNote.setOnClickListener { backButton(it) }
    }

    private fun updatePriorityUI() {
        when (selectedPriority) {
            0 -> binding.radioLow.isChecked = true
            1 -> binding.radioMedium.isChecked = true
            2 -> binding.radioHigh.isChecked = true
        }
    }

    private fun saveButton(view: View) {
        val title = binding.editTextTitleAddNote.text.toString().trim()
        val text = binding.editTextWriteAddNote.text.toString().trim()

        if (edit == 1) {
            val oldTitle = arguments?.getString("title") ?: ""
            val oldText = arguments?.getString("text") ?: ""
            val id = arguments?.getInt("id") ?: 0
            val oldPriority = arguments?.getInt("priority") ?: 0

            if (oldTitle == title && oldText == text && oldPriority == selectedPriority) {
                Toast.makeText(requireContext(), "No changes made!", Toast.LENGTH_SHORT).show()
            } else if (title.isNotEmpty() && text.isNotEmpty()) {
                val action = AddNoteFragmentDirections.actionAddNoteFragmentToSavePopupFragment(
                    title, text, edit, id, priority = selectedPriority
                )
                Navigation.findNavController(view).navigate(action)
            }
        } else if (title.isNotEmpty() && text.isNotEmpty()) {
            val action = AddNoteFragmentDirections.actionAddNoteFragmentToSavePopupFragment(
                title, text, edit, 0, priority = selectedPriority
            )
            Navigation.findNavController(view).navigate(action)
        } else {
            Toast.makeText(
                requireContext(),
                "Please fill in the Title and Text sections!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun backButton(view: View) {
        val action = AddNoteFragmentDirections.actionAddNoteFragmentToHomeFragment()
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mDisposable.clear()
    }
}