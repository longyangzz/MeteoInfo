package com.dclw.ncforecast.biz;


import com.dclw.ncforecast.mapper.ReadGribToSqlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadGribToSqlBIZ {
    @Autowired
    private ReadGribToSqlMapper readGribToSqlMapper;


    public Integer inserGribDataToSql(String values) {

        return  readGribToSqlMapper.inserGribDataToSql(values);
    }
}
