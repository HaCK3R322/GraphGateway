NameSave6:set_path("/name")
NameSave6:set_method("POST")
NameSave6:get_message():set_name(HttpRequest:get_message():get_name())