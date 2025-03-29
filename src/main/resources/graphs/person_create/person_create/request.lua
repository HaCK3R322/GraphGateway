PersonCreate:set_path("/person")
PersonCreate:set_method("POST")

local nameId = SaveName:get_message():get_id()
local ageId = SaveAge:get_message():get_id()

PersonCreate:get_message():set_nameId(nameId)
PersonCreate:get_message():set_ageId(ageId)