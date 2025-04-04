NameSave10:set_path("/name")
NameSave10:set_method("POST")
NameSave10:get_message():set_name(HttpRequest:get_message():get_name())