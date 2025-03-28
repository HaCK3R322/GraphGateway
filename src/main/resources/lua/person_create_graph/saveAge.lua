local name = HttpRequest.message.name

SaveAge.message:set_name(name)

SaveAge.message:set_path("/test/ageservice/age")
SaveAge.message:set_method("POST")