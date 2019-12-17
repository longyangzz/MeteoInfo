/**
 * Created by Miller on 2019/5/5.
 */

package com.dclw.ncforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("Hello")
public class Hello {
   @RequestMapping("index")
    public String TestDemo(){
       return "hello";
   }

    @RequestMapping("test")
    public @ResponseBody String test(){
       int i = 1;
       boolean state =true;
       while(state)
        {
            i = i +1;
            if(i == 10000000)
            {
                state = false;
            }
        }

        return "test";
    }
}
