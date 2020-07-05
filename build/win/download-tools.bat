@echo off

@rem download mingw64 8.1

if "%TOOL_CHAIN%" == "mingw64" (
  if not exist "mingw64" (
    if not exist "archive\x86_64-8.1.0-release-posix-seh-rt_v6-rev0.7z" (
      curl -L "https://sourceforge.net/projects/mingw-w64/files/Toolchains targetting Win64/Personal Builds/mingw-builds/8.1.0/threads-posix/seh/x86_64-8.1.0-release-posix-seh-rt_v6-rev0.7z/download" --output archive\x86_64-8.1.0-release-posix-seh-rt_v6-rev0.7z
    )
    7z x archive\x86_64-8.1.0-release-posix-seh-rt_v6-rev0.7z
  )
)

@rem download cmake 3.17.3

if not exist "cmake" (
  if not exist "archive\cmake-3.17.3-win64-x64.zip" (
    curl -L "https://github.com/Kitware/CMake/releases/download/v3.17.3/cmake-3.17.3-win64-x64.zip" --output archive\cmake-3.17.3-win64-x64.zip
  )
  tar xf archive\cmake-3.17.3-win64-x64.zip
  rename cmake-3.17.3-win64-x64 cmake
)

@rem download ninja 1.10.0

if not exist "ninja" (
  mkdir ninja 2> NUL
  if not exist "archive\ninja-win.zip" (
    curl -L "https://github.com/ninja-build/ninja/releases/download/v1.10.0/ninja-win.zip" --output archive\ninja-win-1.10.0.zip
  )
  tar xf archive\ninja-win-1.10.0.zip -C ninja
)