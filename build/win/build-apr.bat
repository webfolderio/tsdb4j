@echo off

if not exist "archive\apr-1.6.5-win32-src.zip" (
  curl -L https://downloads.apache.org/apr/apr-1.6.5-win32-src.zip --output archive\apr-1.6.5-win32-src.zip
)

if not exist "build\apr" (
  tar xf archive\apr-1.6.5-win32-src.zip -C build
  cd build
  rename apr-1.6.5 apr
  cd ..
  copy /Y patch\apr\apr_version.patch build\apr
  copy /Y patch\apr\apr-1.patch build\apr
  copy /Y patch\apr\apr-2-wtypes.patch build\apr
  copy /Y patch\apr\CMakeLists.txt build\apr
  cd build
  cd apr
  %PATCH_EXE% -p0 --forward < apr-1.patch
  %PATCH_EXE% -p0 --forward < apr-2-wtypes.patch
  %PATCH_EXE% -p0 --forward < apr_version.patch
  cd ..
  cd ..
)

cd build
cd apr
mkdir %BUILD_FOLDER% 2> NUL

cd %BUILD_FOLDER%

cmake ^
 -DCMAKE_BUILD_TYPE=%BUILD_TYPE% ^
 -DCMAKE_C_COMPILER=%C_COMPILER_EXE% ^
 -DCMAKE_INSTALL_PREFIX=%INSTALL_DIR% ^
 -DCMAKE_C_FLAGS=%APR_C_FLAGS% ^
 -DINSTALL_PDB=OFF ^
 -DBUILD_SHARED_LIBS=%APR_SHARED% ^
 .. ^
 -G"Ninja"

%CMD_BUILD%

cd /D "%~dp0"