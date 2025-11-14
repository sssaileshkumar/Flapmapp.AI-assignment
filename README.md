Real-Time Edge Detection Viewer
This is an Android application that captures a live camera feed, processes it in real-time to detect edges using OpenCV and C++, and displays the output using OpenGL ES. It also features a web-based viewer to display the processed feed.

Features Implemented
Android App
Camera Feed Integration: Utilizes the Camera2 API to capture a high-framerate video stream.
Native C++ & JNI: All image processing is handled in native C++ for maximum performance, connected to the Android app via JNI.
OpenCV for Frame Processing: Implements Canny edge detection to process the camera frames.
OpenGL ES Rendering: Renders the processed video feed smoothly on a GLSurfaceView.
Web Server: A lightweight HTTP server (NanoHTTPD) is integrated to serve the web viewer and stream the processed frames.
Web Viewer
TypeScript-based: A simple web page built with HTML and TypeScript.
Real-time Display: Fetches and displays the processed frames from the Android web server.
Frame Stats: Includes placeholders for displaying FPS and resolution.
Screenshots
Add your screenshots or GIFs here showing the app in action.

Setup Instructions
Clone the repository.
Download the OpenCV for Android SDK: Obtain the SDK from the official OpenCV website.
Configure the OpenCV path:
Open the app/CMakeLists.txt file.
Locate the line set(OpenCV_DIR "...").
Replace the placeholder path with the absolute path to the sdk/native/jni directory within your downloaded OpenCV SDK.
Sync Gradle and build the project.
Architecture
JNI (Java Native Interface)
The communication between the Kotlin/Java code and the native C++ code is established through JNI. The CameraPreview.kt class includes a native method processFrame which is implemented in native-lib.cpp. This allows the high-performance OpenCV C++ code to be called from the Android application.

Frame Flow
Capture: The CameraPreview class captures frames from the device's camera using the Camera2 API's ImageReader.
Process: Each frame (in YUV_420_888 format) is passed to the processFrame native function. In C++, the Y-plane of the image is converted into an OpenCV Mat object. The Canny edge detection algorithm is then applied.
Render:
OpenGL: The resulting edge-detected image (a single-channel Mat) is converted to a Bitmap with an ARGB_8888 configuration. This Bitmap is then passed to the MyGLRenderer which renders it as a texture onto a GLSurfaceView.
Web: The same Bitmap is also compressed into a JPEG and sent to the WebServer.
TypeScript Web Viewer
The WebServer.kt class hosts a simple web page on port 8080 of the device. The viewer.js file on this page contains TypeScript code that repeatedly fetches the /frame endpoint to get the latest processed frame and displays it. This demonstrates a simple way to bridge native processing results to a web layer.
