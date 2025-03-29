PersonSave:set_path("/api/person")
PersonSave:set_method("POST")

PersonSave:get_message():set_id("123")
PersonSave:get_message():set_age(22)
PersonSave:get_message():copy_fullName(FullName:get_message())