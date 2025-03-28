local id = PersonCreate.message:get_id()
local name = HttpRequest.message:get_name()
local age = HttpRequest.message:get_age()

HttpResponse.message:set_id(id)
HttpResponse.message:set_name(name)
HttpResponse.message:set_age(age)