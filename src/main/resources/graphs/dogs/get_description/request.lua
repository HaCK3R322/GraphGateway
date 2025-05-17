local id = HttpRequest:get_message():get_id()

GetDescription:set_path("/breeds/" .. id)
GetDescription:set_method("GET")