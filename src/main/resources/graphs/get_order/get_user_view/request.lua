local userId = GetCourier:get_message():get_userId()

GetUserView:set_path("/view/users?id=" .. userId)
GetUserView:set_method("GET")