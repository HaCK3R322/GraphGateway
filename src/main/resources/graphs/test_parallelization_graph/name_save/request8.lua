NameSave8:set_path("/name")
NameSave8:set_method("POST")
NameSave8:get_message():set_name(HttpRequest:get_message():get_name())