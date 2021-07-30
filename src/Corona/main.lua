
local fileExport = require "plugin.exportFile"

local function debugListener( event )
	print("ExportFile Event", require("json").prettify(event))
end


local fileName = "fl " .. os.date() .. ".txt"
local filePath = system.pathForFile( fileName, system.TemporaryDirectory )

-- Create some test file with contents
local f = io.open( filePath, "w" )
f:write( os.date() )
f:close( )


fileExport.export {
	path = filePath,           -- mandatory, string, path for file to export
	listener = debugListener,  -- optional, listener, to recieve event.isError and event.response
	name = fileName,           -- optional, string, if file name should be different than in path
	type = 'text/plain',       -- optional, string, MIME type of the output, default is */*
}



-- local function networkListener( event )
--     if ( event.isError ) then
--         print( "Network error - download failed: ", event.response )
--     elseif ( event.phase == "began" ) then
--         print( "Progress Phase: began" )
--     elseif ( event.phase == "ended" ) then
--         print( "Displaying response image file" )
-- 		fileExport.export{
--             path = system.pathForFile(event.response.filename, event.response.baseDirectory),
--             listener = debugListener,
--         }
--     end
-- end

-- network.download(
--     "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-large-zip-file.zip",
--     "GET",
--     networkListener,
--     { progress = false },
--     "sample.zip",
--     system.TemporaryDirectory
-- )
