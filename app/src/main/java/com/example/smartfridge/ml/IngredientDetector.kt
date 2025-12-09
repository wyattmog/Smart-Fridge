package com.example.smartfridge.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.smartfridge.assetFilePath
import java.nio.FloatBuffer

class IngredientDetector(context: Context) {

    private val classNames: List<String>
    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    init {
        val modelPath = context.assetFilePath("best.onnx")
        val options = OrtSession.SessionOptions()
        
        // Enable NNAPI for hardware acceleration
        options.addNnapi()

        session = env.createSession(modelPath, options)
        classNames = context.assets.open("classes.txt").bufferedReader().readLines()
    }

    fun detect(bitmap: Bitmap): List<String> {
        val inputTensor = preprocess(bitmap)
        val outputs = session.run(mapOf("images" to inputTensor))
        @Suppress("UNCHECKED_CAST")
        val outputBuffer = outputs[0].value as Array<Array<FloatArray>>
        return postprocess(outputBuffer)
    }

    private fun preprocess(bitmap: Bitmap): OnnxTensor {
        val size = 640
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val buffer = FloatBuffer.allocate(size * size * 3)

        val pixels = IntArray(size * size)
        resizedBitmap.getPixels(pixels, 0, size, 0, 0, size, size)

        for (i in 0 until size * size) {
            val pixel = pixels[i]
            buffer.put(i, ((pixel shr 16 and 0xFF) / 255.0f))
            buffer.put(i + size * size, ((pixel shr 8 and 0xFF) / 255.0f))
            buffer.put(i + 2 * size * size, ((pixel and 0xFF) / 255.0f))
        }

        val shape = longArrayOf(1, 3, size.toLong(), size.toLong())
        return OnnxTensor.createTensor(env, buffer, shape)
    }

    private fun postprocess(output: Array<Array<FloatArray>>): List<String> {
        val confThreshold = 0.45f
        val detectedIngredients = mutableSetOf<String>()

        val numDetections = output[0][0].size
        val propertiesPerDetection = output[0].size

        var maxConfidenceFound = 0f

        for (i in 0 until numDetections) {
            val detection = FloatArray(propertiesPerDetection) { j ->
                output[0][j][i]
            }

            val classScores = detection.copyOfRange(4, propertiesPerDetection)

            var maxScore = -1f
            var classId = -1
            for (k in classScores.indices) {
                if (classScores[k] > maxScore) {
                    maxScore = classScores[k]
                    classId = k
                }
            }
            
            if (maxScore > maxConfidenceFound) {
                maxConfidenceFound = maxScore
            }

            if (maxScore > confThreshold && classId != -1) {
                val ingredientName = classNames[classId]
                detectedIngredients.add(ingredientName)
                Log.d("IngredientDetector", "Detected: $ingredientName with confidence $maxScore")
            }
        }

        Log.d("IngredientDetector", "Max confidence found in this image: $maxConfidenceFound")
        if (detectedIngredients.isEmpty()) {
            Log.w("IngredientDetector", "No ingredients detected with confidence > $confThreshold")
        }

        return detectedIngredients.toList()
    }
}
