package com.samrj.devil.sound.effects;

import static com.samrj.devil.buffer.PublicBuffers.fbuffer;
import com.samrj.devil.math.Vector3f;
import org.lwjgl.openal.EFX10;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class EAXReverb extends Reverb
{
    public float    gainLF              = EFX10.AL_EAXREVERB_DEFAULT_GAINLF                ,
                    decayLFRatio        = EFX10.AL_EAXREVERB_DEFAULT_DECAY_LFRATIO         ,
                    reflectionsGain     = EFX10.AL_EAXREVERB_DEFAULT_REFLECTIONS_GAIN      ,
                    reflectionsDelay    = EFX10.AL_EAXREVERB_DEFAULT_REFLECTIONS_DELAY     ,
                    echoTime            = EFX10.AL_EAXREVERB_DEFAULT_ECHO_TIME             ,
                    echoDepth           = EFX10.AL_EAXREVERB_DEFAULT_ECHO_DEPTH            ,
                    modulationTime      = EFX10.AL_EAXREVERB_DEFAULT_MODULATION_TIME       ,
                    modulationDepth     = EFX10.AL_EAXREVERB_DEFAULT_MODULATION_DEPTH      ,
                    hfReference         = EFX10.AL_EAXREVERB_DEFAULT_HFREFERENCE           ,
                    lfReference         = EFX10.AL_EAXREVERB_DEFAULT_LFREFERENCE           ;
    public Vector3f reflectionsPan      = new Vector3f(),
                    lateReverbPan       = new Vector3f();
    
    public EAXReverb()
    {
        super(EFX10.AL_EFFECT_EAXREVERB);
    }
    
    @Override
    public void setProps(int id)
    {
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_DENSITY               , density             );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_DIFFUSION             , diffusion           );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_GAIN                  , gain                );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_GAINHF                , gainHF              );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_GAINLF                , gainLF              );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_DECAY_TIME            , decayTime           );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_DECAY_HFRATIO         , decayHFRatio        );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_DECAY_LFRATIO         , decayLFRatio        );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_REFLECTIONS_GAIN      , reflectionsGain     );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_REFLECTIONS_DELAY     , reflectionsDelay    );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_LATE_REVERB_GAIN      , lateReverbGain      );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_LATE_REVERB_DELAY     , lateReverbDelay     );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_ECHO_TIME             , echoTime            );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_ECHO_DEPTH            , echoDepth           );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_MODULATION_TIME       , modulationTime      );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_MODULATION_DEPTH      , modulationDepth     );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_AIR_ABSORPTION_GAINHF , airAbsorptionGainHF );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_HFREFERENCE           , hfReference         );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_LFREFERENCE           , lfReference         );
        EFX10.alEffectf(id, EFX10.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR   , roomRolloffFactor   );
        EFX10.alEffecti(id, EFX10.AL_EAXREVERB_DECAY_HFLIMIT         , decayHFLimit        );
        fbuffer.clear();
        reflectionsPan.putIn(fbuffer);
        fbuffer.rewind();
        EFX10.alEffect (id, EFX10.AL_EAXREVERB_REFLECTIONS_PAN, fbuffer);
        fbuffer.clear();
        lateReverbPan.putIn(fbuffer);
        fbuffer.rewind();
        EFX10.alEffect (id, EFX10.AL_EAXREVERB_LATE_REVERB_PAN, fbuffer);
    }
}
