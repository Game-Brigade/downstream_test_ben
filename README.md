Downstream

Previously the background was just the 1280x720 rectangle
I changed it to repeat (so that I could have the entire "level" bigger than just what the camera sees) in a 5x5 pattern. 

To use different control assists:
  At the top of FishController, set any of the fields
  "enableSlow," "enableLeadingLine," or "enableTetherRadius" to true

To change camera control:
  In FishController.update(), set the variable camera_mode to 0,1,2,3
    0:  laggy catch up
        if tethered, move quickly to center on tether, 
        else move slowly to fish
    1:  quick catch up
        if tethered, move slowly to tether, 
        else move quickly to fish
    2:  laggy catch up with space
        if tethered, move slowly to tether; 
        else if pressing space move quickly to fish, 
        else slowly to fish
    3:  follow player