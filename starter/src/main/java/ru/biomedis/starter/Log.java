package ru.biomedis.starter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Anama on 22.02.2016.
 */
public class Log
{
    public static final Logger logger = LogManager.getLogger("org.biomedis.starter");
    public static void loggerTest(){logger.error("IT WORKING!");}

  static
  {


  }


}
