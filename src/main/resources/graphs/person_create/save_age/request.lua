SaveAge:set_path("/age")
SaveAge:set_method("POST")

local age = HttpRequest:get_message():get_age()

SaveAge:get_message():set_age(age)