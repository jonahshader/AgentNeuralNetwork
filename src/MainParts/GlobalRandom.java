package MainParts;

import java.io.Serializable;
import java.util.Random;
import java.util.SplittableRandom;

//TODO: should this be serialized into the save file? will it be deterministic?
public class GlobalRandom implements Serializable {

    public final static SplittableRandom fastRand = new SplittableRandom();
    public final static Random random = new Random();
}
