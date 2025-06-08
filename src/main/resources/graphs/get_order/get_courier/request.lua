local courierId = GetDelivery:get_message():get_id()

GetCourier:set_path("/couriers?id=" .. courierId)
GetCourier:set_method("GET")