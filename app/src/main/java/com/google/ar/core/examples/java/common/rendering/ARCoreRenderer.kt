/*
 * Copyright 2018 eje inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.examples.java.common.rendering

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.ar.core.examples.java.common.rendering.CameraBackground
import com.google.ar.core.exceptions.CameraNotAvailableException
import org.rajawali3d.materials.textures.StreamingTexture
import org.rajawali3d.math.Matrix4
import org.rajawali3d.renderer.Renderer
import javax.microedition.khronos.opengles.GL10

/**
 * Base class for implementing ARCore app with rajawali. You have to create class which extends
 * this class and pass it to [org.rajawali3d.view.SurfaceView.setSurfaceRenderer].
 */
abstract class ARCoreRenderer(context: Context,
                              private val session: Session) : Renderer(context) {

    private lateinit var backgroundTexture: StreamingTexture
    private val displayRotationHelper = DisplayRotationHelper(context)

    // Temporary matrix values
    private val projectionMatrixValues = FloatArray(16)
    private val viewMatrixValues = FloatArray(16)
    private val projectionMatrix = Matrix4()
    private val viewMatrix = Matrix4()

    override fun onResume() {
        super.onResume()

        displayRotationHelper.onResume()

        try {
            session.resume()
        } catch (e: CameraNotAvailableException) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            Log.e("ARCoreRenderer", "Error", e)
        }
    }

    override fun onPause() {
        super.onPause()

        displayRotationHelper.onPause()

        session.pause()
    }

    override fun onRenderSurfaceSizeChanged(gl: GL10?, width: Int, height: Int) {
        super.onRenderSurfaceSizeChanged(gl, width, height)
        displayRotationHelper.onSurfaceChanged(width, height)
    }

    override fun initScene() {

        // Create camera background
        val plane = CameraBackground()

        // Keep texture to field for later update
        backgroundTexture = plane.texture

        // Add to scene
        currentScene.addChild(plane)
    }

    /**
     * Process every frame update.
     */
    abstract fun onFrame(frame: Frame, ellapsedRealtime: Long, deltaTime: Double)

    override fun onRender(ellapsedRealtime: Long, deltaTime: Double) {
        super.onRender(ellapsedRealtime, deltaTime)

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session)

        session.setCameraTextureName(backgroundTexture.textureId)

        val frame = session.update()
        val camera = frame.camera

        // If not tracking, don't draw 3d objects.
        if (camera.trackingState == TrackingState.PAUSED) {
            return
        }

        onFrame(frame, ellapsedRealtime, deltaTime)

        // Update projection matrix.
        camera.getProjectionMatrix(projectionMatrixValues, 0, 0.1f, 100.0f)
        projectionMatrix.setAll(projectionMatrixValues)

        currentScene.camera.projectionMatrix = projectionMatrix

        // Update camera matrix.
        camera.getViewMatrix(viewMatrixValues, 0)
        viewMatrix.setAll(viewMatrixValues).inverse()

        currentScene.camera.setRotation(viewMatrix)
        currentScene.camera.position = viewMatrix.translation
    }

    override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {}
    override fun onTouchEvent(event: MotionEvent?) {}
}