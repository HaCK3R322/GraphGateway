local nameId = SaveName.message:get_id()
local ageId = SaveAge.message:get_id()

PersonCreate.message:set_nameId(nameId)
PersonCreate.message:set_ageId(ageId)

PersonCreate:set_path("/test/personservice/person")
PersonCreate.message:set_method("POST")