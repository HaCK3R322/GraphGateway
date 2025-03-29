local id = PersonCreate:get_message():get_id()
local name = HttpRequest:get_message():get_name()
local age = HttpRequest:get_message():get_age()

HttpResponse:get_message():set_id(id)
HttpResponse:get_message():set_name(name)
HttpResponse:get_message():set_age(age)

HttpResponse:set_code(201)
HttpResponse:set_discarded(false)