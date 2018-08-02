package Audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Metronome {

    private Mixer mixer;   // main mixer
    private File sound;
    private DataLine.Info info;
    private AudioInputStream ais;
    private Mixer.Info [] mixInfos;
    private String filename;
    private Clip clip;
    private boolean reset;

    public Metronome() throws UnsupportedAudioFileException, IOException, LineUnavailableException{

        setUpMetronome();
    }

    public void setUpMetronome()  throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        mixInfos = AudioSystem.getMixerInfo();
        mixer = AudioSystem.getMixer(mixInfos[0]);

        filename = "/Users/ivaannagen/Documents/Samples/Samplephonics_-_LofiCuts/TCCLofiCuts_Wav_SP/One Shots/Drums/Clicks and FX/120_RasterClick_SP_02.wav";
        sound = new File(filename);
        ais = AudioSystem.getAudioInputStream(sound);
        AudioFormat format = ais.getFormat();
        info = new DataLine.Info(Clip.class, format);
        clip = (Clip)AudioSystem.getLine(info);
        clip.open(ais);
        reset = false;
    }

    public void play(float bpm) throws LineUnavailableException, IOException {

            clip.start();

    }

    public void stop () throws LineUnavailableException, IOException {

        clip.stop();
        clip.flush();
        clip.drain();
        clip.close();
        clip.open(ais);
    }

}
