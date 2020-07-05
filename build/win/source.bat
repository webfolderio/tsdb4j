@echo off

cd /D "%~dp0"

set ROOT_DIR=%~dp0

endlocal

mkdir build 2> NUL
mkdir archive 2> NUL
mkdir dist 2> NUL

if "%TOOL_CHAIN%" == "" set TOOL_CHAIN=mingw64
if "%SHARED_LIB%" == "" set SHARED_LIB=false
if "%BUILD_TYPE%" == "" set BUILD_TYPE=Release
set BUILD_FOLDER=cmake-build-mingw-%BUILD_TYPE%

set C_COMPILER_EXE=gcc.exe
set CXX_COMPILER_EXE=g++.exe

if "%TOOL_CHAIN%" == "llvm" (
  set C_COMPILER_EXE=clang.exe
  set CXX_COMPILER_EXE=clang++.exe
)

set CMD_BUILD=ninja install

set INSTALL_DIR=%ROOT_DIR%dist
set BOOST_ROOT=%ROOT_DIR%build\boost_1_65_1
set PATH=%ROOT_DIR%\cmake\bin;%ROOT_DIR%mingw64\bin;%ROOT_DIR%ninja;%INSTALL_DIR%\bin;%BOOST_ROOT%\stage\lib;%ROOT_DIR%build\Akumuli\%BUILD_FOLDER%\libakumuli;%PATH%
set PATCH_EXE="%PROGRAMFILES%\Git\usr\bin\patch.exe"

set EXPAT_C_FLAGS=-w
set EXPAT_CXX_FLAGS=-w
set EXPAT_SHARED=ON

set SQLITE_C_FLAGS=-w
set SQLLITE_SHARED=ON

set APR_C_FLAGS=-w
set APR_SHARED=ON

set APR_UTIL_C_FLAGS=-w
set APR_UTIL_SHARED=ON

set MUPARSER_C_FLAGS=-w
set MUPARSER_CXX_FLAGS=-w
set EXPAT_SHARED=ON
set MUPARSER_SHARED=ON

set MICROHTTPD_C_FLAGS=-w
set MICROHTTPD_SHARED=ON

set LOG4CXX_C_FLAGS=-w
set LOG4CXX_CXX_FLAGS=-w
set LOG4CXX_SHARED=ON

set LIB_SUFFIX=dll.a

if "%SHARED_LIB%" == "false" (
  set LIB_SUFFIX=a
  set EXPAT_SHARED=OFF
  set SQLLITE_SHARED=OFF
  set APR_SHARED=OFF
  set APR_UTIL_SHARED=OFF
  set MUPARSER_SHARED=OFF
  set MICROHTTPD_SHARED=OFF
  set LOG4CXX_SHARED=OFF
)
