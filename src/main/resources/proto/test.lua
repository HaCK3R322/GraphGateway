SaveName:set_path("/lolkek")
SaveName:set_method("GET")

SaveName:get_message():set_name("artem")

SaveName:get_message():get_person():set_id("4444444")
SaveName:get_message():get_person():set_age("99")

local newTableAsMessage = SaveName:get_message():get_person()
SaveName:get_message():set_person(newTableAsMessage)