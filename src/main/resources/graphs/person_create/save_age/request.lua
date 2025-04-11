SaveAge:set_path("/age")
SaveAge:set_method("POST")

local age = HttpRequest:get_message():get_age()

if age < 21 then
    SaveAge:discard("Age is less than 21")
    return
end

SaveAge:get_message():set_age(age)