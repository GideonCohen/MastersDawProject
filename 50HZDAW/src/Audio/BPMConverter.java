package Audio;

public class BPMConverter {

    private int bpm;
    private long baseValue;

    public BPMConverter () {

        baseValue = 60000; // 1 minute
        setBPM(120);

    }

    /**
     * Set millisecond value for bars according to BPM.
     * Return milliseconds per bar.
     */

    public long setBars (int bars) {

        long bar = (baseValue / bpm) * 4;
        return (bars) * bar;
    }

    /**
     * Set millisecond value for beats according to BPM.
     * Return milliseconds per quarter beat.
     */

        public long setQuarterBeat (int quarters) {
            long quarterBeat = baseValue / bpm;
            return (quarters) * quarterBeat;
        }

    /**
     * Set millisecond value for beats according to BPM.
     * Return milliseconds per eight beat.
     */

    public long setEighthBeat (int eights) {
        long eighthBeat = (baseValue / bpm) / 2;
        return (eights) * eighthBeat;
    }

    /**
     * Set the bpm of the track.
     */
    public void setBPM (int bpm) {
        this.bpm = bpm;
    }
}
