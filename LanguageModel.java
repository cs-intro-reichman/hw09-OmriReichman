import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char chr;
        In in = new In(fileName);
        List probs;
        for (int i = 0; i < this.windowLength; i++) {
            window = window + in.readChar();
        }
        while (!in.isEmpty()) {
            chr = in.readChar();
            if (CharDataMap.containsKey(window)) {
                probs = CharDataMap.get(window);
            } else {
                probs = new List();
            }
            CharDataMap.put(window, probs);
            probs.update(chr);
            window = window + chr;
            window = window.substring(1);
             }
             for (List probsL : this.CharDataMap.values()) {
                calculateProbabilities(probsL);
             }
        }

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		ListIterator listItr = probs.listIterator(0);
        int sum = 0;
        int i = 0;
       
        while (listItr.hasNext()) {
            listItr.next();
            sum = sum + probs.get(i).count;
            i++;
        }
        
        ListIterator listItr1 = probs.listIterator(0);
        i = 0;
        
        while (listItr1.hasNext()) {
            probs.get(i).p = (double) probs.get(i).count/sum;
            listItr1.next(); 
            i++;
        }

        ListIterator listItr2 = probs.listIterator(0);
        i = 0;

        while (listItr2.hasNext()) {
            if (i == 0) {
                probs.get(i).cp = probs.get(i).p;
            } else {
                int j = 0;
                while (j <= i) {
                    probs.get(i).cp = probs.get(i).cp + probs.get(j).p;
                    j++;
                }
            }
            i++;
            listItr2.next();
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		double r = randomGenerator.nextDouble();
        ListIterator listItr = probs.listIterator(0);
        int i = 0;
        CharData first = probs.getFirst();

        if (first.cp > r) {
            return first.chr;
        }
        while (listItr.hasNext()) {
            if (probs.get(i).cp > r) {
                return probs.get(i).chr;
            }
            i++;
        }
        return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < this.windowLength) {
            return initialText;
        }
        String str = initialText;
        char chr;

        for (int i = 0; i < textLength; i++) {
            str = str.substring(str.length() - windowLength);
            if (!CharDataMap.containsKey(str)) {
                return initialText;
            }
            chr = this.getRandomChar(CharDataMap.get(str));
            initialText = initialText + chr;
            str = str + chr;
        }
        return initialText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
    }
}
