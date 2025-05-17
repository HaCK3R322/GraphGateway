local id = HttpRequest:get_message():get_id()

GetName:set_path("/breeds/" .. id)
GetName:set_method("GET")