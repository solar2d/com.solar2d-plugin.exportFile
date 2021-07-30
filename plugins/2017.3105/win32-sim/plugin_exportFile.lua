local Library = require "CoronaLibrary"

-- Create library
local lib = Library:new{ name='plugin.exportFile', publisherId='com.solar2d' }

-------------------------------------------------------------------------------
-- BEGIN (Insert your implementation starting here)
-------------------------------------------------------------------------------
local function placeholder()
	print( "WARNING: This library is not available on this platform")
end

lib.export = placeholder

return lib