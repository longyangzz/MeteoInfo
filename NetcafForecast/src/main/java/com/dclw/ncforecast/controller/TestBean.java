/**
 * Created by Miller on 2019/5/5.
 */

package com.dclw.ncforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("testBean")
public class TestBean {
   @RequestMapping("testDemo")
    public String TestDemo(){
       return "hello";
   }
}
