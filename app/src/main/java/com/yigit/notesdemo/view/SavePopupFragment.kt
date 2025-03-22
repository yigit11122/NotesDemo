package com.yigit.notesdemo.view

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.yigit.notesdemo.databinding.FragmentSavePopupBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SavePopupFragment : DialogFragment() {
    private var _binding: FragmentSavePopupBinding? = null
    private val binding get() = _binding!!
    private lateinit var notes: Note
    private lateinit var noteDAO: NoteDAO
    private val mDisposable = CompositeDisposable()
    private var noteArguments: NoteArguments? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            attributes = attributes.apply {
                gravity = Gravity.CENTER
                dimAmount = 0.5f
                flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            }
        }
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDAO = App.noteDB.NoteDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavePopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            noteArguments = SavePopupFragmentArgs.fromBundle(bundle).noteData
            noteArguments?.let { data ->
                notes = Note(
                    title = data.title ?: "", text = data.text ?: "", priority = data.priority ?: 0
                ).apply {
                    if (data.edit == 1) id = data.id ?: 0
                }
            }
        }

        binding.saveButtonTrue.setOnClickListener { saveTrue() }
        binding.saveButtonFalse.setOnClickListener { saveFalse(it) }
    }

    private fun saveTrue() {
        noteArguments?.let { data ->
            if (data.edit == 1) {
                mDisposable.add(noteDAO.update(notes).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForUpdate) { it.printStackTrace() })
            } else {
                mDisposable.add(noteDAO.insert(notes).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert) { it.printStackTrace() })
            }
        }
    }

    private fun handleResponseForInsert() {
        dismiss()
        val action = SavePopupFragmentDirections.actionSavePopupFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun handleResponseForUpdate() {
        dismiss()
        val action = SavePopupFragmentDirections.actionSavePopupFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun saveFalse(view: View) {
        dismiss()
        findNavController().popBackStack()
    }



    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
        _binding = null
    }
}
