package com.samrj.devil.al;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import org.lwjgl.openal.EXTEfx;

public final class EAXReverbPreset
{
    //Default presets

    public static EAXReverbPreset generic()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset paddedCell()
        {return new EAXReverbPreset(0.1715f, 1.0000f, 0.3162f, 0.0010f, 1.0000f, 0.1700f, 0.1000f, 1.0000f, 0.2500f, 0.0010f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2691f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset room()
        {return new EAXReverbPreset(0.4287f, 1.0000f, 0.3162f, 0.5929f, 1.0000f, 0.4000f, 0.8300f, 1.0000f, 0.1503f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0629f, 0.0030f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset bathroom()
        {return new EAXReverbPreset(0.1715f, 1.0000f, 0.3162f, 0.2512f, 1.0000f, 1.4900f, 0.5400f, 1.0000f, 0.6531f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 3.2734f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset livingRoom()
        {return new EAXReverbPreset(0.9766f, 1.0000f, 0.3162f, 0.0010f, 1.0000f, 0.5000f, 0.1000f, 1.0000f, 0.2051f, 0.0030f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2805f, 0.0040f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset stoneRoom()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.7079f, 1.0000f, 2.3100f, 0.6400f, 1.0000f, 0.4411f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1003f, 0.0170f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset auditorium()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.5781f, 1.0000f, 4.3200f, 0.5900f, 1.0000f, 0.4032f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7170f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset concertHall()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.5623f, 1.0000f, 3.9200f, 0.7000f, 1.0000f, 0.2427f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.9977f, 0.0290f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset cave()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 1.0000f, 1.0000f, 2.9100f, 1.3000f, 1.0000f, 0.5000f, 0.0150f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7063f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset arena()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.4477f, 1.0000f, 7.2400f, 0.3300f, 1.0000f, 0.2612f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0186f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset hangar()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.3162f, 1.0000f, 10.0500f, 0.2300f, 1.0000f, 0.5000f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2560f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset carpetedHallway()
        {return new EAXReverbPreset(0.4287f, 1.0000f, 0.3162f, 0.0100f, 1.0000f, 0.3000f, 0.1000f, 1.0000f, 0.1215f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1531f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset hallway()
        {return new EAXReverbPreset(0.3645f, 1.0000f, 0.3162f, 0.7079f, 1.0000f, 1.4900f, 0.5900f, 1.0000f, 0.2458f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.6615f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset stoneCorridor()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.7612f, 1.0000f, 2.7000f, 0.7900f, 1.0000f, 0.2472f, 0.0130f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.5758f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset alley()
        {return new EAXReverbPreset(1.0000f, 0.3000f, 0.3162f, 0.7328f, 1.0000f, 1.4900f, 0.8600f, 1.0000f, 0.2500f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.9954f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1250f, 0.9500f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset forest()
        {return new EAXReverbPreset(1.0000f, 0.3000f, 0.3162f, 0.0224f, 1.0000f, 1.4900f, 0.5400f, 1.0000f, 0.0525f, 0.1620f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7682f, 0.0880f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1250f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset city()
        {return new EAXReverbPreset(1.0000f, 0.5000f, 0.3162f, 0.3981f, 1.0000f, 1.4900f, 0.6700f, 1.0000f, 0.0730f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1427f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset mountains()
        {return new EAXReverbPreset(1.0000f, 0.2700f, 0.3162f, 0.0562f, 1.0000f, 1.4900f, 0.2100f, 1.0000f, 0.0407f, 0.3000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1919f, 0.1000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset quarry()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.3162f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0000f, 0.0610f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.7783f, 0.0250f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1250f, 0.7000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset plain()
        {return new EAXReverbPreset(1.0000f, 0.2100f, 0.3162f, 0.1000f, 1.0000f, 1.4900f, 0.5000f, 1.0000f, 0.0585f, 0.1790f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1089f, 0.1000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset parkingLot()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 1.0000f, 1.0000f, 1.6500f, 1.5000f, 1.0000f, 0.2082f, 0.0080f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2652f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset sewerPipe()
        {return new EAXReverbPreset(0.3071f, 0.8000f, 0.3162f, 0.3162f, 1.0000f, 2.8100f, 0.1400f, 1.0000f, 1.6387f, 0.0140f, new Vec3(0.0000f, 0.0000f, 0.0000f), 3.2471f, 0.0210f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset underwater()
        {return new EAXReverbPreset(0.3645f, 1.0000f, 0.3162f, 0.0100f, 1.0000f, 1.4900f, 0.1000f, 1.0000f, 0.5963f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 7.0795f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 1.1800f, 0.3480f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset drugged()
        {return new EAXReverbPreset(0.4287f, 0.5000f, 0.3162f, 1.0000f, 1.0000f, 8.3900f, 1.3900f, 1.0000f, 0.8760f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 3.1081f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 1.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset dizzy()
        {return new EAXReverbPreset(0.3645f, 0.6000f, 0.3162f, 0.6310f, 1.0000f, 17.2300f, 0.5600f, 1.0000f, 0.1392f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.4937f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.8100f, 0.3100f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset psychotic()
        {return new EAXReverbPreset(0.0625f, 0.5000f, 0.3162f, 0.8404f, 1.0000f, 7.5600f, 0.9100f, 1.0000f, 0.4864f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 2.4378f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 4.0000f, 1.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}

    //Castle presets

    public static EAXReverbPreset castleSmallRoom()
        {return new EAXReverbPreset(1.0000f, 0.8900f, 0.3162f, 0.3981f, 0.1000f, 1.2200f, 0.8300f, 0.3100f, 0.8913f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.9953f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1380f, 0.0800f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset castleShortPassage()
        {return new EAXReverbPreset(1.0000f, 0.8900f, 0.3162f, 0.3162f, 0.1000f, 2.3200f, 0.8300f, 0.3100f, 0.8913f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0230f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1380f, 0.0800f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset castleMediumRoom()
        {return new EAXReverbPreset(1.0000f, 0.9300f, 0.3162f, 0.2818f, 0.1000f, 2.0400f, 0.8300f, 0.4600f, 0.6310f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.5849f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1550f, 0.0300f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset castleLargeRoom()
        {return new EAXReverbPreset(1.0000f, 0.8200f, 0.3162f, 0.2818f, 0.1259f, 2.5300f, 0.8300f, 0.5000f, 0.4467f, 0.0340f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0160f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1850f, 0.0700f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset castleLongPassage()
        {return new EAXReverbPreset(1.0000f, 0.8900f, 0.3162f, 0.3981f, 0.1000f, 3.4200f, 0.8300f, 0.3100f, 0.8913f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0230f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1380f, 0.0800f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset castleHall()
        {return new EAXReverbPreset(1.0000f, 0.8100f, 0.3162f, 0.2818f, 0.1778f, 3.1400f, 0.7900f, 0.6200f, 0.1778f, 0.0560f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0240f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset castleCupboard()
        {return new EAXReverbPreset(1.0000f, 0.8900f, 0.3162f, 0.2818f, 0.1000f, 0.6700f, 0.8700f, 0.3100f, 1.4125f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 3.5481f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1380f, 0.0800f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset castleCourtyard()
        {return new EAXReverbPreset(1.0000f, 0.4200f, 0.3162f, 0.4467f, 0.1995f, 2.1300f, 0.6100f, 0.2300f, 0.2239f, 0.1600f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7079f, 0.0360f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.3700f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset castleAlcove()
        {return new EAXReverbPreset(1.0000f, 0.8900f, 0.3162f, 0.5012f, 0.1000f, 1.6400f, 0.8700f, 0.3100f, 1.0000f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0340f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1380f, 0.0800f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1);}

    //Factory presets

    public static EAXReverbPreset factorySmallRoom()
        {return new EAXReverbPreset(0.3645f, 0.8200f, 0.3162f, 0.7943f, 0.5012f, 1.7200f, 0.6500f, 1.3100f, 0.7079f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.7783f, 0.0240f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1190f, 0.0700f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryShortPassage()
        {return new EAXReverbPreset(0.3645f, 0.6400f, 0.2512f, 0.7943f, 0.5012f, 2.5300f, 0.6500f, 1.3100f, 1.0000f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0380f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1350f, 0.2300f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryMediumRoom()
        {return new EAXReverbPreset(0.4287f, 0.8200f, 0.2512f, 0.7943f, 0.5012f, 2.7600f, 0.6500f, 1.3100f, 0.2818f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0230f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1740f, 0.0700f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryLargeRoom()
        {return new EAXReverbPreset(0.4287f, 0.7500f, 0.2512f, 0.7079f, 0.6310f, 4.2400f, 0.5100f, 1.3100f, 0.1778f, 0.0390f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0230f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2310f, 0.0700f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryLongPassage()
        {return new EAXReverbPreset(0.3645f, 0.6400f, 0.2512f, 0.7943f, 0.5012f, 4.0600f, 0.6500f, 1.3100f, 1.0000f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0370f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1350f, 0.2300f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryHall()
        {return new EAXReverbPreset(0.4287f, 0.7500f, 0.3162f, 0.7079f, 0.6310f, 7.4300f, 0.5100f, 1.3100f, 0.0631f, 0.0730f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.8913f, 0.0270f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0700f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryCupboard()
        {return new EAXReverbPreset(0.3071f, 0.6300f, 0.2512f, 0.7943f, 0.5012f, 0.4900f, 0.6500f, 1.3100f, 1.2589f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.9953f, 0.0320f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1070f, 0.0700f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryCourtyard()
        {return new EAXReverbPreset(0.3071f, 0.5700f, 0.3162f, 0.3162f, 0.6310f, 2.3200f, 0.2900f, 0.5600f, 0.2239f, 0.1400f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.3981f, 0.0390f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.2900f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}
    public static EAXReverbPreset factoryAlcove()
        {return new EAXReverbPreset(0.3645f, 0.5900f, 0.2512f, 0.7943f, 0.5012f, 3.1400f, 0.6500f, 1.3100f, 1.4125f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0000f, 0.0380f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1140f, 0.1000f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1);}

    //Ice palace presets

    public static EAXReverbPreset icePalaceSmallRoom()
        {return new EAXReverbPreset(1.0000f, 0.8400f, 0.3162f, 0.5623f, 0.2818f, 1.5100f, 1.5300f, 0.2700f, 0.8913f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1640f, 0.1400f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceShortPassage()
        {return new EAXReverbPreset(1.0000f, 0.7500f, 0.3162f, 0.5623f, 0.2818f, 1.7900f, 1.4600f, 0.2800f, 0.5012f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0190f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1770f, 0.0900f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceMediumRoom()
        {return new EAXReverbPreset(1.0000f, 0.8700f, 0.3162f, 0.5623f, 0.4467f, 2.2200f, 1.5300f, 0.3200f, 0.3981f, 0.0390f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0270f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1860f, 0.1200f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceLargeRoom()
        {return new EAXReverbPreset(1.0000f, 0.8100f, 0.3162f, 0.5623f, 0.4467f, 3.1400f, 1.5300f, 0.3200f, 0.2512f, 0.0390f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0000f, 0.0270f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2140f, 0.1100f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceLongPassage()
        {return new EAXReverbPreset(1.0000f, 0.7700f, 0.3162f, 0.5623f, 0.3981f, 3.0100f, 1.4600f, 0.2800f, 0.7943f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0250f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1860f, 0.0400f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceHall()
        {return new EAXReverbPreset(1.0000f, 0.7600f, 0.3162f, 0.4467f, 0.5623f, 5.4900f, 1.5300f, 0.3800f, 0.1122f, 0.0540f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.6310f, 0.0520f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2260f, 0.1100f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceCupboard()
        {return new EAXReverbPreset(1.0000f, 0.8300f, 0.3162f, 0.5012f, 0.2239f, 0.7600f, 1.5300f, 0.2600f, 1.1220f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.9953f, 0.0160f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1430f, 0.0800f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceCourtyard()
        {return new EAXReverbPreset(1.0000f, 0.5900f, 0.3162f, 0.2818f, 0.3162f, 2.0400f, 1.2000f, 0.3800f, 0.3162f, 0.1730f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.3162f, 0.0430f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2350f, 0.4800f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset icePalaceAlcove()
        {return new EAXReverbPreset(1.0000f, 0.8400f, 0.3162f, 0.5623f, 0.2818f, 2.7600f, 1.4600f, 0.2800f, 1.1220f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.8913f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1610f, 0.0900f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1);}

    //Space station presets

    public static EAXReverbPreset spaceStationSmallRoom()
        {return new EAXReverbPreset(0.2109f, 0.7000f, 0.3162f, 0.7079f, 0.8913f, 1.7200f, 0.8200f, 0.5500f, 0.7943f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0130f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1880f, 0.2600f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset spaceStationShortPassage()
        {return new EAXReverbPreset(0.2109f, 0.8700f, 0.3162f, 0.6310f, 0.8913f, 3.5700f, 0.5000f, 0.5500f, 1.0000f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0160f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1720f, 0.2000f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset spaceStationMediumRoom()
        {return new EAXReverbPreset(0.2109f, 0.7500f, 0.3162f, 0.6310f, 0.8913f, 3.0100f, 0.5000f, 0.5500f, 0.3981f, 0.0340f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0350f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2090f, 0.3100f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset spaceStationLargeRoom()
        {return new EAXReverbPreset(0.3645f, 0.8100f, 0.3162f, 0.6310f, 0.8913f, 3.8900f, 0.3800f, 0.6100f, 0.3162f, 0.0560f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.8913f, 0.0350f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2330f, 0.2800f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset spaceStationLongPassage()
        {return new EAXReverbPreset(0.4287f, 0.8200f, 0.3162f, 0.6310f, 0.8913f, 4.6200f, 0.6200f, 0.5500f, 1.0000f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0310f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.2300f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset spaceStationHall()
        {return new EAXReverbPreset(0.4287f, 0.8700f, 0.3162f, 0.6310f, 0.8913f, 7.1100f, 0.3800f, 0.6100f, 0.1778f, 0.1000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.6310f, 0.0470f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.2500f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset spaceStationCupboard()
        {return new EAXReverbPreset(0.1715f, 0.5600f, 0.3162f, 0.7079f, 0.8913f, 0.7900f, 0.8100f, 0.5500f, 1.4125f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.7783f, 0.0180f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1810f, 0.3100f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset spaceStationAlcove()
        {return new EAXReverbPreset(0.2109f, 0.7800f, 0.3162f, 0.7079f, 0.8913f, 1.1600f, 0.8100f, 0.5500f, 1.4125f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0000f, 0.0180f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1920f, 0.2100f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1);}

    //Wooden galleon presets

    public static EAXReverbPreset woodenSmallRoom()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.1122f, 0.3162f, 0.7900f, 0.3200f, 0.8700f, 1.0000f, 0.0320f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.8913f, 0.0290f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenShortPassage()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.1259f, 0.3162f, 1.7500f, 0.5000f, 0.8700f, 0.8913f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.6310f, 0.0240f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenMediumRoom()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.1000f, 0.2818f, 1.4700f, 0.4200f, 0.8200f, 0.8913f, 0.0490f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.8913f, 0.0290f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenLargeRoom()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.0891f, 0.2818f, 2.6500f, 0.3300f, 0.8200f, 0.8913f, 0.0660f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7943f, 0.0490f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenLongPassage()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.1000f, 0.3162f, 1.9900f, 0.4000f, 0.7900f, 1.0000f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.4467f, 0.0360f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenHall()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.0794f, 0.2818f, 3.4500f, 0.3000f, 0.8200f, 0.8913f, 0.0880f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7943f, 0.0630f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenCupboard()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.1413f, 0.3162f, 0.5600f, 0.4600f, 0.9100f, 1.1220f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0280f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenCourtyard()
        {return new EAXReverbPreset(1.0000f, 0.6500f, 0.3162f, 0.0794f, 0.3162f, 1.7900f, 0.3500f, 0.7900f, 0.5623f, 0.1230f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1000f, 0.0320f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}
    public static EAXReverbPreset woodenAlcove()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.1259f, 0.3162f, 1.2200f, 0.6200f, 0.9100f, 1.1220f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7079f, 0.0240f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 4705.0000f, 99.6000f, 0.0000f, 0x1);}

    //Sports presets

    public static EAXReverbPreset sportEmptyStadium()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.4467f, 0.7943f, 6.2600f, 0.5100f, 1.1000f, 0.0631f, 0.1830f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.3981f, 0.0380f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset sportSquashCourt()
        {return new EAXReverbPreset(1.0000f, 0.7500f, 0.3162f, 0.3162f, 0.7943f, 2.2200f, 0.9100f, 1.1600f, 0.4467f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7943f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1260f, 0.1900f, 0.2500f, 0.0000f, 0.9943f, 7176.8999f, 211.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset sportSmallSwimmingPool()
        {return new EAXReverbPreset(1.0000f, 0.7000f, 0.3162f, 0.7943f, 0.8913f, 2.7600f, 1.2500f, 1.1400f, 0.6310f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7943f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1790f, 0.1500f, 0.8950f, 0.1900f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset sportLargeSwimmingPool()
        {return new EAXReverbPreset(1.0000f, 0.8200f, 0.3162f, 0.7943f, 1.0000f, 5.4900f, 1.3100f, 1.1400f, 0.4467f, 0.0390f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.5012f, 0.0490f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2220f, 0.5500f, 1.1590f, 0.2100f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset sportGymnasium()
        {return new EAXReverbPreset(1.0000f, 0.8100f, 0.3162f, 0.4467f, 0.8913f, 3.1400f, 1.0600f, 1.3500f, 0.3981f, 0.0290f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.5623f, 0.0450f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1460f, 0.1400f, 0.2500f, 0.0000f, 0.9943f, 7176.8999f, 211.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset sportFullStadium()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.0708f, 0.7943f, 5.2500f, 0.1700f, 0.8000f, 0.1000f, 0.1880f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2818f, 0.0380f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset sportStadiumTannoy()
        {return new EAXReverbPreset(1.0000f, 0.7800f, 0.3162f, 0.5623f, 0.5012f, 2.5300f, 0.8800f, 0.6800f, 0.2818f, 0.2300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.5012f, 0.0630f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}

    //Prefab presets

    public static EAXReverbPreset prefabWorkshop()
        {return new EAXReverbPreset(0.4287f, 1.0000f, 0.3162f, 0.1413f, 0.3981f, 0.7600f, 1.0000f, 1.0000f, 1.0000f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset prefabSchoolRoom()
        {return new EAXReverbPreset(0.4022f, 0.6900f, 0.3162f, 0.6310f, 0.5012f, 0.9800f, 0.4500f, 0.1800f, 1.4125f, 0.0170f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0150f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.0950f, 0.1400f, 0.2500f, 0.0000f, 0.9943f, 7176.8999f, 211.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset prefabPracticeRoom()
        {return new EAXReverbPreset(0.4022f, 0.8700f, 0.3162f, 0.3981f, 0.5012f, 1.1200f, 0.5600f, 0.1800f, 1.2589f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.0950f, 0.1400f, 0.2500f, 0.0000f, 0.9943f, 7176.8999f, 211.2000f, 0.0000f, 0x1);}
    public static EAXReverbPreset prefabOuthouse()
        {return new EAXReverbPreset(1.0000f, 0.8200f, 0.3162f, 0.1122f, 0.1585f, 1.3800f, 0.3800f, 0.3500f, 0.8913f, 0.0240f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.6310f, 0.0440f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1210f, 0.1700f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 107.5000f, 0.0000f, 0x0);}
    public static EAXReverbPreset prefabCaravan()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.0891f, 0.1259f, 0.4300f, 1.5000f, 1.0000f, 1.0000f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.9953f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}

    //Dome and pipe resets

    public static EAXReverbPreset domeTomb()
        {return new EAXReverbPreset(1.0000f, 0.7900f, 0.3162f, 0.3548f, 0.2239f, 4.1800f, 0.2100f, 0.1000f, 0.3868f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.6788f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1770f, 0.1900f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 20.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset pipeSmall()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.3548f, 0.2239f, 5.0400f, 0.1000f, 0.1000f, 0.5012f, 0.0320f, new Vec3(0.0000f, 0.0000f, 0.0000f), 2.5119f, 0.0150f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 20.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset domeSaintPauls()
        {return new EAXReverbPreset(1.0000f, 0.8700f, 0.3162f, 0.3548f, 0.2239f, 10.4800f, 0.1900f, 0.1000f, 0.1778f, 0.0900f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0420f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.1200f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 20.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset pipeLongThin()
        {return new EAXReverbPreset(0.2560f, 0.9100f, 0.3162f, 0.4467f, 0.2818f, 9.2100f, 0.1800f, 0.1000f, 0.7079f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7079f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 20.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset pipeLarge()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.3548f, 0.2239f, 8.4500f, 0.1000f, 0.1000f, 0.3981f, 0.0460f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.5849f, 0.0320f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 20.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset pipeResonant()
        {return new EAXReverbPreset(0.1373f, 0.9100f, 0.3162f, 0.4467f, 0.2818f, 6.8100f, 0.1800f, 0.1000f, 0.7079f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0000f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 20.0000f, 0.0000f, 0x0);}

    //Outdoor presets

    public static EAXReverbPreset outdoorsBackyard()
        {return new EAXReverbPreset(1.0000f, 0.4500f, 0.3162f, 0.2512f, 0.5012f, 1.1200f, 0.3400f, 0.4600f, 0.4467f, 0.0690f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.7079f, 0.0230f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2180f, 0.3400f, 0.2500f, 0.0000f, 0.9943f, 4399.1001f, 242.9000f, 0.0000f, 0x0);}
    public static EAXReverbPreset outdoorsRollingPlains()
        {return new EAXReverbPreset(1.0000f, 0.0000f, 0.3162f, 0.0112f, 0.6310f, 2.1300f, 0.2100f, 0.4600f, 0.1778f, 0.3000f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.4467f, 0.0190f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 4399.1001f, 242.9000f, 0.0000f, 0x0);}
    public static EAXReverbPreset outdoorsDeepCanyon()
        {return new EAXReverbPreset(1.0000f, 0.7400f, 0.3162f, 0.1778f, 0.6310f, 3.8900f, 0.2100f, 0.4600f, 0.3162f, 0.2230f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.3548f, 0.0190f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 4399.1001f, 242.9000f, 0.0000f, 0x0);}
    public static EAXReverbPreset outdoorsCreek()
        {return new EAXReverbPreset(1.0000f, 0.3500f, 0.3162f, 0.1778f, 0.5012f, 2.1300f, 0.2100f, 0.4600f, 0.3981f, 0.1150f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.1995f, 0.0310f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2180f, 0.3400f, 0.2500f, 0.0000f, 0.9943f, 4399.1001f, 242.9000f, 0.0000f, 0x0);}
    public static EAXReverbPreset outdoorsValley()
        {return new EAXReverbPreset(1.0000f, 0.2800f, 0.3162f, 0.0282f, 0.1585f, 2.8800f, 0.2600f, 0.3500f, 0.1413f, 0.2630f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.3981f, 0.1000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.3400f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 107.5000f, 0.0000f, 0x0);}

    //Mood presets

    public static EAXReverbPreset moodHeaven()
        {return new EAXReverbPreset(1.0000f, 0.9400f, 0.3162f, 0.7943f, 0.4467f, 5.0400f, 1.1200f, 0.5600f, 0.2427f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0290f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0800f, 2.7420f, 0.0500f, 0.9977f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset moodHell()
        {return new EAXReverbPreset(1.0000f, 0.5700f, 0.3162f, 0.3548f, 0.4467f, 3.5700f, 0.4900f, 2.0000f, 0.0000f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1100f, 0.0400f, 2.1090f, 0.5200f, 0.9943f, 5000.0000f, 139.5000f, 0.0000f, 0x0);}
    public static EAXReverbPreset moodMemory()
        {return new EAXReverbPreset(1.0000f, 0.8500f, 0.3162f, 0.6310f, 0.3548f, 4.0600f, 0.8200f, 0.5600f, 0.0398f, 0.0000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1220f, 0.0000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.4740f, 0.4500f, 0.9886f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}

    //Driving presets

    public static EAXReverbPreset drivingCommentator()
        {return new EAXReverbPreset(1.0000f, 0.0000f, 0.3162f, 0.5623f, 0.5012f, 2.4200f, 0.8800f, 0.6800f, 0.1995f, 0.0930f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2512f, 0.0170f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.2500f, 0.0000f, 0.9886f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset drivingPitGarage()
        {return new EAXReverbPreset(0.4287f, 0.5900f, 0.3162f, 0.7079f, 0.5623f, 1.7200f, 0.9300f, 0.8700f, 0.5623f, 0.0000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0160f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.1100f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset drivingInCarRacer()
        {return new EAXReverbPreset(0.0832f, 0.8000f, 0.3162f, 1.0000f, 0.7943f, 0.1700f, 2.0000f, 0.4100f, 1.7783f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7079f, 0.0150f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 10268.2002f, 251.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset drivingInCarSports()
        {return new EAXReverbPreset(0.0832f, 0.8000f, 0.3162f, 0.6310f, 1.0000f, 0.1700f, 0.7500f, 0.4100f, 1.0000f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.5623f, 0.0000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 10268.2002f, 251.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset drivingInCarLuxury()
        {return new EAXReverbPreset(0.2560f, 1.0000f, 0.3162f, 0.1000f, 0.5012f, 0.1300f, 0.4100f, 0.4600f, 0.7943f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.5849f, 0.0100f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 10268.2002f, 251.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset drivingFullGrandStand()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 0.2818f, 0.6310f, 3.0100f, 1.3700f, 1.2800f, 0.3548f, 0.0900f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1778f, 0.0490f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 10420.2002f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset drivingEmptyGrandStand()
        {return new EAXReverbPreset(1.0000f, 1.0000f, 0.3162f, 1.0000f, 0.7943f, 4.6200f, 1.7500f, 1.4000f, 0.2082f, 0.0900f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2512f, 0.0490f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 10420.2002f, 250.0000f, 0.0000f, 0x0);}
    public static EAXReverbPreset drivingTunnel()
        {return new EAXReverbPreset(1.0000f, 0.8100f, 0.3162f, 0.3981f, 0.8913f, 3.4200f, 0.9400f, 1.3100f, 0.7079f, 0.0510f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7079f, 0.0470f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2140f, 0.0500f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 155.3000f, 0.0000f, 0x1);}

    //City presets

    public static EAXReverbPreset cityStreets()
        {return new EAXReverbPreset(1.0000f, 0.7800f, 0.3162f, 0.7079f, 0.8913f, 1.7900f, 1.1200f, 0.9100f, 0.2818f, 0.0460f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1995f, 0.0280f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset citySubway()
        {return new EAXReverbPreset(1.0000f, 0.7400f, 0.3162f, 0.7079f, 0.8913f, 3.0100f, 1.2300f, 0.9100f, 0.7079f, 0.0460f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0280f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1250f, 0.2100f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset cityMuseum()
        {return new EAXReverbPreset(1.0000f, 0.8200f, 0.3162f, 0.1778f, 0.1778f, 3.2800f, 1.4000f, 0.5700f, 0.2512f, 0.0390f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.8913f, 0.0340f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1300f, 0.1700f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 107.5000f, 0.0000f, 0x0);}
    public static EAXReverbPreset cityLibrary()
        {return new EAXReverbPreset(1.0000f, 0.8200f, 0.3162f, 0.2818f, 0.0891f, 2.7600f, 0.8900f, 0.4100f, 0.3548f, 0.0290f, new Vec3(0.0000f, 0.0000f, -0.0000f), 0.8913f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1300f, 0.1700f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 107.5000f, 0.0000f, 0x0);}
    public static EAXReverbPreset cityUnderpass()
        {return new EAXReverbPreset(1.0000f, 0.8200f, 0.3162f, 0.4467f, 0.8913f, 3.5700f, 1.1200f, 0.9100f, 0.3981f, 0.0590f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.8913f, 0.0370f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.1400f, 0.2500f, 0.0000f, 0.9920f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset cityAbandoned()
        {return new EAXReverbPreset(1.0000f, 0.6900f, 0.3162f, 0.7943f, 0.8913f, 3.2800f, 1.1700f, 0.9100f, 0.4467f, 0.0440f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2818f, 0.0240f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9966f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}

    //Misc presets

    public static EAXReverbPreset dustyRoom()
        {return new EAXReverbPreset(0.3645f, 0.5600f, 0.3162f, 0.7943f, 0.7079f, 1.7900f, 0.3800f, 0.2100f, 0.5012f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0060f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2020f, 0.0500f, 0.2500f, 0.0000f, 0.9886f, 13046.0000f, 163.3000f, 0.0000f, 0x1);}
    public static EAXReverbPreset chapel()
        {return new EAXReverbPreset(1.0000f, 0.8400f, 0.3162f, 0.5623f, 1.0000f, 4.6200f, 0.6400f, 1.2300f, 0.4467f, 0.0320f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7943f, 0.0490f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.1100f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1);}
    public static EAXReverbPreset smallWaterRoom()
        {return new EAXReverbPreset(1.0000f, 0.7000f, 0.3162f, 0.4477f, 1.0000f, 1.5100f, 1.2500f, 1.1400f, 0.8913f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.4125f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1790f, 0.1500f, 0.8950f, 0.1900f, 0.9920f, 5000.0000f, 250.0000f, 0.0000f, 0x0);}
    
    public float density, diffusion;
    public float gain, gainHF, gainLF;
    public float decayTime, decayHFRatio, decayLFRatio;
    public float reflectionsGain, reflectionsDelay;
    public final Vec3 reflectionsPan = new Vec3();
    public float lateReverbGain, lateReverbDelay;
    public final Vec3 lateReverbPan = new Vec3();
    public float echoTime, echoDepth, modulationTime, modulationDepth;
    public float airAbsorptionGainHF, hfReference, lFReference;
    public float roomRolloffFactor;
    public int decayHFLimit;
    
    public EAXReverbPreset()
    {
    }
    
    public EAXReverbPreset(float density, float diffusion,
            float gain, float gainHF, float gainLF,
            float decayTime, float decayHFRatio, float decayLFRatio,
            float reflectionsGain, float reflectionsDelay, Vec3 reflectionsPan,
            float lateReverbGain, float lateReverbDelay, Vec3 lateReverbPan,
            float echoTime, float echoDepth, float modulationTime, float modulationDepth,
            float airAbsorptionGainHF, float hfReference, float lFReference,
            float roomRolloffFactor, int decayHFLimit)
    {
        this.density = density;
        this.diffusion = diffusion;
        this.gain = gain;
        this.gainHF = gainHF;
        this.gainLF = gainLF;
        this.decayTime = decayTime;
        this.decayHFRatio = decayHFRatio;
        this.decayLFRatio = decayLFRatio;
        this.reflectionsGain = reflectionsGain;
        this.reflectionsDelay = reflectionsDelay;
        this.reflectionsPan.set(reflectionsPan);
        this.lateReverbGain = lateReverbGain;
        this.lateReverbDelay = lateReverbDelay;
        this.lateReverbPan.set(lateReverbPan);
        this.echoTime = echoTime;
        this.echoDepth = echoDepth;
        this.modulationTime = modulationTime;
        this.modulationDepth = modulationDepth;
        this.airAbsorptionGainHF = airAbsorptionGainHF;
        this.hfReference = hfReference;
        this.lFReference = lFReference;
        this.roomRolloffFactor = roomRolloffFactor;
        this.decayHFLimit = decayHFLimit;
    }
    
    public EAXReverbPreset(EAXReverbPreset other)
    {
        set(other);
    }
    
    public EAXReverbPreset set(EAXReverbPreset other)
    {
        density = other.density;
        diffusion = other.diffusion;
        gain = other.gain;
        gainHF = other.gainHF;
        gainLF = other.gainLF;
        decayTime = other.decayTime;
        decayHFRatio = other.decayHFRatio;
        decayLFRatio = other.decayLFRatio;
        reflectionsGain = other.reflectionsGain;
        reflectionsDelay = other.reflectionsDelay;
        reflectionsPan.set(other.reflectionsPan);
        lateReverbGain = other.lateReverbGain;
        lateReverbDelay = other.lateReverbDelay;
        lateReverbPan.set(other.lateReverbPan);
        echoTime = other.echoTime;
        echoDepth = other.echoDepth;
        modulationTime = other.modulationTime;
        modulationDepth = other.modulationDepth;
        airAbsorptionGainHF = other.airAbsorptionGainHF;
        hfReference = other.hfReference;
        lFReference = other.lFReference;
        roomRolloffFactor = other.roomRolloffFactor;
        decayHFLimit = other.decayHFLimit;
        
        return this;
    }
    
    public EAXReverbPreset mix(EAXReverbPreset other, float t)
    {
        density = Util.lerp(density, other.density, t);
        diffusion = Util.lerp(diffusion, other.diffusion, t);
        gain = Util.lerp(gain, other.gain, t);
        gainHF = Util.lerp(gainHF, other.gainHF, t);
        gainLF = Util.lerp(gainLF, other.gainLF, t);
        decayTime = Util.lerp(decayTime, other.decayTime, t);
        decayHFRatio = Util.lerp(decayHFRatio, other.decayHFRatio, t);
        decayLFRatio = Util.lerp(decayLFRatio, other.decayLFRatio, t);
        reflectionsGain = Util.lerp(reflectionsGain, other.reflectionsGain, t);
        reflectionsDelay = Util.lerp(reflectionsDelay, other.reflectionsDelay, t);
        reflectionsPan.lerp(other.reflectionsPan, t);
        lateReverbGain = Util.lerp(lateReverbGain, other.lateReverbGain, t);
        lateReverbDelay = Util.lerp(lateReverbDelay, other.lateReverbDelay, t);
        lateReverbPan.lerp(other.lateReverbPan, t);
        echoTime = Util.lerp(echoTime, other.echoTime, t);
        echoDepth = Util.lerp(echoDepth, other.echoDepth, t);
        modulationTime = Util.lerp(modulationTime, other.modulationTime, t);
        modulationDepth = Util.lerp(modulationDepth, other.modulationDepth, t);
        airAbsorptionGainHF = Util.lerp(airAbsorptionGainHF, other.airAbsorptionGainHF, t);
        hfReference = Util.lerp(hfReference, other.hfReference, t);
        lFReference = Util.lerp(lFReference, other.lFReference, t);
        roomRolloffFactor = Util.lerp(roomRolloffFactor, other.roomRolloffFactor, t);
        decayHFLimit = Math.round(Util.lerp(decayHFLimit, other.decayHFLimit, t));
        
        return this;
    }
    
    public EAXReverbPreset apply(Effect effect)
    {
        effect.setType(EXTEfx.AL_EFFECT_EAXREVERB);
        effect.paramf(EXTEfx.AL_EAXREVERB_DENSITY, density);
        effect.paramf(EXTEfx.AL_EAXREVERB_DIFFUSION, diffusion);
        effect.paramf(EXTEfx.AL_EAXREVERB_GAIN, gain);
        effect.paramf(EXTEfx.AL_EAXREVERB_GAINHF, gainHF);
        effect.paramf(EXTEfx.AL_EAXREVERB_GAINLF, gainLF);
        effect.paramf(EXTEfx.AL_EAXREVERB_DECAY_TIME, decayTime);
        effect.paramf(EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, decayHFRatio);
        effect.paramf(EXTEfx.AL_EAXREVERB_DECAY_LFRATIO, decayLFRatio);
        effect.paramf(EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, reflectionsGain);
        effect.paramf(EXTEfx.AL_EAXREVERB_REFLECTIONS_DELAY, reflectionsDelay);
        effect.paramVec3(EXTEfx.AL_EAXREVERB_REFLECTIONS_PAN, reflectionsPan);
        effect.paramf(EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, lateReverbGain);
        effect.paramf(EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, lateReverbDelay);
        effect.paramVec3(EXTEfx.AL_EAXREVERB_LATE_REVERB_PAN, lateReverbPan);
        effect.paramf(EXTEfx.AL_EAXREVERB_ECHO_TIME, echoTime);
        effect.paramf(EXTEfx.AL_EAXREVERB_ECHO_DEPTH, echoDepth);
        effect.paramf(EXTEfx.AL_EAXREVERB_MODULATION_TIME, modulationTime);
        effect.paramf(EXTEfx.AL_EAXREVERB_MODULATION_DEPTH, modulationDepth);
        effect.paramf(EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, airAbsorptionGainHF);
        effect.paramf(EXTEfx.AL_EAXREVERB_HFREFERENCE, hfReference);
        effect.paramf(EXTEfx.AL_EAXREVERB_LFREFERENCE, lFReference);
        effect.paramf(EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, roomRolloffFactor);
        effect.parami(EXTEfx.AL_EAXREVERB_DECAY_HFLIMIT, decayHFLimit);
        
        return this;
    }
}
