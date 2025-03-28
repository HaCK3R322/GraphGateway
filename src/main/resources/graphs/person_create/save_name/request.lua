local name = HttpRequest.message:get_name()

SaveName.message:set_name(name)
SaveAge.message:set_path("/test/nameservice/name")
SaveAge.message:set_method("POST")