NameSave3:set_path("/name")
NameSave3:set_method("POST")
NameSave3:get_message():set_name(HttpRequest:get_message():get_name())