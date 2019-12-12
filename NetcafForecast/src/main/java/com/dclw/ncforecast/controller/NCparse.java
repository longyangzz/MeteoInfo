package com.dclw.ncforecast.controller;

import com.dclw.ncforecast.biz.ReadGribToSqlBIZ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("NCparse")
public class NCparse {
    @Autowired
    private ReadGribToSqlBIZ readGribToSqlBIZ;

    @RequestMapping("readGribToSql")
    public String readGribToSql(){
        Integer lt = readGribToSqlBIZ.inserGribDataToSql();

        return "hello";
    }
}
