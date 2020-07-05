#!/bin/bash

if [ -z "$BOOST_TOOLSET" ]
then
  BOOST_TOOLSET=gcc
fi

CFLAGS="-w -fPIC"
CXXFLAGS="-w -fPIC"
BUILD_DIR=$(pwd)
BOOST_ROOT=$BUILD_DIR/boost
INSTALL_DIR=$BUILD_DIR/install

if [ "$FAST_BUILD" != "true" ]
then
  # -----------------------------------------------------------------------------
  # download muparser
  # -----------------------------------------------------------------------------

  if [ ! -d "muparser" ]; then
    curl -O -L https://github.com/beltoforion/muparser/archive/v2.2.5.zip
    unzip -q v2.2.5.zip
    mv muparser-2.2.5 muparser
    rm -f v2.2.5.zip
  fi

  # -----------------------------------------------------------------------------
  # download boost
  # -----------------------------------------------------------------------------

  if [ ! -d "boost" ]; then
    curl -O -L https://sourceforge.net/projects/boost/files/boost/1.65.1/boost_1_65_1.tar.bz2/download
    mv download boost_1_65_1.tar.bz2
    tar xf boost_1_65_1.tar.bz2
    rm -f boost_1_65_1.tar.bz2
    mv boost_1_65_1 boost
  fi

  # -----------------------------------------------------------------------------
  # download sqlite3
  # -----------------------------------------------------------------------------

  if [ ! -d "sqlite3" ]; then
    curl -O -L https://sqlite.org/2020/sqlite-autoconf-3320100.tar.gz
    tar xfz sqlite-autoconf-3320100.tar.gz
    rm -f sqlite-autoconf-3320100.tar.gz
    mv sqlite-autoconf-3320100 sqlite3
  fi

  # -----------------------------------------------------------------------------
  # download expat
  # -----------------------------------------------------------------------------

  if [ ! -d "expat" ]; then
    curl -L -O https://github.com/libexpat/libexpat/releases/download/R_2_2_9/expat-2.2.9.tar.gz
    tar xfz expat-2.2.9.tar.gz
    rm -f expat-2.2.9.tar.gz
    mv expat-2.2.9 expat
  fi

  # -----------------------------------------------------------------------------
  # download apr
  # -----------------------------------------------------------------------------

  if [ ! -d "apr" ]; then
    curl -O https://archive.apache.org/dist/apr/apr-1.6.5.tar.bz2
    tar xf apr-1.6.5.tar.bz2
    rm -f apr-1.6.5.tar.bz2
    mv apr-1.6.5 apr
  fi

  # -----------------------------------------------------------------------------
  # download apr-util
  # -----------------------------------------------------------------------------

  if [ ! -d "apr-util" ]; then
    curl -O https://archive.apache.org/dist/apr/apr-util-1.6.1.tar.bz2
    tar xf apr-util-1.6.1.tar.bz2
    rm -f apr-util-1.6.1.tar.bz2
    mv apr-util-1.6.1 apr-util
  fi

  # -----------------------------------------------------------------------------
  # download akumuli
  # -----------------------------------------------------------------------------

  if [ ! -d "akumuli" ]; then
    curl -L -O https://github.com/akumuli/Akumuli/archive/v0.8.80.zip
    unzip -q v0.8.80.zip
    mv Akumuli-0.8.80 akumuli
    rm -f v0.8.80.zip
  fi

  # -----------------------------------------------------------------------------
  # build muparser
  # -----------------------------------------------------------------------------

  cd muparser
  ./configure --prefix=$INSTALL_DIR --enable-shared=no --enable-samples=no
  make
  make install
  cd ..

  # -----------------------------------------------------------------------------
  # build boost
  # -----------------------------------------------------------------------------

  cd boost
  if [ ! -f "b2" ]
  then
    ./bootstrap.sh --with-toolset=$BOOST_TOOLSET
  fi
  ./b2 \
   toolset=$BOOST_TOOLSET \
   variant=release \
   link=static \
   warnings=off \
   cxxflags="-w -fPIC -std=c++11" \
   --with-chrono \
   --with-system \
   --with-thread \
   --with-filesystem \
   --with-regex \
   --with-date_time
  cd ..

  # -----------------------------------------------------------------------------
  # build sqlite3
  # -----------------------------------------------------------------------------

  cd sqlite3
  ./configure --prefix=$INSTALL_DIR --enable-static --enable-shared
  make
  make install
  cd ..

  # -----------------------------------------------------------------------------
  # build expat
  # -----------------------------------------------------------------------------

  cd expat
  ./configure --prefix=$INSTALL_DIR --enable-static
  make
  make install
  cd ..

  # -----------------------------------------------------------------------------
  # build apr
  # -----------------------------------------------------------------------------

  cd apr
  ./configure --prefix=$INSTALL_DIR --with-installbuilddir=$INSTALL_DIR/apr --enable-static
  make
  make install
  cd ..

  # -----------------------------------------------------------------------------
  # build apr-util
  # -----------------------------------------------------------------------------

  cd apr-util
  ./configure \
   --prefix=$INSTALL_DIR \
   --with-apr=$INSTALL_DIR \
   --with-expat=$INSTALL_DIR \
   --disable-util-dso \
   --without-gdbm \
   --without-ldap \
   --without-pgsql \
   --without-odbc \
   --with-sqlite3=$INSTALL_DIR
  make
  make install
  cd ..

  # -----------------------------------------------------------------------------
  # build akumuli
  # -----------------------------------------------------------------------------

  cp -p CMakeLists.root akumuli/CMakeLists.txt
  cp -p CMakeLists.lib akumuli/libakumuli/CMakeLists.txt

  cd akumuli
  mkdir -p build
  cd build

  cmake \
   -DCMAKE_INSTALL_LIBDIR=lib \
   -DCMAKE_BUILD_TYPE=Release \
   -DCMAKE_INSTALL_PREFIX=$INSTALL_DIR \
   -DAPR_INCLUDE_DIR=$INSTALL_DIR/include/apr-1 \
   -DAPRUTIL_INCLUDE_DIR=$INSTALL_DIR/include/apr-1 \
   -DSQLITE3_INCLUDE_DIR=$INSTALL_DIR/include \
   -DSQLITE3_LIBRARY=$INSTALL_DIR/libsqlite3.a \
   -DAPR_LIBRARY=$INSTALL_DIR/libapr-1.a \
   -DAPRUTIL_LIBRARY=$INSTALL_DIR/libaprutil-1.a \
   ..
  
  make
  make install
  cd ..
  cd ..
fi

# -----------------------------------------------------------------------------
# build tsdb4j
# -----------------------------------------------------------------------------

mkdir -p tsdb4j
cp -p -r ../../native/* tsdb4j/
cd tsdb4j
mkdir -p build
cd build

cmake \
 -DCMAKE_INSTALL_LIBDIR=lib \
 -DCMAKE_BUILD_TYPE=Release \
 -DCMAKE_INSTALL_PREFIX=$INSTALL_DIR \
 -DAKU_LIBRARY=$INSTALL_DIR/lib/libakumuli.a \
 -DROARING_LIBRARY=$INSTALL_DIR/lib/libroaring.a \
 -DLZ4_LIBRARY=$INSTALL_DIR/lib/liblz4.a \
 -DAPR_LIBRARY=$INSTALL_DIR/lib/libapr-1.a \
 -DAPR_UTIL_LIBRARY=$INSTALL_DIR/lib/libaprutil-1.a \
 -DSQLITE_LIBRARY=$INSTALL_DIR/lib/libsqlite3.a \
 -DAKU_INCLUDE_DIRS=$INSTALL_DIR/include \
 -DAPR_INCLUDE_DIR=$INSTALL_DIR/include/apr-1 \
 ..

make
make install

mkdir -p ../../../../src/main/resources/META-INF
cp $INSTALL_DIR/lib/libtsdb4j.{so,dylib} ../../../../src/main/resources/META-INF
