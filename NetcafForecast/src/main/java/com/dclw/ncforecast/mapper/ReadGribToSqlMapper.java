package com.dclw.ncforecast.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ReadGribToSqlMapper extends Mapper<Boolean> {
    //! 调用sql写出数据
    Integer inserGribDataToSql(@Param(value="stateValues")String values);
}

