NameSave5:set_path("/name")
NameSave5:set_method("POST")
NameSave5:get_message():set_name(HttpRequest:get_message():get_name())