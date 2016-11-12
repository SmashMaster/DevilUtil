package com.samrj.devil.al;

import com.samrj.devil.math.Vec3;
import org.lwjgl.openal.EXTEfx;

public enum EAXReverbPreset
{
    GENERIC         (1.0000f, 1.0000f, 0.3162f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2589f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    PADDEDCELL      (0.1715f, 1.0000f, 0.3162f, 0.0010f, 1.0000f, 0.1700f, 0.1000f, 1.0000f, 0.2500f, 0.0010f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2691f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    ROOM            (0.4287f, 1.0000f, 0.3162f, 0.5929f, 1.0000f, 0.4000f, 0.8300f, 1.0000f, 0.1503f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0629f, 0.0030f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    BATHROOM        (0.1715f, 1.0000f, 0.3162f, 0.2512f, 1.0000f, 1.4900f, 0.5400f, 1.0000f, 0.6531f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 3.2734f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    LIVINGROOM      (0.9766f, 1.0000f, 0.3162f, 0.0010f, 1.0000f, 0.5000f, 0.1000f, 1.0000f, 0.2051f, 0.0030f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2805f, 0.0040f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    STONEROOM       (1.0000f, 1.0000f, 0.3162f, 0.7079f, 1.0000f, 2.3100f, 0.6400f, 1.0000f, 0.4411f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.1003f, 0.0170f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    AUDITORIUM      (1.0000f, 1.0000f, 0.3162f, 0.5781f, 1.0000f, 4.3200f, 0.5900f, 1.0000f, 0.4032f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7170f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    CONCERTHALL     (1.0000f, 1.0000f, 0.3162f, 0.5623f, 1.0000f, 3.9200f, 0.7000f, 1.0000f, 0.2427f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.9977f, 0.0290f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    CAVE            (1.0000f, 1.0000f, 0.3162f, 1.0000f, 1.0000f, 2.9100f, 1.3000f, 1.0000f, 0.5000f, 0.0150f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7063f, 0.0220f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0),
    ARENA           (1.0000f, 1.0000f, 0.3162f, 0.4477f, 1.0000f, 7.2400f, 0.3300f, 1.0000f, 0.2612f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.0186f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    HANGAR          (1.0000f, 1.0000f, 0.3162f, 0.3162f, 1.0000f, 10.050f, 0.2300f, 1.0000f, 0.5000f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.2560f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    CARPETEDHALLWAY (0.4287f, 1.0000f, 0.3162f, 0.0100f, 1.0000f, 0.3000f, 0.1000f, 1.0000f, 0.1215f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1531f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    HALLWAY         (0.3645f, 1.0000f, 0.3162f, 0.7079f, 1.0000f, 1.4900f, 0.5900f, 1.0000f, 0.2458f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.6615f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    STONECORRIDOR   (1.0000f, 1.0000f, 0.3162f, 0.7612f, 1.0000f, 2.7000f, 0.7900f, 1.0000f, 0.2472f, 0.0130f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.5758f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    ALLEY           (1.0000f, 0.3000f, 0.3162f, 0.7328f, 1.0000f, 1.4900f, 0.8600f, 1.0000f, 0.2500f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.9954f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1250f, 0.9500f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    FOREST          (1.0000f, 0.3000f, 0.3162f, 0.0224f, 1.0000f, 1.4900f, 0.5400f, 1.0000f, 0.0525f, 0.1620f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.7682f, 0.0880f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1250f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    CITY            (1.0000f, 0.5000f, 0.3162f, 0.3981f, 1.0000f, 1.4900f, 0.6700f, 1.0000f, 0.0730f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1427f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    MOUNTAINS       (1.0000f, 0.2700f, 0.3162f, 0.0562f, 1.0000f, 1.4900f, 0.2100f, 1.0000f, 0.0407f, 0.3000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1919f, 0.1000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0),
    QUARRY          (1.0000f, 1.0000f, 0.3162f, 0.3162f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0000f, 0.0610f, new Vec3(0.0000f, 0.0000f, 0.0000f), 1.7783f, 0.0250f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1250f, 0.7000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    PLAIN           (1.0000f, 0.2100f, 0.3162f, 0.1000f, 1.0000f, 1.4900f, 0.5000f, 1.0000f, 0.0585f, 0.1790f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.1089f, 0.1000f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    PARKINGLOT      (1.0000f, 1.0000f, 0.3162f, 1.0000f, 1.0000f, 1.6500f, 1.5000f, 1.0000f, 0.2082f, 0.0080f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2652f, 0.0120f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0),
    SEWERPIPE       (0.3071f, 0.8000f, 0.3162f, 0.3162f, 1.0000f, 2.8100f, 0.1400f, 1.0000f, 1.6387f, 0.0140f, new Vec3(0.0000f, 0.0000f, 0.0000f), 3.2471f, 0.0210f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    UNDERWATER      (0.3645f, 1.0000f, 0.3162f, 0.0100f, 1.0000f, 1.4900f, 0.1000f, 1.0000f, 0.5963f, 0.0070f, new Vec3(0.0000f, 0.0000f, 0.0000f), 7.0795f, 0.0110f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 1.1800f, 0.3480f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1),
    DRUGGED         (0.4287f, 0.5000f, 0.3162f, 1.0000f, 1.0000f, 8.3900f, 1.3900f, 1.0000f, 0.8760f, 0.0020f, new Vec3(0.0000f, 0.0000f, 0.0000f), 3.1081f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 0.2500f, 1.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0),
    DIZZY           (0.3645f, 0.6000f, 0.3162f, 0.6310f, 1.0000f, 17.230f, 0.5600f, 1.0000f, 0.1392f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.4937f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 1.0000f, 0.8100f, 0.3100f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0),
    PSYCHOTIC       (0.0625f, 0.5000f, 0.3162f, 0.8404f, 1.0000f, 7.5600f, 0.9100f, 1.0000f, 0.4864f, 0.0200f, new Vec3(0.0000f, 0.0000f, 0.0000f), 2.4378f, 0.0300f, new Vec3(0.0000f, 0.0000f, 0.0000f), 0.2500f, 0.0000f, 4.0000f, 1.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0);
    
    private final float density, diffusion;
    private final float gain, gainHF, gainLF;
    private final float decayTime, decayHFRatio, decayLFRatio;
    private final float reflectionsGain, reflectionsDelay;
    private final Vec3 reflectionsPan;
    private final float lateReverbGain, lateReverbDelay;
    private final Vec3 lateReverbPan;
    private final float echoTime, echoDepth, modulationTime, modulationDepth;
    private final float airAbsorptionGainHF, hfReference, lFReference;
    private final float roomRolloffFactor;
    private final int decayHFLimit;
    
    private EAXReverbPreset(float density, float diffusion,
            float gain, float gainHF, float gainLF,
            float decayTime, float decayHFRatio, float decayLFRatio,
            float reflectionsGain, float reflectionsDelay, Vec3 reflectionsPan,
            float lateReverbGain, float lateReverbDelay, Vec3 lateReverbPan,
            float echoTime, float echoDepth, float modulationTime, float modulationDepth,
            float airAbsorptionGainHF, float hfReference, float lFReference,
            float roomRolloffFactor, int decayHFLimit)
    {
        this.density = density; this.diffusion = diffusion;
        this.gain = gain; this.gainHF = gainHF; this.gainLF = gainLF;
        this.decayTime = decayTime; this.decayHFRatio = decayHFRatio; this.decayLFRatio = decayLFRatio;
        this.reflectionsGain = reflectionsGain; this.reflectionsDelay = reflectionsDelay; this.reflectionsPan = reflectionsPan;
        this.lateReverbGain = lateReverbGain; this.lateReverbDelay = lateReverbDelay; this.lateReverbPan = lateReverbPan;
        this.echoTime = echoTime; this.echoDepth = echoDepth; this.modulationTime = modulationTime; this.modulationDepth = modulationDepth;
        this.airAbsorptionGainHF = airAbsorptionGainHF; this.hfReference = hfReference; this.lFReference = lFReference;
        this.roomRolloffFactor = roomRolloffFactor; this.decayHFLimit = decayHFLimit;
    }
    
    public void apply(Effect effect)
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
    }
}
