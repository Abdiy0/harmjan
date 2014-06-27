/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hms.hwestra.utilities.gtf;

import hms.hwestra.utilities.features.Chromosome;
import hms.hwestra.utilities.features.Strand;
import umcg.genetica.text.Strings;

/**
 *
 * @author Harm-Jan
 */
public class GTFLine {
   
    private final String attribStr;
    private final Chromosome chr;
    private final Strand str;
    private int start;
    private int stop;
    private Double score;
    private Double frame;
    private String geneId;
    private String geneName;
    private String transcriptId;
    private String tssId;
    private final String typeStr;

    public GTFLine(String ln) {
        String[] elems = Strings.tab.split(ln);
        String sequenceStr = elems[0];
        String sourceStr = elems[1];
        typeStr = new String(elems[2]).intern();
        String startStr = elems[3];
        String stopStr = elems[4];
        String scoreStr = elems[5];
        String strandStr = elems[6];
        String frameStr = elems[7];
        attribStr = elems[8];

        chr = Chromosome.parseChr(sequenceStr);
        str = Strand.parseStr(strandStr);
        start = Integer.parseInt(startStr);
        stop = Integer.parseInt(stopStr);
        score = null;
        frame = null;
        try {
            score = Double.parseDouble(scoreStr);
        } catch (NumberFormatException e) {

        }
        try {
            frame = Double.parseDouble(frameStr);
        } catch (NumberFormatException e) {

        }

        String[] attribElems = attribStr.split(";");
        for (String attribElem : attribElems) {
            String[] attribSubElems = attribElem.split(" ");
            String property = attribSubElems[0].toLowerCase();
            String value = attribSubElems[1].replaceAll("\"", "");
            if (property.equals("gene_id")) {
                geneId = new String(value).intern();
            } else if (property.equals("gene_name")) {
                geneName = new String(value).intern();
            } else if (property.equals("transcript_id")) {
               transcriptId=new String(value).intern();
            } else if (property.equals("tss_id")) {
                tssId = new String(value).intern();
            } 
        }
        
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public String getTssId() {
        return tssId;
    }

    public String getAttribStr() {
        return attribStr;
    }

    public Chromosome getChr() {
        return chr;
    }

    public Strand getStr() {
        return str;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public Double getScore() {
        return score;
    }

    public Double getFrame() {
        return frame;
    }

    public String getGeneId() {
        return geneId;
    }

    public String getGeneName() {
        return geneName;
    }

    public String getType() {
        return typeStr;
    }
    
    
}
