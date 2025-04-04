NameSave4:set_path("/name")
NameSave4:set_method("POST")
NameSave4:get_message():set_name(HttpRequest:get_message():get_name())