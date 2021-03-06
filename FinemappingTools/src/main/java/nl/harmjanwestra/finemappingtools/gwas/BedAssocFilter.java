package nl.harmjanwestra.finemappingtools.gwas;

import nl.harmjanwestra.finemappingtools.gwas.CLI.BedAssocFilterOptions;
import nl.harmjanwestra.utilities.association.AssociationFile;
import nl.harmjanwestra.utilities.association.AssociationResult;
import nl.harmjanwestra.utilities.bedfile.BedFileReader;
import nl.harmjanwestra.utilities.features.Feature;
import nl.harmjanwestra.utilities.legacy.genetica.io.text.TextFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by hwestra on 2/24/16.
 */
public class BedAssocFilter {


	public BedAssocFilter(BedAssocFilterOptions options) throws IOException {

		HashMap<String, Double> thresholdsPerLocus = null;
		if (options.thresholdfile != null) {
			TextFile tf = new TextFile(options.thresholdfile, TextFile.R);
			String[] elems = tf.readLineElems(TextFile.tab);
			thresholdsPerLocus = new HashMap<String, Double>();
			while (elems != null) {
				String region = elems[0];
				double threshold = Double.parseDouble(elems[2]);
				thresholdsPerLocus.put(region, threshold);
				elems = tf.readLineElems(TextFile.tab);
			}
			tf.close();
		}

		// load bed regions to testNormal
		BedFileReader bf = new BedFileReader();
		ArrayList<Feature> regions = bf.readAsList(options.regionFile);

		TextFile out = new TextFile(options.outfile, TextFile.W);

		AssociationFile assocFile = new AssociationFile();
		ArrayList<AssociationResult> allResults = assocFile.read(options.assocFile);


		ArrayList<Feature> regionsAfterFilter = new ArrayList<>();
		for (int i = 0; i < regions.size(); i++) {
			Feature region = regions.get(i);
			double threshold = options.threshold;
			if (thresholdsPerLocus != null) {
				Double t = thresholdsPerLocus.get(region.toString());
				if (t != null) {
					threshold = t;
				}
			}
			threshold = -Math.log10(threshold);

			System.out.println(i + "/" + regions.size());
			ArrayList<AssociationResult> results = filter(allResults, region);
			boolean written = false;
			for (int j = 0; j < results.size() && !written; j++) {
				AssociationResult result = results.get(j);
				if (result.getLog10Pval() > threshold) {
					out.writeln(region.toBedString());
					regionsAfterFilter.add(region);
					written = true;
				}
			}
		}
		out.close();


		if (options.printTopAssocPerRegion) {
			// now also get the top association per region
			TextFile tfout = new TextFile(options.outfile + "-topAssociations.txt", TextFile.W);
			for (int i = 0; i < regionsAfterFilter.size(); i++) {
				Feature region = regionsAfterFilter.get(i);
				ArrayList<AssociationResult> results = filter(allResults, region);

				double maxP = 0;
				AssociationResult maxResult = null;
				for (int j = 0; j < results.size(); j++) {
					AssociationResult result = results.get(j);
					if (result.getLog10Pval() > maxP) {
						maxP = result.getLog10Pval();
						maxResult = result;
					}
				}

				// write the top result
				if (maxResult != null) {
					tfout.writeln(region.toString() + "\t" + maxResult.toString());
				}

			}
			tfout.close();

		}

	}

	private ArrayList<AssociationResult> filter(ArrayList<AssociationResult> allResults, Feature region) {
		ArrayList<AssociationResult> r = allResults.stream().filter(r1 -> r1.getSnp().overlaps(region)).collect(Collectors.toCollection(ArrayList::new));
		return r;
	}
}
