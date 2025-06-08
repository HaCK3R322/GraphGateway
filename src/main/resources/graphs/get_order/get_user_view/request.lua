local userId = GetCourier:get_message():get_userId()

GetUserView:set_path("/view/users?id=" .. userId)
GetCourier:set_method("GET")