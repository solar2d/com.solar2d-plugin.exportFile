# Exporting files for Android

This plugin is useful when you want to expose some files generated by your app to system-wide directory, like "Downloads"

# Syntax

## `build.settings`

```lua
{
    plugins = {
        ["plugin.exportFile"] = { publisherId = "com.solar2D" },
    }
}
```
## Usage

```lua

local fileExport = require "plugin.exportFile"
fileExport.export {
	path = filePath,           -- mandatory, string, full path for file to export
	listener = debugListener,  -- optional, listener, to recieve event.isError and event.response
	name = fileName,           -- optional, string, if file name should be different than in path
	type = 'text/plain',       -- optional, string, MIME type of the output, default is */*
}

```

All arguments except `path` are optional. Path must point to readable file. For example `system.pathForFile( "share.txt", system.CachesDirectory )`

When called on API less than 19 would try to read and write files directly, so make sure to require WRITE_EXTERNAL_STORAGE permission in advance if targeting that platforms.
