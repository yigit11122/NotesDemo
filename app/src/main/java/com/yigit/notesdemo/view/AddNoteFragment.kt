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
    private var noteArguments: NoteArguments? = null

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

        arguments?.let { args ->
            noteArguments = AddNoteFragmentArgs.fromBundle(args).noteData
            noteArguments?.let { data ->
                edit = data.edit ?: 0
                data.title?.let { binding.editTextTitleAddNote.setText(it) }
                data.text?.let { binding.editTextWriteAddNote.setText(it) }
                data.priority?.let {
                    selectedPriority = it
                    updatePriorityUI()
                }
            }
        }

        binding.radioGroupPriority.setOnCheckedChangeListener { _, checkedId ->
            selectedPriority = when (checkedId) {
                R.id.radioLow -> 0
                R.id.radioMedium -> 1
                R.id.radioHigh -> 2
                else -> 0
            }
        }

        binding.saveAddNote.setOnClickListener { saveButton(it) }
        binding.chevronLeftAddNote.setOnClickListener { backButton() }
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

        val currentNoteArguments = NoteArguments(
            edit = edit,
            id = noteArguments?.id ?: 0,
            title = title,
            text = text,
            priority = selectedPriority
        )

        if (edit == 1) {
            noteArguments?.let { oldData ->
                val oldTitle = oldData.title ?: ""
                val oldText = oldData.text ?: ""
                val oldPriority = oldData.priority ?: 0

                if (oldTitle == title && oldText == text && oldPriority == selectedPriority) {
                    Toast.makeText(requireContext(), "No changes made!", Toast.LENGTH_SHORT).show()
                } else if (title.isNotEmpty() && text.isNotEmpty()) {
                    val action = AddNoteFragmentDirections.actionAddNoteFragmentToSavePopupFragment(currentNoteArguments)
                    Navigation.findNavController(view).navigate(action)
                }
            }
        } else if (title.isNotEmpty() && text.isNotEmpty()) {
            val action = AddNoteFragmentDirections.actionAddNoteFragmentToSavePopupFragment(currentNoteArguments)
            Navigation.findNavController(view).navigate(action)
        } else {
            Toast.makeText(
                requireContext(),
                "Please fill in the Title and Text sections!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun backButton() {
        val action = if (noteArguments?.id == null || noteArguments?.id == 0) {
            AddNoteFragmentDirections.actionAddNoteFragmentToHomeFragment()
        } else {
            AddNoteFragmentDirections.actionAddNoteFragmentToDetailFragment(noteArguments!!)
        }
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mDisposable.clear()
    }
}