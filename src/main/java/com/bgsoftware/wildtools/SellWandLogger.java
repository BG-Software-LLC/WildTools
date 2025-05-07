package com.bgsoftware.wildtools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SellWandLogger {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final Logger LOGGER = Logger.getLogger("WildTools-SellWand");
    private static boolean enabled = true;

    @SuppressWarnings("all")
    public static void setLogsFile(String logsFilePath){
        if(logsFilePath.isEmpty()){
            enabled = false;
            return;
        }

        File file = new File(plugin.getDataFolder(), logsFilePath);

        FileHandler fileHandler;

        try {
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            fileHandler = new FileHandler(file.getAbsolutePath(), true);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        fileHandler.setFormatter(new LogsFormatter());
        LOGGER.addHandler(fileHandler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void log(String line){
        if(enabled)
            LOGGER.info(line);
    }

    public static void close(){
    }

    private static class LogsFormatter extends Formatter{

        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[dd/MM/yy HH:mm:ss]");

        @Override
        public String format(LogRecord record) {
            return getDateAndTime() + " " + record.getMessage() + "\n";
        }

        private String getDateAndTime() {
            return dateFormat.format(new Date());
        }

    }

}
