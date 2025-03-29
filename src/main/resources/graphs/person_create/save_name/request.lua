SaveName:set_path("/name")
SaveName:set_method("POST")

local name = HttpRequest:get_message():get_name()

SaveName:get_message():set_name(name)