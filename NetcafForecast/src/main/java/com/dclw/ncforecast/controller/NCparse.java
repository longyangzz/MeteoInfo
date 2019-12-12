package com.dclw.ncforecast.controller;

import com.dclw.ncforecast.biz.ReadGribToSqlBIZ;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("NCparse")
public class NCparse {
    @Autowired
    private ReadGribToSqlBIZ readGribToSqlBIZ;

    @RequestMapping("readGribToSql")
    public @ResponseBody String readGribToSql(){
        String path = "D:\\2019\\Z_NWGD_C_BEHK_20180707060034_P_RFFC_SPCC-EDA10_201807070800_00101.GRB2";
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openNetCDFData(path);

        String vName = "u-component_of_wind_height_above_ground";
        GridData _gridData = aDataInfo.getGridData(vName);

        Integer lt = readGribToSqlBIZ.inserGribDataToSql();

        return "readGribToSql sucess";
    }
}
