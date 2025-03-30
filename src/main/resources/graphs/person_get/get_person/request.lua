local id = HttpRequest:get_message():get_id()

local path = "/person?id=" .. id

GetPerson:set_path(path)
GetPerson:set_method("GET")