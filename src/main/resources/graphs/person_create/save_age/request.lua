local age = HttpRequest.message:get_age()

SaveAge.message:set_age(age)
SaveAge.message:set_path("/test/ageservice/age")
SaveAge.message:set_method("POST")