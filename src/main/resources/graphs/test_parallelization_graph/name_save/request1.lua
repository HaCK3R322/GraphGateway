NameSave1:set_path("/name")
NameSave1:set_method("POST")
NameSave1:get_message():set_name(HttpRequest:get_message():get_name())