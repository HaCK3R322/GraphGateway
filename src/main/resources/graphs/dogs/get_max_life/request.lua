local id = HttpRequest:get_message():get_id()

GetMaxLife:set_path("/breeds/" .. id)
GetMaxLife:set_method("GET")