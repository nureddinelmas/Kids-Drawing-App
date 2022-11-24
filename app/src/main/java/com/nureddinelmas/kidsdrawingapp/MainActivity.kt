package com.nureddinelmas.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.custom_dialog.*


class MainActivity : AppCompatActivity() {
	private var drawing : DrawingView? = null
	private var mImageButtonCurrentPaint : ImageButton? = null
	
	
	private val cameraResultLauncher : ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()){
		isGranted ->
		if (isGranted){
			Toast.makeText(this, "Permission granted for camera", Toast.LENGTH_LONG).show()
		} else {
			Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_LONG).show()
		}
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
					Manifest.permission.CAMERA)){
				showRationaleDialog("Permission requires camera access", "Camera cannot be used because Camera access is denied")
			}
			else {
				locationAndCameraResultLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
			}
		}
		
		val brush  = findViewById<ImageButton>(R.id.id_brush)
		
		brush.setOnClickListener {
			showBrushSizeChooserDialog()
		}
		
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
}