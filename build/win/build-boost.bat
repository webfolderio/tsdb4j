@echo off

if not exist "build\boost_1_65_1" (
  if not exist "archive\boost_1_65_1.tar.gz" (
    curl -L  https://sourceforge.net/projects/boost/files/boost/1.65.1/boost_1_65_1.tar.gz/download --output archive\boost_1_65_1.tar.gz
  )
  tar xfz archive\boost_1_65_1.tar.gz -C build
)

cd build
cd boost_1_65_1

if not exist "b2.exe" (
  if "%TOOL_CHAIN%" == "mingw64" (
    cmd /c bootstrap.bat gcc
  )
)

if "%TOOL_CHAIN%" == "mingw64" (
  if "%BUILD_TYPE%" == "Release" (
    if "%SHARED_LIB%" == "true" (
      cmd /c b2.exe variant=release link=shared,static warnings=off toolset=gcc cxxflags="-w -std=c++11" --with-system --with-thread --with-filesystem --with-regex --with-date_time
    ) else if "%SHARED_LIB%" == "false" (
      cmd /c b2.exe variant=release link=static warnings=off toolset=gcc cxxflags="-w -std=c++11" --with-system --with-thread --with-filesystem --with-regex --with-date_time
    )
  ) else (
    cmd /c b2.exe variant=debug link=shared,static warnings=off toolset=gcc cxxflags="-w -std=c++11" --with-system --with-thread --with-filesystem --with-regex --with-date_time
  )
)

cd /D "%~dp0"