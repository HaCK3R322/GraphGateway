NameSave9:set_path("/name")
NameSave9:set_method("POST")
NameSave9:get_message():set_name(HttpRequest:get_message():get_name())