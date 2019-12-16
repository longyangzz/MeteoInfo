package com.dclw.ncforecast.controller;

import com.dclw.ncforecast.biz.ReadGribToSqlBIZ;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.python.apache.xerces.xs.StringList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("NCparse")
public class NCparse {
    @Autowired
    private ReadGribToSqlBIZ readGribToSqlBIZ;

    @Value("${custom.forecastData}")
    private String forecastData;

    @Value("${custom.type}")
    private String diguiTypeValue;


    @RequestMapping("readGribToSql")
    public @ResponseBody String readGribToSql(){
        //！ 目录结构示意
        //！ ForecastData
        //！     20180707
        //！         ShortTerm
        //！             Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2
        //！             Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2
        //！         240Term
        //！     20180708
        //！         ShortTerm
        //! 遍历指定目录下的所有文件夹
        //! 然后对文件夹下的72小时的指定要素数据进行读取写出
        //! 获取下边的目录
        String log = "readGribToSql start" + "\r\n";

        File[] files = new File( forecastData ).listFiles();
        List<String> subPathList = new ArrayList<>();
        //!! 针对目录
        //! isDigui;
        String isDigui = diguiTypeValue;
        int subfolderNum = 1;
        if(isDigui.equals("digui"))
        {
            subfolderNum = files.length;
        }


        for (int i = 0; i != subfolderNum; ++i  ) {
            File file = files[i];
            if ( file.isDirectory() ) {
                subPathList.add( file.getPath() + "\\ShortTerm\\");
            }
        }

        //! 对subPathList目录下的ShortTerm目录下的所有文件进行解析入库处理
        //! 遍历subPathList下的所有grib文件
        for ( String gribFolder : subPathList ) {
            File[] gribFiles = new File( gribFolder ).listFiles();

            for ( File gribFile : gribFiles ) {
                if ( !gribFile.isDirectory() ) {
                    log += "解析文件开始" + gribFile.getPath()  + "\r\n";
                    List<String> cunRecoords = OpenGribToSql(gribFile.getPath());

                    if(cunRecoords.size() > 0){
                        log += "解析到有效数据在文件" + gribFile.getPath()  + "\r\n";
                    }
                    else
                    {
                        log +=  gribFile.getPath() + "中没有解析到有效数据" + "\r\n";
                    }

                    //！ 记录超过1000条分批插入
                    for ( String cunRecoord : cunRecoords ){
                        if(cunRecoord.equals("") || cunRecoord.isEmpty())
                        {
                            continue;
                        }
                        Integer lt = readGribToSqlBIZ.inserGribDataToSql(cunRecoord);
                        log +=  gribFile.getPath() + "中的数据插入成功" + "\r\n";
                    }

                }
            }

        }


        return log;
    }

    //根据指定的文件名，读取并判断类型，解析到数据库中
    public List<String> OpenGribToSql(String fileName)
    {
        List<String> srList = new ArrayList<String >();
        String stateValues = new String();


        String baseName = new File(fileName).getName();
        String [] nameList = baseName.split("_");
        //！ 从文件名称解析生成三个时间
        SimpleDateFormat dateInFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat dateInFormat1 = new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat dateOutFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        /**
        * 字符串转化为时间
        */
        Date date1=null;
        try {
            String genTime = nameList[4];
            date1=dateInFormat.parse(genTime);
            genTime = dateOutFormat.format(date1);

            String startForecastTime = nameList[8];
            date1=dateInFormat1.parse(startForecastTime);
            startForecastTime = dateOutFormat.format(date1);

            int addHour = Integer.parseInt(nameList[9].substring(0,3));
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(date1);
            rightNow.add(Calendar.HOUR,addHour);
            date1 = rightNow.getTime();
            String forecastTime = dateOutFormat.format(date1);

            // ！ 判断数据文件类型
            if(fileName.endsWith("GRB2") && fileName.contains("EDA10"))
            {
                //String path = forecastData + "\\20180707\\ShortTerm\\Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2";
                String path = fileName;
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openNetCDFData(path);

                String uName = "u-component_of_wind_height_above_ground";
                GridData gridDataU = aDataInfo.getGridData(uName);

                String vName = "v-component_of_wind_height_above_ground";
                GridData gridDataV = aDataInfo.getGridData(vName);

                if(gridDataU.equals(null) || gridDataV.equals(null))
                {
                    return  srList;
                }

                //! 最后一条记录和其它记录有末尾是否加逗号的区别
                String fs = "";

                int xNum = gridDataV.xArray.length;//gridDataV.xArray.length;
                int yNUm = gridDataV.yArray.length;
                int tNum = xNum * yNUm;

                // 遍历维度层
                for(int row = 0; row != yNUm; ++row)
                {
                    for(int i = 0; i != xNum; ++i)
                    {
                        //! 经纬度索引号按行拍
                        float lon = (float)( gridDataV.xArray[i]);
                        float lat = (float)( gridDataV.yArray[i]);

                        int index = xNum * row + i;
                        float uValue = (float)( gridDataU.data[row][i]);
                        float vValue = (float)( gridDataV.data[row][i]);

                        if (index == tNum - 1 || ((index+1)%1000 == 0) ) {
                            fs = String.format("('%s', '%s', '%s', %f, %f, %f, %f, %s)", genTime, startForecastTime, forecastTime, uValue, vValue, lon, lat, Integer.toString(index));
                            stateValues += fs;
                            srList.add(stateValues);

                            //! 清空
                            stateValues = "";
                        } else {
                            fs = String.format("('%s', '%s', '%s', %f, %f, %f, %f, %s),", genTime, startForecastTime, forecastTime, uValue, vValue, lon, lat, Integer.toString(index));
                            stateValues += fs;
                        }


                    }
                }
            }
            return  srList;

        } catch (ParseException e) {
            return  srList;
        }

    }
}
