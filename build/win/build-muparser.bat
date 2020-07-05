@echo off

if not exist "build\muparser" (
  if not exist "archive\muparser-v2.2.6.1.tar.gz" (
    curl -L https://github.com/beltoforion/muparser/archive/v2.2.6.1.tar.gz --output archive\muparser-v2.2.6.1.tar.gz
  )
  tar xfz archive\muparser-v2.2.6.1.tar.gz -C build
  cd build
  rename muparser-2.2.6.1 muparser
  cd ..
)

cd build
cd muparser
mkdir %BUILD_FOLDER% 2> NUL

cd %BUILD_FOLDER%

cmake ^
 -DCMAKE_BUILD_TYPE=%BUILD_TYPE% ^
 -DCMAKE_C_COMPILER=%C_COMPILER_EXE% ^
 -DCMAKE_CXX_COMPILER=%CXX_COMPILER_EXE% ^
 -DCMAKE_INSTALL_PREFIX=%INSTALL_DIR% ^
 -DCMAKE_C_FLAGS=%LOG4CXX_C_FLAGS% ^
 -DCMAKE_CXX_FLAGS=%LOG4CXX_CXX_FLAGS% ^
 -DINSTALL_PDB=OFF ^
 -DENABLE_SAMPLES=OFF ^
 -DBUILD_SHARED_LIBS=%MUPARSER_SHARED% ^
 -DENABLE_OPENMP=OFF ^
 .. ^
 -G"Ninja"

%CMD_BUILD%

cd /D "%~dp0"