#include <jni.h>
#include <android/bitmap.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_CameraPreview_processFrame(
        JNIEnv *env,
        jobject /* this */,
        jint width, jint height,
        jobject y_buffer, jint y_row_stride,
        jobject u_buffer, jint u_row_stride,
        jobject v_buffer, jint v_row_stride,
        jobject bitmap_out) {

    auto y_plane = static_cast<uint8_t *>(env->GetDirectBufferAddress(y_buffer));

    cv::Mat y_mat(height, width, CV_8UC1, y_plane, y_row_stride);
    cv::Mat edges;
    cv::Canny(y_mat, edges, 100, 200);

    void *bitmap_pixels;
    AndroidBitmap_lockPixels(env, bitmap_out, &bitmap_pixels);

    cv::Mat output_mat(height, width, CV_8UC4, bitmap_pixels);
    cv::cvtColor(edges, output_mat, cv::COLOR_GRAY2RGBA);

    AndroidBitmap_unlockPixels(env, bitmap_out);
}
