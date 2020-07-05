cmake_minimum_required(VERSION 3.10)

project(tsdb4j LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 11)

find_package(JNI REQUIRED)

if ($ENV{CLION_IDE})
  if (WIN32)
    set(TSDB4J_OS "win")
    set(TSDBJ_DIST_DIR "dist")
  else()
    set(TSDB4J_OS "nix")
    set(TSDBJ_DIST_DIR "install")
  endif()
  set(ENV{BOOST_ROOT} "${CMAKE_CURRENT_SOURCE_DIR}/../build/${TSDB4J_OS}/build/boost_1_65_1")
  set(ENV{DCMAKE_INSTALL_PREFIX} "${CMAKE_CURRENT_SOURCE_DIR}/../build/${TSDB4J_OS}/${TSDBJ_DIST_DIR}")
  set(ENV{CMAKE_PREFIX_PATH} "${CMAKE_CURRENT_SOURCE_DIR}/../build/${TSDB4J_OS}/${TSDBJ_DIST_DIR}")
  find_library(AKU_LIBRARY NAMES libakumuli)
  find_library(APR_LIBRARY NAMES libapr-1)
  find_library(APR_UTIL_LIBRARY NAMES libaprutil-1)
  find_library(SQLITE_LIBRARY NAMES libsqlite3)
  find_library(LZ4_LIBRARY NAMES liblz4)
  find_library(ROARING_LIBRARY NAMES libroaring)
endif()

find_path(AKU_INCLUDE_DIRS NAMES "akumuli.h")
find_path(APR_INCLUDE_DIR NAMES "apr.h")

INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH})
INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH2})

if (WIN32)
  set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -static")
endif()

set(Boost_USE_STATIC_LIBS ON)
set(Boost_USE_MULTITHREADED ON)
set(Boost_USE_STATIC_RUNTIME OFF)

if(POLICY CMP0074)
  cmake_policy(SET CMP0074 NEW)
endif()

find_package(Boost 1.65.1
        COMPONENTS
        system
        thread
        filesystem
        regex
        date_time
        REQUIRED
)

# json parser from boost::property_tree needs this badly
add_definitions(-DBOOST_SPIRIT_THREADSAFE)
add_definitions(-DBOOST_PHOENIX_THREADSAFE)
add_definitions(-DBOOST_DATE_TIME_POSIX_TIME_STD_CONFIG)

include_directories(${Boost_INCLUDE_DIRS} ${AKU_INCLUDE_DIRS} ${APR_INCLUDE_DIR})

file(GLOB SRC
    "*.h"
    "*.cpp"
    "jnipp/*.h"
    "jnipp/*.cpp"
)

add_library(tsdb4j SHARED ${SRC})

if (WIN32)
  set_target_properties(tsdb4j PROPERTIES PREFIX "")
endif()

if (CMAKE_BUILD_TYPE MATCHES Release)
  set_target_properties(tsdb4j PROPERTIES LINK_FLAGS_RELEASE -s)
endif()

if (WIN32)
  target_link_libraries(tsdb4j PRIVATE
          ${AKU_LIBRARY}
          ${ROARING_LIBRARY}
          ${LZ4_LIBRARY}
          ${APR_LIBRARY}
          ${APR_UTIL_LIBRARY}
          ${SQLITE_LIBRARY}
          ${Boost_LIBRARIES}
          -static-libstdc++
          -static-libgcc
          pthread
          ws2_32
          mswsock
          Rpcrt4
  )
else()
  target_link_libraries(tsdb4j PRIVATE
          ${AKU_LIBRARY}
          ${ROARING_LIBRARY}
          ${LZ4_LIBRARY}
          ${APR_LIBRARY}
          ${APR_UTIL_LIBRARY}
          ${SQLITE_LIBRARY}
          ${Boost_LIBRARIES}
          pthread
  )
endif()

set_property(TARGET tsdb4j PROPERTY POSITION_INDEPENDENT_CODE ON)

if ($ENV{CLION_IDE})
  set_target_properties(tsdb4j
          PROPERTIES
          RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../src/main/resources/META-INF")
endif()

install(TARGETS tsdb4j LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR})