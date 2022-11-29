package com.nureddinelmas.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.WHITE
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.custom_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


class MainActivity : AppCompatActivity() {
	private var drawing : DrawingView? = null
	private var mImageButtonCurrentPaint : ImageButton? = null
	var customProgressBar : Dialog? = null
	
	val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
		result ->
		if (result.resultCode == RESULT_OK && result.data != null){
			val imageBackground = findViewById<ImageView>(R.id.iv_background)
			imageBackground.setImageURI(result.data?.data)
		}
	}
	
	private val cameraResultLauncher : ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()){
		isGranted ->
		if (isGranted){
			Toast.makeText(this, "Permission granted for camera", Toast.LENGTH_LONG).show()
		} else {
			Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_LONG).show()
		}
	}
	private fun isReadStorageAllowed() : Boolean{
		var result = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
		
		return result == PackageManager.PERMISSION_GRANTED
	}
	
	private val locationAndCameraResultLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
			permission->
		permission.entries.forEach{
			val permissionName = it.key
			val isGranted = it.value
			
			if(isGranted){
				when (permissionName) {
					Manifest.permission.ACCESS_FINE_LOCATION -> {
						Toast.makeText(this, "Permission granted for location", Toast.LENGTH_LONG).show()
					}
					Manifest.permission.ACCESS_COARSE_LOCATION -> {
						Toast.makeText(this, "Permission granted for COARSE LOCATION", Toast.LENGTH_LONG).show()
					}
					Manifest.permission.READ_EXTERNAL_STORAGE -> {
						
						Toast.makeText(this, "Permission granted for READ_EXTERNAL_STORAGE", Toast.LENGTH_LONG).show()
						
						val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
						
						openGalleryLauncher.launch(pickIntent)
					
					}
					Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
						Toast.makeText(this, "Permission granted for WRITE_EXTERNAL_STORAGE", Toast.LENGTH_LONG).show()
					
					}
					else -> {
						Toast.makeText(this, "Permission granted for CAMERA", Toast.LENGTH_LONG).show()
					}
				}
			} else {
				when (permissionName) {
					Manifest.permission.ACCESS_FINE_LOCATION -> {
						Toast.makeText(this, "Permission denied for location", Toast.LENGTH_LONG).show()
					}
					Manifest.permission.ACCESS_COARSE_LOCATION -> {
						Toast.makeText(this, "Permission denied for ACCESS COARSE LOCATION", Toast.LENGTH_LONG).show()
					}
					Manifest.permission.READ_EXTERNAL_STORAGE -> {
						Toast.makeText(this, "Permission denied for READ_EXTERNAL_STORAGE", Toast.LENGTH_LONG).show()
					}
					Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
						Toast.makeText(this, "Permission denied for WRITE_EXTERNAL_STORAGE", Toast.LENGTH_LONG).show()
					}
					else -> {
						
						Toast.makeText(this, "Permission denied for Camera", Toast.LENGTH_LONG).show()
					}
				}
			}
		}
	
	}
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
		val linearLayoutPaintColors =   findViewById<LinearLayout>(R.id.ll_paint_colors)
		mImageButtonCurrentPaint = linearLayoutPaintColors[2] as ImageButton
		
		mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
		
		drawing = findViewById(R.id.drawingView)
		drawing!!.setSizeForBrush(10.toFloat())
		
		val galleryImageButton = findViewById<ImageButton>(R.id.id_gallery)
		
		galleryImageButton.setOnClickListener {
			// customDialogFunction()
			// customProgressDialogFunction()
		
			if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
					Manifest.permission.CAMERA) && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) && shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
				showRationaleDialog("Permission requires for all", "Camera cannot be used because Camera access is denied")
			}
			else {
				locationAndCameraResultLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))
			}
		}
		
		val brush  = findViewById<ImageButton>(R.id.id_brush)
		
		brush.setOnClickListener {
			showBrushSizeChooserDialog()
		}
		
		val undo  = findViewById<ImageButton>(R.id.id_undo)
		
		undo.setOnClickListener {
		drawing?.setUndo()
		}
		
		
		val save  = findViewById<ImageButton>(R.id.id_save)
		
		save.setOnClickListener {
		
			if(isReadStorageAllowed()){
				
				val customDialog = Dialog(this@MainActivity)
				
				customDialog.setContentView(R.layout.custom_dialog)
				customDialog.titleText.text = "Adding Background"
				customDialog.descriptionText.text = "Do you want to add background image ?"
				
				customDialog.tv_submit.text = "With background"
				customDialog.tv_cancel.text = "Without background"
				customDialog.tv_submit.setOnClickListener {
					lifecycleScope.launch{
					val flDrawing = findViewById<FrameLayout>(R.id.fl_drawing_view_container)
						customDialog.dismiss()
						saveBitmapFile(getBitmapFromView(flDrawing))
					}
				}
				
				customDialog.tv_cancel.setOnClickListener {
					lifecycleScope.launch{
						customDialog.dismiss()
					saveBitmapFile(getBitmapFromView(drawing!!))
					}
				}
				
				customDialog.show()
				
				
			}
		
		}
		
		
		
	}
	
	
	private fun getBitmapFromView(view: View): Bitmap {
		val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
		
		val canvas = Canvas(returnedBitmap)
		val bgDrawable = view.background
		
		if (bgDrawable != null){
			bgDrawable.draw(canvas)
		} else {
			canvas.drawColor(Color.WHITE)
		}
		
		view.draw(canvas)
		
		return returnedBitmap
	}
	
	
	
	private suspend fun saveBitmapFile(mBitmap: Bitmap?) : String{
		showProgressBarDialog()
		var result = ""
		withContext(Dispatchers.IO){
			if(mBitmap != null){
				try {
					val bytes = ByteArrayOutputStream()
					mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
					
					val f = File(externalCacheDir?.absoluteFile.toString() + File.separator + "KidsPainting" + System.currentTimeMillis() / 1000 + ".png")
				val fo = FileOutputStream(f)
					fo.write(bytes.toByteArray())
					fo.close()
					
					result = f.absolutePath
					runOnUiThread{
						cancelProgressBarDialog()
						if(result.isNotEmpty()){
							Toast.makeText(this@MainActivity, "File saved succesfully : $result", Toast.LENGTH_LONG).show()
							shareImage(FileProvider.getUriForFile(baseContext, "com.nureddinelmas.kidsdrawingapp.fileprovider", f))
						} else {
							Toast.makeText(this@MainActivity, "Something went wrong while saving the file", Toast.LENGTH_LONG).show()
							
						}
					}
				}
				catch (e: Exception) {
					result= ""
					e.printStackTrace()
				}
			}
		}
		
		return result
	}
	
	
	private fun showRationaleDialog( title: String, message: String){
		val builder: AlertDialog.Builder = AlertDialog.Builder(this)
		builder.setTitle(title)
			.setMessage(message)
			.setPositiveButton("Cancel"){dialog, _->
				dialog.dismiss()
			}
		
		builder.create().show()
	}
	
	
	private fun shareImage(uri: Uri){
		val shareIntent = Intent()
		shareIntent.action = Intent.ACTION_SEND
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
		shareIntent.type = "image/png"
		startActivity(Intent.createChooser(shareIntent, "share via"))
	}
	
	fun paintClicked(view: View){
	if (view !== mImageButtonCurrentPaint){
		val imageButton = view as ImageButton
		val colorTag = imageButton.tag.toString()
		drawing?.setColor(colorTag)
		
		imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
		
		mImageButtonCurrentPaint?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))
		
		mImageButtonCurrentPaint = view
	}
	}
	
	private fun showBrushSizeChooserDialog(){
		val brushDialog = Dialog(this)
		brushDialog.setContentView(R.layout.dialog_brush_size)
		brushDialog.setTitle("Brush Size : ")
		val smallButton = brushDialog.findViewById<ImageButton>(R.id.id_small_brush)
		smallButton.setOnClickListener {
			drawing?.setSizeForBrush(10.toFloat())
			brushDialog.dismiss()
		}
		
		val mediumButton = brushDialog.findViewById<ImageButton>(R.id.id_medium_brush)
		mediumButton.setOnClickListener {
			drawing?.setSizeForBrush(20.toFloat())
			brushDialog.dismiss()
		}
		
		val largeButton = brushDialog.findViewById<ImageButton>(R.id.id_large_brush)
		largeButton.setOnClickListener {
			drawing?.setSizeForBrush(30.toFloat())
			brushDialog.dismiss()
		}
		brushDialog.show()
	}
	
	
	private fun customDialogFunction(){
		val customDialog = Dialog(this)
		
		customDialog.setContentView(R.layout.custom_dialog)
		customDialog.tv_submit.setOnClickListener {
			Toast.makeText(applicationContext, "clicked submit", Toast.LENGTH_LONG).show()
			customDialog.dismiss()
		}
		customDialog.tv_cancel.setOnClickListener {
			Toast.makeText(applicationContext, "clicked cancel", Toast.LENGTH_LONG).show()
			customDialog.dismiss()
		}
		
		customDialog.show()
	}
	
	
	private fun customProgressDialogFunction(){
		val customProgressBarDialog = Dialog(this)
		
		customProgressBarDialog.setContentView(R.layout.custom_progress_bar_layout)
		
		customProgressBarDialog.show()
	}
	
	private fun showProgressBarDialog() {
		customProgressBar = Dialog(this@MainActivity)
		
		customProgressBar?.setContentView(R.layout.dialog_custom_progress)
		
		customProgressBar?.show()
	}
	
	private fun cancelProgressBarDialog(){
		if(customProgressBar != null){
			customProgressBar?.dismiss()
			customProgressBar = null
		}
	}
}