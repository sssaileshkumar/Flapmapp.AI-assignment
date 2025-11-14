cmake_minimum_required(VERSION 3.4.1)

# Set the path to your OpenCV SDK
set(OpenCV_DIR "C:/Users/LENOVO/Downloads/opencv-4.12.0-android-sdk (1)/sdk/native/jni")

find_package(OpenCV REQUIRED)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/jni/native-lib.cpp )

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} 
        ${OpenCV_LIBS})