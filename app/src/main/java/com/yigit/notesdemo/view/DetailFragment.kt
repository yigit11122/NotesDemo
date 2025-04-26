package com.yigit.notesdemo.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.yigit.notesdemo.databinding.FragmentDetailBinding
import com.yigit.notesdemo.model.Note
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File

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
            noteId = noteData?.id
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
        binding.shareDetail.setOnClickListener { shareButton() }
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

    private fun shareButton() {
        selectedNote?.let { note ->
            val cleanText = note.text.replace(Regex("\\[\\[image:.+?\\]\\]"), "").trim()
            val shareText = "${note.title}\n$cleanText"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Notu Paylaş"))
        }
    }

    private fun handleResponseForFind(note: Note) {
        binding.DetailTitle.text = note.title
        val imageMatch = Regex("\\[\\[image:(.+?)\\]\\]").find(note.text)
        if (imageMatch != null) {
            val imagePath = imageMatch.groupValues[1]
            try {
                var bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap != null) {
                    // EXIF yönlendirmesini düzelt
                    bitmap = fixImageOrientation(imagePath, bitmap)
                    binding.detailImage.setImageBitmap(bitmap)
                    binding.detailCardViewImage.visibility = View.VISIBLE
                } else {
                    binding.detailCardViewImage.visibility = View.GONE
                }
                binding.DetailWrite.text = note.text.replace(imageMatch.value, "").trim()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Resim yüklenemedi",
                    Toast.LENGTH_SHORT
                ).show()
                binding.DetailWrite.text = note.text
                binding.detailCardViewImage.visibility = View.GONE
            }
        } else {
            binding.DetailWrite.text = note.text
            binding.detailCardViewImage.visibility = View.GONE
        }

        binding.textViewPriority.text = when (note.priority) {
            0 -> "Öncelik: Düşük"
            1 -> "Öncelik: Orta"
            2 -> "Öncelik: Yüksek"
            else -> "Öncelik: Belirsiz"
        }
        selectedNote = note
    }

    // Dosya yolundan EXIF yönlendirmesini düzelt
    private fun fixImageOrientation(imagePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(File(imagePath))
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}