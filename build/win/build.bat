call source.bat
if not "%FAST_BUILD%" == "true" (
  call download-tools.bat
  call build-boost.bat
  call build-thirdparty.bat
  call build-akumuli.bat
)
call build-tsdb4j.bat