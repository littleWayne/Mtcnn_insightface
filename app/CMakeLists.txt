cmake_minimum_required(VERSION 3.4.1)

#include头文件目录
include_directories(src/main/cpp/include
                    src/main/cpp/)

#source directory源文件目录
file(GLOB native_srcs src/main/cpp/*.cpp)
#set(FACERECOGNITION_COMPILE_CODE ${FACERECOGNITION_SRC})


#添加ncnn库
add_library(libncnn STATIC IMPORTED )
set_target_properties(libncnn
  PROPERTIES IMPORTED_LOCATION
  ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

#编译为动态库
add_library(facerecognition SHARED ${native_srcs})

#添加工程所依赖的库
find_library(  log-lib log )
target_link_libraries(  facerecognition
                       libncnn
                       jnigraphics
                       z
                       ${log-lib} )
