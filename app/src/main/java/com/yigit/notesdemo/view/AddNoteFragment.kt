package com.yigit.notesdemo.view

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.yigit.notesdemo.R
import com.yigit.notesdemo.databinding.FragmentAddNoteBinding
import com.yigit.notesdemo.roomdb.App
import com.yigit.notesdemo.roomdb.NoteDAO
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.io.File
import java.io.FileOutputStream
import androidx.exifinterface.media.ExifInterface

class AddNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteDAO: NoteDAO
    private var edit: Int = 0
    private var selectedPriority: Int = 0
    private var noteArguments: NoteArguments? = null

    private val mDisposable = CompositeDisposable()

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null
    private var originalImagePath: String? = null
    private var isNewImageSelected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
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
                data.text?.let { text ->
                    val imageMatch = Regex("\\[\\[image:(.+?)\\]\\]").find(text)
                    if (imageMatch != null) {
                        val imagePath = imageMatch.groupValues[1]
                        try {
                            val bitmap = BitmapFactory.decodeFile(imagePath)
                            if (bitmap != null) {
                                // EXIF yönlendirmesini düzelt
                                val correctedBitmap = fixImageOrientation(imagePath, bitmap)
                                binding.imageViewAddNote.setImageBitmap(correctedBitmap)
                                binding.cardViewImageAddNote.visibility = View.VISIBLE
                                selectedBitmap = correctedBitmap
                                selectedImage = Uri.parse("file://$imagePath")
                                originalImagePath = imagePath
                            }
                            binding.editTextWriteAddNote.setText(
                                text.replace(imageMatch.value, "").trim()
                            )
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Resim yüklenemedi",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.editTextWriteAddNote.setText(text)
                        }
                    } else {
                        binding.editTextWriteAddNote.setText(text)
                    }
                }
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
        binding.galleryAddNote.setOnClickListener {
            isNewImageSelected = true
            imageSelect(it)
        }
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
            noteArguments?.let { oldData ->
                val oldTitle = oldData.title ?: ""
                val oldPriority = oldData.priority ?: 0
                val oldTextClean = oldData.text?.replace(Regex("\\[\\[image:.+?\\]\\]"), "")?.trim() ?: ""
                val newTextClean = text
                val oldImageMatch = oldData.text?.let { Regex("\\[\\[image:(.+?)\\]\\]").find(it) }
                val oldImagePath = oldImageMatch?.groupValues?.get(1)
                val isImageUnchanged = if (oldImagePath != null && selectedImage != null && !isNewImageSelected) {
                    oldImagePath == originalImagePath
                } else {
                    oldImagePath == null && selectedImage == null
                }

                if (oldTitle == title && oldTextClean == newTextClean && oldPriority == selectedPriority && isImageUnchanged) {
                    Toast.makeText(requireContext(), "Değişiklik yapılmadı!", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        var imagePath: String? = null
        if (selectedImage != null && (originalImagePath == null || isNewImageSelected)) {
            try {
                val file = File(requireContext().filesDir, "image_${System.currentTimeMillis()}.png")
                requireActivity().contentResolver.openInputStream(selectedImage!!)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                imagePath = file.absolutePath
                if (isNewImageSelected) {
                    originalImagePath = imagePath
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Resim kaydedilemedi", Toast.LENGTH_SHORT).show()
            }
        } else if (selectedImage != null) {
            imagePath = originalImagePath
        }

        val finalText = if (imagePath != null) {
            if (text.isNotEmpty()) "$text\n[[image:$imagePath]]" else "[[image:$imagePath]]"
        } else {
            text
        }

        val currentNoteArguments = NoteArguments(
            edit = edit,
            id = noteArguments?.id ?: 0,
            title = title,
            text = finalText,
            priority = selectedPriority
        )

        if (title.isNotEmpty() && finalText.isNotEmpty()) {
            val action = AddNoteFragmentDirections.actionAddNoteFragmentToSavePopupFragment(
                currentNoteArguments
            )
            Navigation.findNavController(view).navigate(action)
        } else {
            Toast.makeText(
                requireContext(),
                "Lütfen başlık ve metin bölümlerini doldurun!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun imageSelect(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    Snackbar.make(
                        view,
                        "Fotoğraf izni vermezseniz notlarınıza resim ekleyemeyiz. Lütfen ayarlarınızı kontrol edin.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("İzin ver") {
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }.show()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Snackbar.make(
                        view,
                        "Fotoğraf izni vermezseniz notlarınıza resim ekleyemeyiz. Lütfen ayarlarınızı kontrol edin.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("İzin ver") {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    Toast.makeText(requireContext(), "İzin verilmedi!", Toast.LENGTH_LONG).show()
                }
            }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null && intentFromResult.data != null) {
                        selectedImage = intentFromResult.data
                        try {
                            // Resmi geçici dosyaya kopyala (JPEG formatında, EXIF'i korumak için)
                            val tempFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                            requireActivity().contentResolver.openInputStream(selectedImage!!)?.use { input ->
                                FileOutputStream(tempFile).use { output ->
                                    input.copyTo(output)
                                }
                            }

                            // Bitmap'i dosya yolundan yükle, EXIF yönlendirmesini göz ardı et
                            val options = BitmapFactory.Options().apply {
                                inSampleSize = 4 // Bellek optimizasyonu
                            }
                            var bitmap = BitmapFactory.decodeFile(tempFile.absolutePath, options)
                            if (bitmap == null) {
                                // Dosya yüklenemezse ImageDecoder ile dene
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(
                                        requireActivity().contentResolver,
                                        selectedImage!!
                                    )
                                    bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                        decoder.setTargetSampleSize(4)
                                    }
                                } else {
                                    bitmap = MediaStore.Images.Media.getBitmap(
                                        requireActivity().contentResolver,
                                        selectedImage
                                    )
                                }
                            }

                            // EXIF yönlendirmesini düzelt
                            selectedBitmap = fixImageOrientation(tempFile.absolutePath, bitmap!!)
                            binding.imageViewAddNote.setImageBitmap(selectedBitmap)
                            binding.cardViewImageAddNote.visibility = View.VISIBLE

                            // Geçici dosyayı sil
                            tempFile.delete()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Resim yüklenemedi: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Resim seçilmedi!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    // Dosya yolundan EXIF yönlendirmesini düzelt
    private fun fixImageOrientation(imagePath: String, bitmap: Bitmap): Bitmap {
        try {
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
                // Eğer EXIF verisi yoksa veya normal ise döndürme yapma
                ExifInterface.ORIENTATION_NORMAL -> return bitmap
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            // EXIF okunamazsa resmi olduğu gibi döndür
            return bitmap
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