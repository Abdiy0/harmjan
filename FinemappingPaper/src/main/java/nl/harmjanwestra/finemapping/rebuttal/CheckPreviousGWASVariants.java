package nl.harmjanwestra.finemapping.rebuttal;

import nl.harmjanwestra.utilities.association.AssociationFile;
import nl.harmjanwestra.utilities.association.AssociationResult;
import nl.harmjanwestra.utilities.enums.Chromosome;
import nl.harmjanwestra.utilities.features.Feature;
import nl.harmjanwestra.utilities.legacy.genetica.io.text.TextFile;
import nl.harmjanwestra.utilities.legacy.genetica.text.Strings;

import java.io.IOException;
import java.util.ArrayList;

public class CheckPreviousGWASVariants {
	
	
	public static void main(String[] args) {
		String[] finemap = new String[]{
		
		
		
		};
		String[] gwas = new String[]{
		
		};
		String[] output = new String[]{
		
		};
		String[] gwashits = new String[]{
		
		};
		
		
	}
	
	
	public void run(String[] finemap, String[] gwas, String[] gwashits, String[] out) throws IOException {
		
		AssociationFile af = new AssociationFile();
		for (int i = 0; i < finemap.length; i++) {
			System.out.println();
			System.out.println();
			ArrayList<AssociationResult> results = af.read(finemap[i]);
			
			ArrayList<String> hitlist = new ArrayList<>();
			TextFile tf2 = new TextFile(gwashits[i], TextFile.R);
			hitlist = tf2.readAsArrayList();
			tf2.close();
			
			TextFile tf = new TextFile(gwas[i], TextFile.R);
			TextFile outf = new TextFile(out[i], TextFile.W);
			String[] elems = tf.readLineElems(Strings.whitespace);
			int nrMissingSig = 0;
			int present = 0;
			int missing = 0;
			int hitsfound = 0;
			int hitsmissed = 0;
			while (elems != null) {
				
				Chromosome chr = Chromosome.parseChr(elems[0]);
				Integer pos = Integer.parseInt(elems[1]);
				Double d = Double.parseDouble(elems[2]);
				String name = elems[3];
				Feature f = new Feature();
				f.setChromosome(chr);
				f.setStart(pos);
				f.setStop(pos);
				
				
				AssociationResult r = isinset(results, f);
				
				for (String s : hitlist) {
					if (s.equals(name)) {
						if (r != null) {
							System.out.println("Hit: " + s + " present: " + (r != null) + "\t" + d + "\t" + r.getSnp().toString() + "\t" + r.getPval() + "\t" + r.getLog10Pval());
							hitsfound++;
						} else {
							System.out.println("Hit: " + s + " NOT present");
							hitsmissed++;
						}
						
					}
				}
				
				if (r == null) {
					missing++;
					if (d < 7.5E-7) {
						nrMissingSig++;
					}
					outf.writeln(Strings.concat(elems, Strings.tab));
				} else {
					present++;
				}
				
				elems = tf.readLineElems(Strings.whitespace);
			}
			outf.close();
			tf.close();
			
			System.out.println("SignificantMissing: " + nrMissingSig + "\tMissing: " + missing + "\tPresent: " + present);
		}
		
		
	}
	
	private AssociationResult isinset(ArrayList<AssociationResult> results, Feature f) {
		for (AssociationResult r : results) {
			if (r.getSnp().overlaps(f)) {
				return r;
			}
		}
		return null;
	}
	
}
