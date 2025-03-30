local nameId = GetPerson:get_message():get_nameId()

local path = "/name?id=" .. nameId

GetName:set_path(path)
GetName:set_method("GET")