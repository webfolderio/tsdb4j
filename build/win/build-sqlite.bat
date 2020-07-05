@echo off

if not exist "build\sqlite" (
  if not exist "archive\sqlite-amalgamation-3320200.zip" (
    curl -L https://www.sqlite.org/2020/sqlite-amalgamation-3320200.zip --output archive\sqlite-amalgamation-3320200.zip
  )
  tar xf archive\sqlite-amalgamation-3320200.zip -C build
  cd build
  rename sqlite-amalgamation-3320200 sqlite
  cd ..
  copy /Y patch\sqlite\CMakeLists.txt build\sqlite
)


cd build
cd sqlite
mkdir %BUILD_FOLDER% 2> NUL

cd %BUILD_FOLDER%

cmake ^
 -DCMAKE_BUILD_TYPE=%BUILD_TYPE% ^
 -DCMAKE_C_COMPILER=%C_COMPILER_EXE% ^
 -DCMAKE_INSTALL_PREFIX=%INSTALL_DIR% ^
 -DCMAKE_C_FLAGS=%SQLITE_C_FLAGS% ^
 -DBUILD_SHARED_LIBS=%SQLLITE_SHARED% ^
 .. ^
 -G"Ninja"

%CMD_BUILD%

cd /D "%~dp0"
