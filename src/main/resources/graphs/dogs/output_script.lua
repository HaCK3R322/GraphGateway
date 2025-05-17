local name = GetName:get_message():get_data():get_attributes():get_name()
local description = GetDescription:get_message():get_data():get_attributes():get_description()
local maxLife = GetMaxLife:get_message():get_data():get_attributes():get_life():get_max()

HttpResponse:get_message():set_name(name)
HttpResponse:get_message():set_description(description)
HttpResponse:get_message():set_maxLife(maxLife)

HttpResponse:set_code(200)