package com.example.spellcasterfurtherdonegood

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.spellcasterfurtherdonegood.drawingrecognizer.PDollarRecognizer
import com.example.spellcasterfurtherdonegood.drawingrecognizer.Point
import com.example.spellcasterfurtherdonegood.drawingrecognizer.PointCloud
import com.example.spellcasterfurtherdonegood.drawingrecognizer.PointCloudView
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

class DrawActivity : AppCompatActivity() {

    var sommaticPercentage = 100
    private lateinit var recognizer: PDollarRecognizer
    private lateinit var distanceTextView: TextView
    private lateinit var pointCloudView: PointCloudView
    private val points: MutableList<Point> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV")
        }

        val spellName = intent.getStringExtra("spellName")
        val spellSomatic = intent.getBooleanExtra("spellSomatic", false)
        val spellMaterial = intent.getStringExtra("spellMaterial")

        val spellNameTextView: TextView = findViewById(R.id.spell_name)
        val nextButton: Button = findViewById(R.id.button_next)
        val retryButton: Button = findViewById(R.id.button_retry)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        distanceTextView = findViewById(R.id.distance)
        pointCloudView = findViewById(R.id.pointCloudView)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            finish()
        }

        spellNameTextView.text = spellName
        progressBar.progress = 0
        progressBar.progressTintList = getColorStateList(R.color.colorProgressFirst)

        // Initialize PDollarRecognizer
        recognizer = PDollarRecognizer()

        // Load the cercle.png image and convert it to points
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.circle)
        val pointsFromImage = extractPointsFromImage(bitmap)

        // Recognize the points
        recognizer.pointClouds = ArrayList( mutableListOf(
            PointCloud("Circle", pointsFromImage)
        ))

        pointCloudView.setOnStrokeCompleteListener { stroke ->
            var lastId = 0
            if(points.isNotEmpty()) {
                lastId = points[points.size - 1].id
            }
            stroke.forEach { point ->
                Point(point.x, point.y, lastId + 1)
            }
            points.addAll(stroke)
            val result = recognizer.recognize(points)
            distanceTextView.text = result.score.toString()
            sommaticPercentage = (result.score * 100).toInt()
            progressBar.progress = sommaticPercentage
            if (sommaticPercentage > 0){
                progressBar.progressTintList = getColorStateList(R.color.colorProgressFirst)
            }
            if(sommaticPercentage > 25){
                progressBar.progressTintList = getColorStateList(R.color.colorProgressSecond)
            }
            if(sommaticPercentage > 50){
                progressBar.progressTintList = getColorStateList(R.color.colorProgressThird)
            }
            if(sommaticPercentage > 75){
                progressBar.progressTintList = getColorStateList(R.color.colorProgressFourth)
            }
        }
    }

    fun nextButton(view: View) {
        val nani = intent.getStringExtra("spellDamage")
        val dice = nani?.split("d")
        val maxDiceValue = dice?.get(1)?.toInt()
        val diceNumber = dice?.get(0)?.toInt()
        val damage = maxDiceValue?.times(diceNumber!!)
        val spellDamage = (intent.getIntExtra("incantationPercentage", 0) + sommaticPercentage) * damage!! / 200
        AlertDialog.Builder(this)
            .setTitle("Damage Report")
            .setMessage("You did $spellDamage damage.")
            .setPositiveButton("OK") { dialog, which ->
                finish()
            }
            .show()
    }

    fun retryButton(view: View) {
        points.clear()
        pointCloudView.clear()
        distanceTextView.text = "Recognition result: "
    }

    private fun extractPointsFromImage(bitmap: Bitmap): List<Point> {
        val points = mutableListOf<Point>()
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC1)
        val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        org.opencv.android.Utils.bitmapToMat(bmp32, mat)

        // Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        // Apply Canny edge detection
        Imgproc.Canny(mat, mat, 50.0, 150.0)

        // Find contours
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(mat, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Extract points from contours
        for (contour in contours) {
            for (point in contour.toArray()) {
                points.add(Point(point.x.toFloat(), point.y.toFloat(), 0))
            }
        }

        return points
    }
}