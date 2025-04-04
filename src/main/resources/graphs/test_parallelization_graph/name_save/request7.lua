NameSave7:set_path("/name")
NameSave7:set_method("POST")
NameSave7:get_message():set_name(HttpRequest:get_message():get_name())