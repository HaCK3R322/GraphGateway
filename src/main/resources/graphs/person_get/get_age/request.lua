local ageId = GetPerson:get_message():get_ageId()

local path = "/age?id=" .. ageId

GetAge:set_path(path)
GetAge:set_method("GET")
