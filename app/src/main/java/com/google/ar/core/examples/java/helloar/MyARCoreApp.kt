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
package com.google.ar.core.examples.java.helloar

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import com.google.ar.core.*
import com.google.ar.core.examples.java.common.helpers.TapHelper
import com.google.ar.core.examples.java.common.rendering.ARCoreRenderer
import org.rajawali3d.Object3D
import org.rajawali3d.loader.LoaderOBJ
import org.rajawali3d.materials.Material
import org.rajawali3d.materials.textures.Texture

class MyARCoreApp(context: Context,
                  private val tapHelper: TapHelper,
                  session: Session) : ARCoreRenderer(context, session) {

    private lateinit var droid: Object3D

    override fun initScene() {
        super.initScene()

        //  Spawn droid object in front of you
        val objParser = LoaderOBJ(this, R.raw.andy)
        objParser.parse()
        droid = objParser.parsedObject.getChildAt(1)
        droid.setPosition(0.0, 0.0, -1.0)
        droid.material = Material().apply {
            addTexture(Texture("droid", R.raw.andy_tex))
            color = Color.BLACK
        }
        currentScene.addChild(droid)
    }

    override fun onFrame(frame: Frame, ellapsedRealtime: Long, deltaTime: Double) {

        val tap = tapHelper.poll()

        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {
            onTap(frame, tap)
        }

        /*
         * Light estimating.
         */

        // Compute lighting from average intensity of the image.
        // The first three components are color scaling factors.
        // The last one is the average pixel intensity in gamma space.
//        val colorCorrectionRgba = FloatArray(4)
//        frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)

        /*
         * Process point cloud
         */

        // Process point cloud
        // Application is responsible for releasing the point cloud resources after
        // using it.
//        val pointCloud = frame.acquirePointCloud()

        // Do something

        // And finally call this
//        pointCloud.release()
    }

    private fun onTap(frame: Frame, tap: MotionEvent) {
        for (hit in frame.hitTest(tap)) {

            // Check if any plane was hit, and if it was hit inside the plane polygon
            val trackable = hit.trackable

            // Creates an anchor if a plane or an oriented point was hit.
            if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose) || trackable is Point && trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {

                // Create anchor at touched place
                val anchor = hit.createAnchor()
//                val rot = FloatArray(4)
//                anchor.pose.getRotationQuaternion(rot, 0)
                val translation = FloatArray(3)
                anchor.pose.getTranslation(translation, 0)

                // Spawn new droid object at anchor position
                val newDroid = droid.clone().apply {
                    setPosition(translation[0].toDouble(), translation[1].toDouble(), translation[2].toDouble())
                }
                currentScene.addChild(newDroid)
                break
            }
        }
    }
}