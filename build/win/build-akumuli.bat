@echo off

if not exist "archive\akumuli-v0.8.80.zip" (
  curl -L https://github.com/akumuli/Akumuli/archive/v0.8.80.zip --output archive\akumuli-v0.8.80.zip
)

if not exist "build\akumuli" (
  tar xf archive\akumuli-v0.8.80.zip -C build
  cd build
  rename Akumuli-0.8.80 akumuli
  cd ..
  copy /Y patch\akumuli\win.patch build\akumuli
  cd build
  cd akumuli
  %PATCH_EXE% -p0 --forward < win.patch
  cd ..
  cd ..
)

cd build
cd akumuli
mkdir %BUILD_FOLDER% 2> NUL

cd %BUILD_FOLDER%

cmake ^
 -DCMAKE_BUILD_TYPE=%BUILD_TYPE% ^
 -DCMAKE_C_COMPILER=%C_COMPILER_EXE% ^
 -DCMAKE_CXX_COMPILER=%CXX_COMPILER_EXE% ^
 -DCMAKE_INSTALL_PREFIX=%INSTALL_DIR% ^
 -DSQLITE3_LIBRARY="%INSTALL_DIR%\lib\libsqlite3.%LIB_SUFFIX%" ^
 -DCMAKE_SHARED_LIBRARY_PREFIX="" ^
 .. ^
 -G"Ninja"

%CMD_BUILD%

cd /D "%~dp0"