package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.yigit.notesdemo.databinding.FragmentSavePopupBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SavePopupFragment : Fragment() {
    private var _binding: FragmentSavePopupBinding? = null
    private val binding get() = _binding!!
    private lateinit var notes: Note
    private lateinit var title: String
    private lateinit var text: String
    private var edit: Int = 0
    private var noteId: Int = 0
    private var priority: Int = 0

    private lateinit var noteDAO: NoteDAO
    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDAO = App.noteDB.NoteDAO() // Singleton’dan al
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavePopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            title = SavePopupFragmentArgs.fromBundle(it).title
            text = SavePopupFragmentArgs.fromBundle(it).text
            edit = SavePopupFragmentArgs.fromBundle(it).edit
            noteId = SavePopupFragmentArgs.fromBundle(it).id
            priority = SavePopupFragmentArgs.fromBundle(it).priority
            notes = Note(title, text, priority).apply {
                if (edit == 1) id = noteId
            }
        }

        binding.saveButtonTrue.setOnClickListener { saveTrue(it) }
        binding.saveButtonFalse.setOnClickListener { saveFalse(it) }
    }

    private fun saveTrue(view: View) {
        if (edit == 1) {
            mDisposable.add(
                noteDAO.update(notes)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForUpdate)
            )
        } else {
            mDisposable.add(
                noteDAO.insert(notes)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
    }

    private fun saveFalse(view: View) {
        val action = SavePopupFragmentDirections.actionSavePopupFragmentToAddNoteFragment(
            edit, noteId, title, text, priority = priority
        )
        Navigation.findNavController(view).navigate(action)
    }

    private fun handleResponseForInsert() {
        val action = SavePopupFragmentDirections.actionSavePopupFragmentToHomeFragment2()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun handleResponseForUpdate() {
        val action = SavePopupFragmentDirections.actionSavePopupFragmentToHomeFragment2()
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
        _binding = null
    }
}