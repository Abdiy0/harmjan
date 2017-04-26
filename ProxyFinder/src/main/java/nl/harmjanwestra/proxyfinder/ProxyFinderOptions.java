package nl.harmjanwestra.proxyfinder;

import org.apache.commons.cli.*;

/**
 * Created by hwestra on 3/22/16.
 */
public class ProxyFinderOptions {
	private static final Options OPTIONS;

	static {
		OPTIONS = new Options();
		Option option = Option.builder().longOpt("proxy").build();
		OPTIONS.addOption(option);

		option = Option.builder()
				.longOpt("tabix")
				.hasArg()
				.desc("Prefix for tabix path [format /path/to/chrCHR.vcf.gz]. Replace the chromosome number with CHR (will be replaced by chr number depending on input SNP or region).")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder()
				.longOpt("samplefilter")
				.hasArg()
				.desc("Limit samples to individuals in this list (one sample per line)")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder("w")
				.longOpt("windowsize")
				.hasArg()
				.desc("Window size [default 1000000]")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder("m")
				.longOpt("maf")
				.hasArg()
				.desc("MAF threshold [default: 0.005]")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder("t")
				.longOpt("threshold")
				.hasArg()
				.desc("R-squared threshold [default: 0.8]")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder("i")
				.longOpt("snps")
				.hasArg()
				.desc("SNP path (format: 3 or 6 columns, tab separated, one or two snps per line: chr pos rsid) ")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder("r")
				.longOpt("regions")
				.hasArg()
				.desc("Region bed file path")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder()
				.longOpt("pairwise")
				.desc("Perform Pairwise LD calculation (use 6 column file for --snps)")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder()
				.longOpt("vcf")
				.hasArg()
				.desc("Use non-indexed VCF as input")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder()
				.longOpt("locusld")
				.desc("Perform Pairwise LD calculation within a region (provide region with --regions)")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder("o")
				.longOpt("out")
				.hasArg()
				.desc("Output path")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder()
				.longOpt("threads")
				.hasArg()
				.desc("Nr of threads [default: 1]")
				.build();
		OPTIONS.addOption(option);

		option = Option.builder()
				.longOpt("matchrsid")
				.desc("Match variants on RS id")
				.build();
		OPTIONS.addOption(option);

	}

	public boolean matchrsid = false;
	public String tabixrefprefix;
	public String samplefilter;
	public int windowsize = 1000000;
	public double threshold = 0.8;
	public String snpfile;
	public String output;
	public int nrthreads = 1;
	public boolean pairwise;
	public String regionfile;
	public boolean locusld;
	public double mafthreshold;
	public String vcf;

	public ProxyFinderOptions(String[] args) {

		boolean run = true;
		try {
			CommandLineParser parser = new DefaultParser();
			final CommandLine cmd = parser.parse(OPTIONS, args, false);

			if (cmd.hasOption("tabix")) {
				tabixrefprefix = cmd.getOptionValue("tabix");
			} else if (cmd.hasOption("vcf")) {
				vcf = cmd.getOptionValue("vcf");
			} else {

				System.out.println("Provide reference");
				run = false;
			}

			if (cmd.hasOption("matchrsid")) {
				matchrsid = true;
			}

			if (cmd.hasOption("w")) {
				windowsize = Integer.parseInt(cmd.getOptionValue("w"));
			}

			if (cmd.hasOption("samplefilter")) {
				samplefilter = cmd.getOptionValue("samplefilter");
			}

			if (cmd.hasOption("pairwise")) {
				pairwise = true;
			}

			if (cmd.hasOption("locusld")) {
				locusld = true;
			}

			if (cmd.hasOption("t")) {
				threshold = Double.parseDouble(cmd.getOptionValue("t"));
			}

			if (cmd.hasOption("i")) {
				snpfile = cmd.getOptionValue("i");
			}

			if (cmd.hasOption("r")) {
				regionfile = cmd.getOptionValue("r");
			}

			if (cmd.hasOption("o")) {
				output = cmd.getOptionValue("o");
			} else {
				System.out.println("Provide output");
				run = false;
			}

			if (cmd.hasOption("maf")) {
				mafthreshold = Double.parseDouble(cmd.getOptionValue("maf"));
			}

			if (cmd.hasOption("threads")) {
				nrthreads = Integer.parseInt(cmd.getOptionValue("threads"));
			}


		} catch (ParseException e) {
			e.printStackTrace();


		}

		if (!run) {
			printHelp();
			System.exit(-1);
		}
	}

	public void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(" ", OPTIONS);
		System.exit(-1);
	}

}
