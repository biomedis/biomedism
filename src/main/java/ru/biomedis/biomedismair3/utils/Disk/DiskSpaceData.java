package ru.biomedis.biomedismair3.utils.Disk;

import java.nio.file.FileStore;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * представляет класс агрегации  параметров дискового пространства
 * Created by Anama on 02.09.2015.
 */
public class DiskSpaceData
{
    static final long KILO_BYTE = 1024;
    static final long MEGA_BYTE = 1024*1024;

    public static  enum SizeDiskType{BYTE,KILLO,MEGA};
    private long total;
    private long avaliable;
    private long used;

    public DiskSpaceData(long total, long avaliable, long used) {
        this.total = total;
        this.avaliable = avaliable;
        this.used = used;
    }

    public long getTotal(SizeDiskType type)
    {
        switch (type)
        {
            case BYTE:
                return total;

            case KILLO:
                return total/KILO_BYTE;

            case MEGA:
                return total/MEGA_BYTE;

            default:   return total;
        }

    }



    public long getAvaliable(SizeDiskType type) {

        switch (type)
        {
            case BYTE:
                return avaliable;

            case KILLO:
                return avaliable/KILO_BYTE;

            case MEGA:
                return avaliable/MEGA_BYTE;

            default:   return avaliable;
        }
    }



    public long getUsed(SizeDiskType type) {

        switch (type)
        {
            case BYTE:
                return used;

            case KILLO:
                return used/KILO_BYTE;

            case MEGA:
                return used/MEGA_BYTE;

            default:   return used;
        }
    }

    public static DiskSpaceData getDiskSpace(FileStore fs)
    {
        DiskSpaceData ds=null;

        try {
            long totalSpace = fs.getTotalSpace() ;
            long usableSpace = fs.getUsableSpace();
            long usedSpace = (fs.getTotalSpace() - fs.getUnallocatedSpace());
            ds=new DiskSpaceData(totalSpace,usableSpace,usedSpace);

        }catch (Exception e)
        {
            logger.error("",e);
            ds=null;
        }
        return ds;
    }
}

