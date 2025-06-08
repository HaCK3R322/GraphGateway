FinalizeOrder:set_path("/finalize/order")
FinalizeOrder:set_method("POST")

FinalizeOrder:get_message():set_orderId(HttpRequest:get_message():get_id())
FinalizeOrder:get_message():copy_userView(GetUserView:get_message())
FinalizeOrder:get_message():copy_delivery(GetDelivery:get_message())
FinalizeOrder:get_message():copy_courier(GetCourier:get_message())