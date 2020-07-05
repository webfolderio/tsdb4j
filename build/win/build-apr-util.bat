@echo off

if not exist "archive\apr-util-1.6.1-win32-src.zip" (
  curl -L https://downloads.apache.org/apr/apr-util-1.6.1-win32-src.zip --output archive\apr-util-1.6.1-win32-src.zip
)

if not exist "build\apr-util" (
  tar xf archive\apr-util-1.6.1-win32-src.zip -C build
  cd build
  rename apr-util-1.6.1 apr-util
  cd ..
  copy /Y patch\apr-util\apu_version.patch build\apr-util
  copy /Y patch\apr-util\sqlite3.patch build\apr-util
  copy /Y patch\apr-util\odbc.patch build\apr-util
  copy /Y patch\apr-util\CMakeLists.txt build\apr-util
  cd build
  cd apr-util
  %PATCH_EXE% -p0 --forward < apu_version.patch
  %PATCH_EXE% -p0 --forward < sqlite3.patch
  %PATCH_EXE% -p0 --forward < odbc.patch
  cd ..
  cd ..
)

cd build
cd apr-util
mkdir %BUILD_FOLDER% 2> NUL

cd %BUILD_FOLDER%

cmake ^
 -DCMAKE_BUILD_TYPE=%BUILD_TYPE% ^
 -DCMAKE_C_COMPILER=%C_COMPILER_EXE% ^
 -DCMAKE_INSTALL_PREFIX=%INSTALL_DIR% ^
 -DCMAKE_C_FLAGS=%APR_UTIL_C_FLAGS% ^
 -DINSTALL_PDB=OFF ^
 -DAPR_LIBRARIES="%INSTALL_DIR%\lib\libapr-1.%LIB_SUFFIX%" ^
 -DSQLITE_LIBRARY="%INSTALL_DIR%\lib\libsqlite3.%LIB_SUFFIX%" ^
 -DXMLLIB_LIBRARIES="%INSTALL_DIR%\lib\libexpat.%LIB_SUFFIX%" ^
 -DAPR_HAS_LDAP=OFF ^
 -DAPU_HAVE_ODBC=OFF ^
 -DBUILD_SHARED_LIBS=%APR_UTIL_SHARED% ^
 .. ^
 -G"Ninja"

%CMD_BUILD%

cd /D "%~dp0"