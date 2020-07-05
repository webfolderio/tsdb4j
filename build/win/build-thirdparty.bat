@echo off

@rem build expat 2.2.9
call build-expat.bat

@rem build sqlite3 3.31.1
call build-sqlite.bat

@rem build apr 1.6.5
call build-apr.bat

@rem build apr-util 1.6.1
call build-apr-util.bat

@rem build muparser 2.2.6.1
call build-muparser.bat