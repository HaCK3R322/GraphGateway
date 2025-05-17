local id = GetId:get_message():get_id()

HttpResponse:get_message():set_id(id)
HttpResponse:set_code(200)