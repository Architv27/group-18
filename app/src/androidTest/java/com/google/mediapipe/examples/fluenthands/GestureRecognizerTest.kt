/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.fluenthands

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.SystemClock
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.core.RunningMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class GestureRecognizerTest {

    private companion object {
        private const val TEST_IMAGE = "hand_thumb_up.jpg"
        private const val TEST_VIDEO = "test_video.mp4"
    }

    private val expectedCategoriesForImageAndLiveStreamMode = listOf(
        Category.create(0.8105f, 0, "Thumb_Up", ""),
    )

    private val expectedCategoryForVideoMode = listOf(
        Category.create(0.8193482f, 0, "Thumb_Up", ""),
    )
    private lateinit var lock: ReentrantLock
    private lateinit var condition: Condition

    @Before
    fun setup() {
        lock = ReentrantLock()
        condition = lock.newCondition()
    }

    /**
     * Verify that the result returned from the Gesture Recognizer Helper with
     * LIVE_STREAM mode is within the acceptable range to the expected result.
     */
    @Test
    @Throws(Exception::class)
    fun recognizerLiveStreamModeResultFallsWithinAcceptedRange() {
        var recognizerResult: GestureRecognizerHelper.ResultBundle? = null
        val gestureRecognizerHelper =
            GestureRecognizerHelper(context = ApplicationProvider.getApplicationContext(),
                mode = RunningMode.LIVE_STREAM,
                gestureListener = object :
                    GestureRecognizerHelper.GestureRecognizerListener {
                    override fun onError(error: String, errorCode: Int) {
                        // Print out the error
                        println(error)

                        // Release the lock
                        lock.withLock {
                            condition.signal()
                        }
                    }

                    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
                        recognizerResult = resultBundle

                        // Release the lock and start verifying the result
                        lock.withLock {
                            condition.signal()
                        }
                    }
                })

        // Create Bitmap and convert to MPImage.
        val testImage = loadImage(TEST_IMAGE)
        val mpImage = BitmapImageBuilder(testImage).build()

        // Run the hand gesture recognition on the sample image.
        gestureRecognizerHelper.recognizeAsync(
            mpImage, SystemClock.uptimeMillis()
        )

        // Lock to wait the GestureRecognizerHelper return the value.
        lock.withLock {
            condition.await()
        }

        // Verify that the recognizer is not null
        assertNotNull(recognizerResult)

        // Expecting one hand for this test case
        val categories = recognizerResult!!.results.first().gestures().first()

        // Verify that the categories are not empty
        assert(categories.isNotEmpty())

        // Verify that the scores are correct
        assertEquals(
            expectedCategoriesForImageAndLiveStreamMode.first().score(),
            categories.first().score(),
            0.01f
        )

        // Verify that the category names are consistent
        assertEquals(
            expectedCategoriesForImageAndLiveStreamMode.first().categoryName(),
            categories.first().categoryName()
        )
    }

    @Throws(Exception::class)
    private fun loadImage(fileName: String): Bitmap? {
        val assetManager: AssetManager =
            InstrumentationRegistry.getInstrumentation().context.assets
        val inputStream: InputStream = assetManager.open(fileName)
        return BitmapFactory.decodeStream(inputStream)
    }

    @Throws(Exception::class)
    private fun getVideoUri(videoName: String): Uri {
        val assetManager: AssetManager =
            InstrumentationRegistry.getInstrumentation().context.assets
        val file = File.createTempFile("test_video", ".mp4")
        val output = FileOutputStream(file)
        val inputStream: InputStream = assetManager.open(videoName)
        inputStream.copyTo(output)
        return file.toUri()
    }
}
