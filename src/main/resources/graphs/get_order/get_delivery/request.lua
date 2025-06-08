local orderId = HttpRequest:get_message():get_id()

GetDelivery:set_path("/deliveries?orderId=" .. orderId)
GetDelivery:set_method("GET")