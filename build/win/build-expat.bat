@echo off

if not exist "build\expat" (
  if not exist "archive\expat-2.2.9.tar.gz" (
    curl -L https://github.com/libexpat/libexpat/releases/download/R_2_2_9/expat-2.2.9.tar.gz --output archive\expat-2.2.9.tar.gz
  )
  tar xfz archive\expat-2.2.9.tar.gz -C build
  cd build
  rename expat-2.2.9 expat
  cd ..
)

cd build
cd expat
mkdir %BUILD_FOLDER% 2> NUL

cd %BUILD_FOLDER%

cmake ^
 -DCMAKE_BUILD_TYPE=%BUILD_TYPE% ^
 -DCMAKE_C_COMPILER=%C_COMPILER_EXE% ^
 -DCMAKE_CXX_COMPILER=%CXX_COMPILER_EXE% ^
 -DCMAKE_INSTALL_PREFIX=%INSTALL_DIR% ^
 -DCMAKE_C_FLAGS=%EXPAT_C_FLAGS% ^
 -DCMAKE_CXX_FLAGS=%EXPAT_CXX_FLAGS% ^
 -DEXPAT_SHARED_LIBS=%EXPAT_SHARED% ^
 -DEXPAT_BUILD_EXAMPLES=OFF ^
 -DEXPAT_BUILD_TESTS=OFF ^
 -DEXPAT_BUILD_DOCS=OFF ^
 -DEXPAT_BUILD_FUZZERS=OFF ^
 -DEXPAT_BUILD_TOOLS=OFF ^
 -DCMAKE_DEBUG_POSTFIX="" ^
 .. ^
 -G"Ninja"

%CMD_BUILD%

cd /D "%~dp0"