package nl.harmjanwestra.vcfutils;

import nl.harmjanwestra.utilities.vcf.VCFGenotypeData;
import nl.harmjanwestra.utilities.vcf.VCFVariant;
import umcg.genetica.console.ProgressBar;
import umcg.genetica.containers.Pair;
import umcg.genetica.io.text.TextFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Harm-Jan on 07/02/16.
 */
public class GeneticSimilarity {


	public void determineGeneticSimilarityBetweenDatasets(String listOfVariantsToExclude, String vcf1, String vcf2, String outfile) throws IOException {

		// read list of variants
		TextFile tf1 = new TextFile(listOfVariantsToExclude, TextFile.R);
		ArrayList<String> listOfVariantsToExcludeArr = tf1.readAsArrayList();
		tf1.close();
		HashSet<String> hashSetVariantsToExclude = new HashSet<String>();
		hashSetVariantsToExclude.addAll(listOfVariantsToExcludeArr);

//		// index the variants
//		HashMap<String, Integer> variantToId = new HashMap<String, Integer>();
//		HashSet<Integer> chromosomes = new HashSet<Integer>();
//		for (int i = 0; i < listOfVariants.size(); i++) {
//			String var = listOfVariants.get(i);
//			String[] varelems = var.split("_");
//
//			chromosomes.add(Integer.parseInt(varelems[0]));
//			variantToId.put(listOfVariants.get(i), i);
//		}
//
//		VCFVariant[][] allvariants = new VCFVariant[2][listOfVariants.size()];

		// load the variants for ds1
		System.out.println("VCF1: " + vcf1);
		System.out.println("VCF2: " + vcf2);
		Pair<ArrayList<String>, ArrayList<VCFVariant>> data1 = loadData(vcf1, hashSetVariantsToExclude);
		ArrayList<String> samples1 = data1.getLeft();
		ArrayList<VCFVariant> variants1 = data1.getRight();

		System.out.println(samples1.size() + " samples in vcf1");
		System.out.println(variants1.size() + " variants in vcf1");
		Pair<ArrayList<String>, ArrayList<VCFVariant>> data2 = loadData(vcf2, hashSetVariantsToExclude);
		ArrayList<String> samples2 = data2.getLeft();
		ArrayList<VCFVariant> variants2 = data2.getRight();
		System.out.println(samples2.size() + " samples in vcf2");
		System.out.println(variants2.size() + " variants in vcf2");

		// merge set of loaded variants
		HashSet<String> hashOfVariants = new HashSet<String>();
		for (VCFVariant v : variants1) {
			hashOfVariants.add(v.toString());
		}
		for (VCFVariant v : variants2) {
			hashOfVariants.add(v.toString());
		}

		ArrayList<String> listOfVariants = new ArrayList<>();
		listOfVariants.addAll(hashOfVariants);
		HashMap<String, Integer> variantToId = new HashMap<String, Integer>();
		int varctr = 0;
		for (String s : listOfVariants) {
			variantToId.put(s, varctr);
			varctr++;
		}
		System.out.println(variantToId.size() + " unique variants..");
		VCFVariant[][] allvariants = new VCFVariant[2][listOfVariants.size()];
		for (VCFVariant v : variants1) {
			Integer id = variantToId.get(v.toString());
			if (id != null) {
				allvariants[0][id] = v;
			}
		}
		for (VCFVariant v : variants2) {
			Integer id = variantToId.get(v.toString());
			if (id != null) {
				allvariants[1][id] = v;
			}
		}

		// prune the set of variants
		int missing = 0;
		for (int i = 0; i < listOfVariants.size(); i++) {
			if (allvariants[0][i] == null || allvariants[1][i] == null) {
				missing++;
			}
		}
		System.out.println(missing + " variants not present in VCF1 or VCF2.");
		VCFVariant[][] tmpvariants = new VCFVariant[2][listOfVariants.size() - missing];
		int present = 0;
		for (int i = 0; i < listOfVariants.size(); i++) {
			if (allvariants[0][i] != null && allvariants[1][i] != null) {
				tmpvariants[0][present] = allvariants[0][i];
				tmpvariants[1][present] = allvariants[1][i];
				present++;
			}
		}
		allvariants = tmpvariants;

		System.out.println(allvariants[0].length + " variants shared between datasets");
		if (allvariants[0].length == 0) {
			System.exit(-1);
		}

		// now see if the alleles match
		VCFMerger merger = new VCFMerger();
		for (int i = 0; i < allvariants[0].length; i++) {
			VCFVariant variant1 = allvariants[0][i];
			VCFVariant variant2 = allvariants[1][i];

			String[] alleles1 = variant1.getAlleles();
			String allele1minor = variant1.getMinorAllele();
			String[] alleles2 = variant2.getAlleles();
			String allele2minor = variant2.getMinorAllele();

			int nrIdenticalAlleles = merger.countIdenticalAlleles(alleles1, alleles2);

			if (nrIdenticalAlleles != 2 || !allele1minor.equals(allele2minor)) {
				allvariants[0][i] = null;
				allvariants[1][i] = null;
			}
		}

		// prune the set of variants
		missing = 0;
		for (int i = 0; i < listOfVariants.size(); i++) {
			if (allvariants[0][i] == null || allvariants[1][i] == null) {
				missing++;
			}
		}
		tmpvariants = new VCFVariant[2][allvariants[0].length - missing];


		present = 0;
		for (int i = 0; i < listOfVariants.size(); i++) {
			if (allvariants[0][i] != null && allvariants[1][i] != null) {
				tmpvariants[0][present] = allvariants[0][i];
				tmpvariants[1][present] = allvariants[1][i];
				present++;
			}
		}
		allvariants = tmpvariants;
		System.out.println(allvariants[0].length + " variants have identical alleles");
		if (allvariants.length == 0) {
			System.exit(-1);
		}

		if (allvariants[0].length < 100) {
			System.out.println("Error: not enough info...");
			System.out.println(allvariants[0].length + " variants remain after pruning");
			System.exit(-1);
		}

		// make two dosage matrices
		double[][] dosages1 = new double[samples1.size()][allvariants[0].length];
		double[][] dosages2 = new double[samples2.size()][allvariants[0].length];

		for (int v = 0; v < allvariants[0].length; v++) {
			VCFVariant var1 = allvariants[0][v];
			double[][] dosage = var1.getDosage();
			for (int s = 0; s < samples1.size(); s++) {
				double d = dosage[s][0];
				if (Double.isNaN(d) || d == -1) {
					dosages1[s][v] = -1;
				} else {
					dosages1[s][v] = d;
				}
			}

			VCFVariant var2 = allvariants[0][v];
			dosage = var2.getDosage();
			for (int s = 0; s < samples2.size(); s++) {
				double d = dosage[s][0];
				if (Double.isNaN(d) || d == -1) {
					dosages2[s][v] = -1;
				} else {
					dosages2[s][v] = d;
				}
			}

		}

		// get the genetic similarity
		nl.harmjanwestra.utilities.vcf.GeneticSimilarity sim = new nl.harmjanwestra.utilities.vcf.GeneticSimilarity();
		Pair<double[][], double[][]> similarity = sim.calculate(allvariants);
		double[][] geneticSimilarity = similarity.getLeft();
		double[][] sharedgenotypes = similarity.getRight();
		double[][] correlationmatrix = new double[samples1.size()][samples2.size()];
		// correlate the samples
		TextFile out = new TextFile(outfile, TextFile.R);
		String header = "Sample1\tSample2\tCorrelation\tGeneticSimilarity\tPercSharedGenotypes";
		out.writeln(header);
		ArrayList<String> pairsBiggerThan9 = new ArrayList<>();
		ProgressBar pb = new ProgressBar(samples1.size(), "Correlating samples");
		for (int i = 0; i < samples1.size(); i++) {
			double[] dosage1 = dosages1[i];
			for (int j = 0; j < samples2.size(); j++) {
				double[] dosage2 = dosages2[i];


				// prune missing
				int missinggt = 0;
				for (int k = 0; k < dosage1.length; k++) {
					if (dosage1[k] == -1 || dosage2[k] == -1) {
						missinggt++;
					}
				}

				double[] x = new double[dosage1.length - missinggt];
				double[] y = new double[dosage2.length - missinggt];

				int presentgt = 0;
				for (int k = 0; k < dosage1.length; k++) {
					if (dosage1[k] == -1 || dosage2[k] == -1) {

					} else {
						x[presentgt] = dosage1[k];
						y[presentgt] = dosage2[k];
						presentgt++;
					}
				}

				double corr = JSci.maths.ArrayMath.correlation(x, y);
				if (corr > 0.9) {
					pairsBiggerThan9.add(samples1.get(i) + "\t" + samples2.get(j) + "\t" + corr + "\t" + geneticSimilarity[i][j] + "\t" + sharedgenotypes[i][j]);
				}
				correlationmatrix[i][j] = corr;
				out.writeln(samples1.get(i) + "\t" + samples2.get(j) + "\t" + corr + "\t" + geneticSimilarity[i][j] + "\t" + sharedgenotypes[i][j]);
			}
			pb.set(i);
		}
		out.close();
		pb.close();

		TextFile out2 = new TextFile(outfile + "biggerthanthreshold.txt", TextFile.W);
		out2.writeln(header);
		out2.writeList(pairsBiggerThan9);
		out2.close();

		TextFile out3 = new TextFile(outfile + "toppairpersample1.txt", TextFile.W);
		out3.writeln(header);
		for (int i = 0; i < samples1.size(); i++) {
			double maxx = 0;
			int maxj = 0;
			for (int j = 0; j < samples2.size(); j++) {
				if (correlationmatrix[i][j] > maxx) {
					maxx = correlationmatrix[i][j];
					maxj = j;
				}
			}
			out3.writeln(samples1.get(i) + "\t" + samples2.get(maxj) + "\t" + correlationmatrix[i][maxj] + "\t" + geneticSimilarity[i][maxj] + "\t" + sharedgenotypes[i][maxj]);
		}
		out3.close();

		TextFile out4 = new TextFile(outfile + "toppairpersample2.txt", TextFile.W);
		out4.writeln(header);
		for (int j = 0; j < samples2.size(); j++) {
			double maxx = 0;
			int maxi = 0;
			for (int i = 0; i < samples1.size(); i++) {

				if (correlationmatrix[i][j] > maxx) {
					maxx = correlationmatrix[i][j];
					maxi = i;
				}
			}
			out4.writeln(samples1.get(maxi) + "\t" + samples2.get(j) + "\t" + correlationmatrix[maxi][j] + "\t" + geneticSimilarity[maxi][j] + "\t" + sharedgenotypes[maxi][j]);
		}
		out4.close();

	}

	private Pair<ArrayList<String>, ArrayList<VCFVariant>> loadData(String vcf, HashSet<String> hashSetVariantsToExclude) throws IOException {

		VCFGenotypeData data1 = new VCFGenotypeData(vcf);
		ArrayList<String> samples = data1.getSamples();
		data1.close();

		TextFile tf = new TextFile(vcf, TextFile.R);
		String ln = tf.readLine();
		ArrayList<VCFVariant> variants = new ArrayList<>();
		int lnsparsed = 0;
		System.out.println("Parsing: " + vcf);
		while (ln != null) {
			if (!ln.startsWith("#")) {
				String substr = ln.substring(0, 150);
				String[] elems = substr.split("\t");
				String varId = elems[2];
				if (!hashSetVariantsToExclude.contains(varId)) {
					VCFVariant variant = new VCFVariant(ln);
					if (variant.isAutosomal() && variant.isBiallelic() && variant.getMAF() > 0.05 && variant.getHwep() > 0.001) {
						variants.add(variant);
					}
				}

			}
			lnsparsed++;
			if (lnsparsed % 1000 == 0) {
				System.out.print(lnsparsed + " lines parsed. " + variants.size() + " in memory\r");
			}
			ln = tf.readLine();
		}
		tf.close();
		System.out.println("");
		System.out.println("Done.");
		return new Pair<>(samples, variants);
	}
}