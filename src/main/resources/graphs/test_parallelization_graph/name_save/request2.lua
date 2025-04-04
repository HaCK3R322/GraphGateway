NameSave2:set_path("/name")
NameSave2:set_method("POST")
NameSave2:get_message():set_name(HttpRequest:get_message():get_name())