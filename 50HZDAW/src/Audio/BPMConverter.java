package Audio;

public class BPMConverter {

    private int bpm;
    private int baseValue;

    public BPMConverter () {

        baseValue = 60000; // 1 minute

    }

    /**
     * Set millisecond value for bars according to BPM.
     * Return milliseconds per bar.
     */

    public double setBars (int bars, int myBPM) {
        this.bpm = myBPM;
        double bar = (baseValue / bpm) * 4;
        return (bars) * bar;
    }

    /**
     * Set millisecond value for half a bar according to BPM.
     * Return milliseconds per half a bar.
     */

    public double setHalfBar (int half) {
        double halfBar = (baseValue / bpm) * 2;
        return (half) * halfBar;
    }

    /**
     * Set millisecond value for beats according to BPM.
     * Return milliseconds per beat.
     */

    public double setBeat (int beats) {
        double beat = baseValue / bpm;
        return (beats) * beat;
    }

    /**
     * Set millisecond value for beats according to BPM.
     * Return milliseconds per eight beat.
     */

    public double setHalfBeat (int halfBeats) {
        double half = (baseValue / bpm) / 2;
        return (halfBeats) * half;
    }

    /**
     * Set millisecond value for quarter beats according to BPM.
     * Return milliseconds per quarter beat.
     */

    public double setQuarterBeat (int quarterBeats) {
        double quarter = (baseValue / bpm) / 4;
        return (quarterBeats) * quarter;
    }

    /**
     * Set millisecond value for quarter beats according to BPM.
     * Return milliseconds per quarter beat.
     */

    public int setEighthBeat (int eighthBeats) {
        int eighth = (baseValue / bpm) / 8;
        return (eighthBeats) * eighth;
    }

    /**
     * Set the bpm of the track.
     */
    public void setBPM (int bpm) {
        this.bpm = bpm;
    }
}
