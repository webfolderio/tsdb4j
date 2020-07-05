@echo off

if not exist "build\tsdb4j" (
  cd build
  mkdir tsdb4j
  cd tsdb4j
  robocopy /s /e /njh /njs /ndl /nc /ns ..\..\..\..\native .
  cd ..
  cd ..
)

cd build
cd tsdb4j
mkdir %BUILD_FOLDER% 2> NUL
cd %BUILD_FOLDER%

cmake ^
 -DCMAKE_BUILD_TYPE=%BUILD_TYPE% ^
 -DCMAKE_C_COMPILER=%C_COMPILER_EXE% ^
 -DCMAKE_CXX_COMPILER=%CXX_COMPILER_EXE% ^
 -DCMAKE_INSTALL_PREFIX=%INSTALL_DIR% ^
 -DAKU_LIBRARY="%INSTALL_DIR%\lib\libakumuli.%LIB_SUFFIX%" ^
 -DROARING_LIBRARY="%INSTALL_DIR%\lib\libroaring.%LIB_SUFFIX%" ^
 -DLZ4_LIBRARY="%INSTALL_DIR%\lib\liblz4.%LIB_SUFFIX%" ^
 -DAPR_LIBRARY="%INSTALL_DIR%\lib\libapr-1.%LIB_SUFFIX%" ^
 -DAPR_UTIL_LIBRARY="%INSTALL_DIR%\lib\libaprutil-1.%LIB_SUFFIX%" ^
 -DSQLITE_LIBRARY="%INSTALL_DIR%\lib\libsqlite3.%LIB_SUFFIX%" ^
 .. ^
 -G"Ninja"

%CMD_BUILD%

cd /D "%~dp0"

mkdir ..\..\src\main\resources\META-INF 2> NUL
copy /Y %INSTALL_DIR%\bin\tsdb4j.dll ..\..\src\main\resources\META-INF